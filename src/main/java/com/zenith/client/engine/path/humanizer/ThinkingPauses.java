package com.zenith.client.engine.path.humanizer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Injects very brief full-stops (200-500 ms) mid-route to simulate a player
 * thinking, checking chat, adjusting something in their hotbar, or glancing
 * around. Frequency is low (~every 20 seconds on average) so it doesn't kill
 * throughput but defeats anticheat pattern detectors.
 */
public final class ThinkingPauses {

    private long nextPauseAt;
    private long pauseUntil;

    public ThinkingPauses() { scheduleNext(System.currentTimeMillis()); }

    private void scheduleNext(long now) {
        nextPauseAt = now + 15_000 + ThreadLocalRandom.current().nextLong(25_000);
    }

    public boolean isPaused(long now) {
        if (now < pauseUntil) return true;
        if (now >= nextPauseAt) {
            pauseUntil = now + 200 + ThreadLocalRandom.current().nextLong(400);
            scheduleNext(now);
            return true;
        }
        return false;
    }

    public void reset() { pauseUntil = 0; scheduleNext(System.currentTimeMillis()); }
}
