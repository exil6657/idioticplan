package com.zenith.client.flipping.budget;

import com.zenith.client.api.cache.BINCache;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.InventoryOpenEvent;
import com.zenith.client.core.event.events.PurseChangeEvent;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.profit.ProfitTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Tracks purse + inventory coins + resellable holdings to derive how much
 * the flipper has available to spend. Wired to PurseChangeEvent and inventory
 * open (which triggers a rescan of coin items in inventory).
 */
public final class BudgetManager {

    private static final BudgetManager INSTANCE = new BudgetManager();
    public static BudgetManager getInstance() { return INSTANCE; }

    private final BudgetConfig config = new BudgetConfig();
    private long purse = 0L;

    private BudgetManager() {}

    public void init() {
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new Listener());
    }

    public BudgetConfig config() { return config; }
    public long purse() { return purse; }

    public void setPurse(long coins) {
        this.purse = Math.max(0, coins);
        config.coinsAvailable = purse + inventoryCoinValue();
    }

    public void recalc() {
        Minecraft mc = Minecraft.getInstance();
        long invCoins = inventoryCoinValue();
        config.coinsAvailable = purse + invCoins;
    }

    /**
     * Coins held as items (enchanted gold block, etc.) — kept minimal in Phase 9; a more
     * thorough scan is added when the inventory scraper is implemented.
     */
    private long inventoryCoinValue() {
        var p = Minecraft.getInstance().player;
        if (p == null) return 0L;
        long coins = 0L;
        // Count actual coin items if present (rare) — don't value held flips to avoid
        // double-counting.
        return coins;
    }

    public boolean canBuy(long price) {
        if (FailsafeManager.getInstance().areMacrosPaused()) return false;
        if (sessionOverLossCeiling()) return false;
        return config.canAfford(price);
    }

    public boolean sessionOverLossCeiling() {
        return config.sessionLossCeiling > 0 && ProfitTracker.getInstance().sessionProfit() < -config.sessionLossCeiling;
    }

    private final class Listener {
        @SubscribeEvent
        public void onPurse(PurseChangeEvent ev) { setPurse(ev.getNewCoins()); }
        @SubscribeEvent
        public void onInv(InventoryOpenEvent ev) { recalc(); }
    }
}
