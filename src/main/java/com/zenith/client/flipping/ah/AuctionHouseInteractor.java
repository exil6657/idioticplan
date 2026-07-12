package com.zenith.client.flipping.ah;

import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.OrderState;

/**
 * Orchestrates the sequence of GUI actions required to (a) BIN-buy an auction
 * and (b) list a held item on the AH. Uses the core interaction layer
 * ({@link com.zenith.client.core.interaction.GUIClickExecutor},
 * {@link com.zenith.client.core.interaction.GUISlotFinder}) for rule §1 slot
 * lookup and {@link com.zenith.client.core.timer.DelayManager} for humanised
 * delays.
 *
 * <p>Phase 9 just defines the state-machine stubs and public API; actual
 * click wiring comes with the GUI system (Phase 10 macro-GUI layer).</p>
 */
public final class AuctionHouseInteractor {

    private static final AuctionHouseInteractor INSTANCE = new AuctionHouseInteractor();
    public static AuctionHouseInteractor getInstance() { return INSTANCE; }

    private volatile Order activeBuy;
    private volatile Order activeList;

    private AuctionHouseInteractor() {}

    public void beginBuy(Order o) {
        this.activeBuy = o;
        o.transition(OrderState.NAVIGATING);
        // TODO Phase 10: navigate to /ah, search for item, click BIN
    }

    public void beginList(Order o) {
        this.activeList = o;
        o.transition(OrderState.NAVIGATING);
        // TODO Phase 10: open AH, place item, set price, confirm listing
    }

    public void cancelBuy() {
        if (activeBuy != null) activeBuy.transition(OrderState.FAILED);
        activeBuy = null;
    }

    public void cancelAll() {
        cancelBuy();
        activeList = null;
    }

    public boolean busy() { return activeBuy != null || activeList != null; }

    public void tick() {
        // Phase 10: tick the GUI nav/click state machines.
    }
}
