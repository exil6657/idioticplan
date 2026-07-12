package com.zenith.client.flipping;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BazaarCache;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.api.moulberry.LowestBINFetcher;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.*;
import com.zenith.client.core.util.ThreadUtils;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.ah.AHCraftFlipEngine;
import com.zenith.client.flipping.ah.AHSalesTracker;
import com.zenith.client.flipping.ah.AuctionHouseInteractor;
import com.zenith.client.flipping.bazaar.BazaarFlipEngine;
import com.zenith.client.flipping.break_.BreakScheduler;
import com.zenith.client.flipping.break_.IdleBehavior;
import com.zenith.client.flipping.break_.PostBreakActionQueue;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.craft.CraftFlipEngine;
import com.zenith.client.flipping.filter.FilterPreset;
import com.zenith.client.flipping.filter.ItemFilterManager;
import com.zenith.client.flipping.npc.NPCFlipEngine;
import com.zenith.client.flipping.order.FillMonitor;
import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.order.OrderState;
import com.zenith.client.flipping.profit.ProfitCalculator;
import com.zenith.client.flipping.profit.ProfitTracker;
import com.zenith.client.flipping.scanner.MarketScanner;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Master controller for the AH/Bazaar/NPC/Craft flipping system.
 *
 * <p>Subsystems driven by this class:</p>
 * <ul>
 *   <li>MarketScanner — polls Coflnet/Moulberry for underpriced BIN listings.</li>
 *   <li>BazaarFlipEngine — watches buy/sell spreads for bazaar flips.</li>
 *   <li>CraftFlipEngine — computes profit from raw material → crafted item.</li>
 *   <li>NPCFlipEngine — compares NPC-sell prices to BIN/Bazaar.</li>
 *   <li>AHCraftFlipEngine — AH buy → craft → AH relist.</li>
 *   <li>OrderManager — lifecycle of every active flip order.</li>
 *   <li>BreakScheduler + IdleBehavior — scheduled AFK-looking breaks.</li>
 *   <li>AuctionHouseInteractor — GUI click state-machine for buying/listing.</li>
 *   <li>BudgetManager — purse + bank reserve awareness.</li>
 *   <li>ProfitTracker + AHSalesTracker — telemetry.</li>
 * </ul>
 *
 * <p><b>Design notes:</b></p>
 * <ul>
 *   <li>Everything runs through state machines (master rule §6).</li>
 *   <li>All buys/listings go through the failsafe gate (rule §1).</li>
 *   <li>Every GUI click uses GUISlotFinder + DelayManager (rules §1, §5).</li>
 *   <li>No packet-sent movement or rotations (rules §2, §3).</li>
 * </ul>
 */
public final class FlipEngine {

    private static final FlipEngine INSTANCE = new FlipEngine();
    public static FlipEngine getInstance() { return INSTANCE; }

    private boolean initialised = false;
    private boolean running = false;
    private long startedAtMs;

    // Subsystems.
    private final FlipperConfig config = new FlipperConfig();
    private final MarketScanner scanner = MarketScanner.getInstance();
    private final BazaarFlipEngine bazaar = BazaarFlipEngine.getInstance();
    private final CraftFlipEngine craft = CraftFlipEngine.getInstance();
    private final NPCFlipEngine npc = NPCFlipEngine.getInstance();
    private final AHCraftFlipEngine ahCraft = AHCraftFlipEngine.getInstance();
    private final OrderManager orders = OrderManager.getInstance();
    private final AuctionHouseInteractor ah = AuctionHouseInteractor.getInstance();
    private final BreakScheduler breaks = BreakScheduler.getInstance();
    private final IdleBehavior idle = IdleBehavior.getInstance();
    private final BudgetManager budget = BudgetManager.getInstance();
    private final ProfitTracker profit = ProfitTracker.getInstance();
    private final AHSalesTracker sales = AHSalesTracker.getInstance();
    private final FillMonitor fillMonitor = FillMonitor.getInstance();

    private FlipEngine() {}

    public void init() {
        if (initialised) return;
        initialised = true;
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new EventListener());
        budget.init();
        ItemFilterManager.getInstance().setActive("high-volume");
        breaks.start();
        scanner.start();
        bazaar.start();

        // Periodic housekeeping (order ticks, fill monitor, break tick) every 1 s.
        ThreadUtils.scheduler().scheduleAtFixedRate(this::slowTick, 1, 1, TimeUnit.SECONDS);

        ZenithClient.LOGGER.info("[FlipEngine] Initialised (AH+Bazaar+NPC+Craft strategies).");
        ZenithChat.getInstance().success("Flip engine initialised (strategies: AH BIN, Bazaar, NPC, Craft).");
    }

    public void start() {
        if (running) return;
        running = true;
        startedAtMs = System.currentTimeMillis();
        ZenithChat.getInstance().info("Flipper started — budget={}, minProfit={} ({})",
                config.maxBudget(), config.minProfitCoins, ItemFilterManager.getInstance().activeName());
    }

    public void stop() {
        running = false;
        ah.cancelAll();
        orders.failAll("manual stop");
        ZenithChat.getInstance().info("Flipper stopped.");
    }

    public boolean isRunning() { return running; }
    public FlipperConfig config() { return config; }
    public MarketScanner scanner() { return scanner; }
    public OrderManager orders() { return orders; }
    public ProfitTracker profit() { return profit; }
    public BudgetManager budget() { return budget; }

    /** Called once per client tick (hooked into ClientTickDispatcher indirectly via GuiEngine). */
    public void tick() {
        if (!running) return;
        if (FailsafeManager.getInstance().areMacrosPaused()) {
            // Freeze new orders during a failsafe; existing orders are already failed by failsafe chain.
            return;
        }

        // 1. Scheduled breaks: on break we only do idle-looking camera movements, no new orders.
        breaks.tick();
        idle.tick(System.currentTimeMillis());
        if (breaks.isOnBreak()) return;

        // 2. Drive order lifecycle.
        orders.tick();
        fillMonitor.tick();
        ah.tick();
        ah.pump(orders); // hand PROPOSED/NAVIGATING buy orders to AH executor if idle
        ahCraft.tick();

        // 3. Consume up to 2 candidates from the scanner queue.
        consumeCandidates();

        // 4. Bazaar engine produces its own candidates (spread-based); consume those too.
        var bc = bazaar.pollCandidate();
        if (bc != null && budget.canBuy(bc.buyPrice)) orders.enqueue(bc);

        // 5. NPC flips.
        var nc = npc.pollCandidate();
        if (nc != null && budget.canBuy(nc.buyPrice)) orders.enqueue(nc);
    }

    private void slowTick() {
        try {
            // Background periodic housekeeping: refresh BIN cache age, recalculate budget, etc.
            budget.recalc();
            if (FailsafeManager.getInstance().areMacrosPaused()) {
                orders.failAll("failsafe");
            }
        } catch (Throwable t) {
            ZenithClient.LOGGER.error("[FlipEngine] slowTick failed", t);
        }
    }

    private void consumeCandidates() {
        int perTick = 2;
        for (int i = 0; i < perTick; i++) {
            FlipCandidate c = scanner.poll();
            if (c == null) break;
            if (!budget.canBuy(c.buyPrice)) continue;
            if (!ItemFilterManager.getInstance().activeFilter().allows(c.itemId, c.buyPrice)) continue;
            orders.enqueue(c);
        }
    }

    public FlipDebugData debugData() {
        return new FlipDebugData(
                running,
                orders.activeOrders().size(),
                orders.listedCount(),
                orders.holdingCount(),
                profit.sessionProfit(),
                profit.sessionFlips(),
                scanner.queueDepth(),
                scanner.scans(),
                BINCache.getInstance().size(),
                BazaarCache.getInstance().size(),
                breaks.isOnBreak(),
                ah.busy(),
                budget.config().coinsAvailable,
                sales.successRate()
        );
    }

    /** Event listener nested class (keeps the bus-facing surface separate). */
    private final class EventListener {
        @SubscribeEvent
        public void onFailsafe(FailsafeTriggerEvent ev) {
            orders.failAll("failsafe:" + ev.getFailsafeName());
        }

        @SubscribeEvent
        public void onBreakStart(BreakStartEvent ev) {
            // Pause new orders as the break starts. Existing orders in LISTED state stay listed.
            orders.failAll("scheduled break");
        }

        @SubscribeEvent
        public void onBreakEnd(BreakEndEvent ev) {
            PostBreakActionQueue.getInstance().enqueue(() -> budget.recalc());
            PostBreakActionQueue.getInstance().enqueue(() -> LowestBINFetcher.getInstance().fetchAsync());
            PostBreakActionQueue.getInstance().drain();
        }
    }
}
