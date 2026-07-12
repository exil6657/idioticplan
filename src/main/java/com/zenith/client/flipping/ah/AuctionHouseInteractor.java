package com.zenith.client.flipping.ah;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
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

    /** Queue an order for listing (held item). Listing flow filled in the next Phase 10 iteration. */
    public void beginList(Order o) {
        if (o == null) return;
        o.transition(OrderState.NAVIGATING);
        ZenithClient.LOGGER.debug("[AH] beginList for {} (listing flow pending)", o.itemId());
        // TODO Phase 10 iteration 2: full list flow (OPEN_MANAGE → CHOOSE_ITEM → CREATE → SET_PRICE → LISTED).
    }

    public void cancelBuy() {
        // Nothing to cancel on the executor itself; it self-fails, but mark active order failed.
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

    /** Dequeue PROPOSED/NAVIGATING buy orders into the executor if it's idle. */
    public void pump(OrderManager mgr) {
        if (busy()) return;
        Order next = mgr.pollToBuy();
        if (next != null) beginBuy(next);
    }
}
