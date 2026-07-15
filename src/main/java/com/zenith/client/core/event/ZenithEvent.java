package com.zenith.client.core.event;

/**
 * Base class for all events dispatched on the {@link ZenithEventBus}.
 *
 * <p>Events are cancellable by default; subclasses override {@link #isCancellable()}
 * to return {@code false} if they are informational-only. Cancelled events do
 * not reach subscribers with a priority lower than the cancelling subscriber.</p>
 */
public class ZenithEvent {

    private boolean cancelled = false;

    /** @return {@code true} if this event type supports cancellation. */
    public boolean isCancellable() {
        return true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (!isCancellable() && cancelled) {
            throw new IllegalStateException("Event " + getClass().getSimpleName() + " is not cancellable");
        }
        this.cancelled = cancelled;
    }

    public void cancel() {
        setCancelled(true);
    }
}
