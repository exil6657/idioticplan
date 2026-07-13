package com.zenith.client.flipping.ah;

import com.zenith.client.ZenithClient;
import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.order.OrderState;

/**
 * High-level orchestrator for Auction House GUI interactions. Wraps
 * {@link AuctionHouseExecutor} (the per-order state machine) with lifecycle
 * methods used by {@link com.zenith.client.flipping.FlipEngine}.
 *
 * <p>All slot lookups go through {@link AuctionHouseGUI} +
 * {@link com.zenith.client.core.interaction.GUISlotFinder} (master rule §1);
 * all clicks go through
 * {@link com.zenith.client.core.interaction.GUIClickExecutor}
 * with humanised {@code DelayManager} gaps (rule §5).</p>
 */
public final class AuctionHouseInteractor {

    private static final AuctionHouseInteractor INSTANCE = new AuctionHouseInteractor();
    public static AuctionHouseInteractor getInstance() { return INSTANCE; }

    private AuctionHouseInteractor() {}

    /** Queue an order for BIN-buy. The executor walks the AH browser to find and buy it. */
    public void beginBuy(Order o) {
        if (o == null) return;
        o.transition(OrderState.NAVIGATING);
        AuctionHouseExecutor.getInstance().buy(o);
    }

    /** Queue an order for listing (item must already be in inventory / HOLDING). */
    public void beginList(Order o) {
        if (o == null) return;
        o.transition(OrderState.NAVIGATING);
        AuctionHouseExecutor.getInstance().list(o);
    }

    public void cancelBuy() {
        // The executor self-fails; nothing to cancel here directly.
        ZenithClient.LOGGER.debug("[AH] cancelBuy called");
    }

    public void cancelAll() {
        cancelBuy();
    }

    /** @return true if the executor is currently driving an AH flow. */
    public boolean busy() { return AuctionHouseExecutor.getInstance().busy(); }

    public void tick() {
        AuctionHouseExecutor.getInstance().tick();
    }

    /**
     * Dequeue PROPOSED/QUEUED_TO_BUY buy orders and HOLDING orders for listing into the
     * executor if it's idle.
     */
    public void pump(OrderManager mgr) {
        if (busy()) return;
        // Prefer finishing listings (already paid for) before new buys.
        Order toList = mgr.pollToList();
        if (toList != null) { beginList(toList); return; }
        Order toBuy = mgr.pollToBuy();
        if (toBuy != null) beginBuy(toBuy);
    }
}
