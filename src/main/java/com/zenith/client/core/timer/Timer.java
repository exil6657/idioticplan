package com.zenith.client.core.timer;

/**
 * Simple elapsed-time timer using {@link System#nanoTime()}.
 *
 * <p>Does NOT use {@code Thread.sleep} — callers poll {@link #hasElapsed(long)}
 * from a tick/event loop (master rule §5: no fixed delays).</p>
 */
public class Timer {

    private long lastResetNs;

    public Timer() {
        reset();
    }

    public void reset() {
        lastResetNs = System.nanoTime();
    }

    /** @return elapsed time in milliseconds since last reset. */
    public long getElapsedMs() {
        return (System.nanoTime() - lastResetNs) / 1_000_000L;
    }

    public boolean hasElapsed(long ms) {
        return getElapsedMs() >= ms;
    }

    /** Reset if elapsed, returning true. Sugar for rate-limited loops. */
    public boolean runIfElapsed(long ms, Runnable action) {
        if (hasElapsed(ms)) {
            reset();
            action.run();
            return true;
        }
        return false;
    }
}
