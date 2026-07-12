package com.zenith.client.core.timer;

/**
 * Tick-based timer driven by the client tick event (~20 tps).
 *
 * <p>Used when timing should be tied to game ticks rather than wall-clock
 * (e.g., attack cooldowns, placement delays).</p>
 */
public class TickTimer {

    private int ticksRemaining;

    public void reset(int ticks) { this.ticksRemaining = ticks; }

    /** @return true once every {@code interval} ticks. Resets internally. */
    public boolean every(int interval) {
        ticksRemaining--;
        if (ticksRemaining <= 0) {
            ticksRemaining = interval;
            return true;
        }
        return false;
    }

    public boolean hasElapsed() { return ticksRemaining <= 0; }
}
