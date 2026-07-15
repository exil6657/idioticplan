package com.zenith.client.flipping.order;

import com.zenith.client.ZenithClient;
import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.profit.FlipRecord;
import com.zenith.client.flipping.profit.ProfitTracker;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Drives all active flip orders through their lifecycle: proposed → queued →
 * navigating → buying → holding → listed → collecting → completed/failed.
 *
 * <p>The manager is ticked by {@link com.zenith.client.flipping.FlipEngine} and
 * orders transition in response to external events (GUI parsing, chat, failsafe
 * triggers, timeouts).</p>
 */
public final class OrderManager {

    private static final OrderManager INSTANCE = new OrderManager();
    public static OrderManager getInstance() { return INSTANCE; }

    private final List<Order> active = new CopyOnWriteArrayList<>();
    private final Deque<Order> buyQueue = new ArrayDeque<>();        // AH buy
    private final Deque<Order> ahListQueue = new ArrayDeque<>();     // AH BIN list
    private final Deque<Order> bazaarBuyQueue = new ArrayDeque<>();  // Bazaar instant/limit buy
    private final Deque<Order> bazaarSellQueue = new ArrayDeque<>(); // Bazaar instant/limit sell

    private OrderManager() {}

    public List<Order> activeOrders() { return Collections.unmodifiableList(active); }

    public int listedCount() {
        int n = 0; for (Order o : active) if (o.state == OrderState.LISTED) n++; return n;
    }

    public int holdingCount() {
        int n = 0; for (Order o : active) if (o.state == OrderState.HOLDING) n++; return n;
    }

    public boolean enqueue(FlipCandidate c) {
        if (c == null) return false;
        if (!BudgetManager.getInstance().canBuy(c.buyPrice)) return false;
        // Dedup: don't queue the same auction twice.
        if (!c.auctionUUID.isEmpty()) {
            for (Order o : active) if (c.auctionUUID.equals(o.candidate.auctionUUID)) return false;
        }
        int cap = BudgetManager.getInstance().config().maxOpenOrders;
        synchronized (active) {
            if (active.size() >= cap) return false;
            Order o = new Order(c);
            o.transition(OrderState.QUEUED_TO_BUY);
            active.add(o);
            // Route by flip type: Bazaar spreads go to the Bazaar executor; AH / NPC / Craft go to AH buy queue.
            switch (c.type) {
                case BAZAAR_SPREAD -> bazaarBuyQueue.offer(o);
                default           -> buyQueue.offer(o);
            }
            ZenithClient.LOGGER.info("[Order] Queued {} ({}) buy@{} list@{} profit={}",
                    c.itemId, c.type, c.buyPrice, c.sellPrice, c.expectedProfit);
            return true;
        }
    }

    public Order pollToBuy() { return buyQueue.poll(); }
    public Order pollToList() { return ahListQueue.poll(); }
    public Order pollToBazaarBuy() { return bazaarBuyQueue.poll(); }
    public Order pollToBazaarSell() { return bazaarSellQueue.poll(); }

    public void enqueueBazaarBuy(Order o) {
        if (o == null) return;
        o.transition(OrderState.QUEUED_TO_BUY);
        if (!active.contains(o)) active.add(o);
        bazaarBuyQueue.offer(o);
    }

    public void enqueueBazaarSell(Order o) {
        if (o == null) return;
        if (!active.contains(o)) active.add(o);
        bazaarSellQueue.offer(o);
    }

    public void markBought(Order o) {
        BudgetManager.getInstance().config().recordBuy(o.buyPrice);
        o.transition(OrderState.HOLDING);
        ahListQueue.offer(o);
    }

    public void markBoughtBazaar(Order o) {
        BudgetManager.getInstance().config().recordBuy(o.buyPrice);
        o.transition(OrderState.HOLDING);
        bazaarSellQueue.offer(o);
    }

    public void markListed(Order o, long listPrice) {
        o.listPrice = listPrice;
        o.transition(OrderState.LISTED);
    }

    public void markCompleted(Order o, long sellPrice) {
        o.transition(OrderState.COMPLETED);
        long profit = sellPrice - o.buyPrice;
        BudgetManager.getInstance().config().recordSell(profit);
        ProfitTracker.getInstance().record(new FlipRecord(
                o.candidate.itemId, o.candidate.itemName, o.candidate.type,
                o.buyPrice, sellPrice, profit, o.heldMs(), true, ""));
        active.remove(o);
    }

    public void fail(Order o, String reason) {
        o.transition(OrderState.FAILED);
        o.note = reason;
        active.remove(o);
        ZenithClient.LOGGER.warn("[Order] FAILED {}: {}", o.itemId(), reason);
    }

    /** Fail all active orders (used by failsafe). */
    public void failAll(String reason) {
        for (Order o : new ArrayList<>(active)) fail(o, reason);
        buyQueue.clear();
        ahListQueue.clear();
        bazaarBuyQueue.clear();
        bazaarSellQueue.clear();
    }

    public void tick() {
        long now = System.currentTimeMillis();
        for (Order o : new ArrayList<>(active)) {
            // Timeout: orders stuck in BUYING/NAVIGATING for >45 s fail.
            if ((o.state == OrderState.BUYING || o.state == OrderState.NAVIGATING)
                    && (now - o.enteredStateAtMs) > 45_000L) {
                fail(o, "buy/navigation timeout");
                continue;
            }
            // Listings older than 30 min are considered stuck; auto-cancel check deferred.
        }
    }
}
