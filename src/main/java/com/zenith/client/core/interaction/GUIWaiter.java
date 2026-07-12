package com.zenith.client.core.interaction;

import com.zenith.client.core.timer.Timer;

import java.util.function.BooleanSupplier;

/**
 * Waits for a GUI condition to be true (e.g. "slot X is now Y", "title changed to Z"),
 * returning true when the condition is met or the timeout expires. Used by macro
 * code instead of Thread.sleep.
 */
public final class GUIWaiter {

    private final Timer timer = new Timer();
    private BooleanSupplier condition;
    private long timeoutMs;
    private boolean started;

    public GUIWaiter waitFor(BooleanSupplier cond, long timeoutMs) {
        this.condition = cond; this.timeoutMs = timeoutMs;
        timer.reset(); started = true;
        return this;
    }

    /** @return true if condition met; false on timeout; null while waiting. */
    public Boolean check() {
        if (!started) return false;
        if (condition == null) return false;
        if (condition.getAsBoolean()) { started = false; return true; }
        if (timer.hasElapsed(timeoutMs)) { started = false; return false; }
        return null;
    }

    public void cancel() { started = false; condition = null; }
}
