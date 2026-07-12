package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/**
 * Fired when the player swaps to a different hotbar slot.
 * Phase 7: used by the failsafe item-swap detector.
 */
public class HeldItemChangeEvent extends ZenithEvent {

    private final int slot;
    private final int prevSlot;

    public HeldItemChangeEvent(int slot, int prevSlot) {
        this.slot = slot;
        this.prevSlot = prevSlot;
    }

    public int getSlot() { return slot; }
    public int getPrevSlot() { return prevSlot; }

    @Override public boolean isCancellable() { return false; }
}
