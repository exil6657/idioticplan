package com.zenith.client.flipping.break_;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.BreakEndEvent;
import com.zenith.client.core.event.events.BreakStartEvent;
import com.zenith.client.core.util.MathUtils;
import com.zenith.client.flipping.order.OrderManager;

import java.util.Random;

/**
 * Schedules randomised breaks during long flipping sessions. Mirrors a human
 * walking away from keyboard for a few minutes every ~45 minutes of play.
 */
public final class BreakScheduler {

    private static final BreakScheduler INSTANCE = new BreakScheduler();
    public static BreakScheduler getInstance() { return INSTANCE; }

    private final BreakConfig config = new BreakConfig();
    private final Random rng = new Random();
    private long startedAt;
    private long nextBreakAt;
    private long breakUntil;
    private boolean onBreak;

    private BreakScheduler() {}

    public BreakConfig config() { return config; }
    public boolean isOnBreak() { return onBreak; }

    public void start() {
        startedAt = System.currentTimeMillis();
        scheduleNext();
    }

    private void scheduleNext() {
        double jitter = 1d + rng.nextGaussian() * config.breakJitterSigma;
        long next = (long) (config.playSessionMs * Math.max(0.4d, jitter));
        nextBreakAt = System.currentTimeMillis() + next;
        breakUntil = 0;
        onBreak = false;
    }

    public void tick() {
        if (!config.enabled) { if (onBreak) end(); return; }
        long now = System.currentTimeMillis();
        if (!onBreak && now >= nextBreakAt) {
            begin(now);
        } else if (onBreak && now >= breakUntil) {
            end();
        }
    }

    private void begin(long now) {
        onBreak = true;
        double durationJ = 1d + rng.nextGaussian() * config.breakJitterSigma;
        breakUntil = now + (long) (config.breakDurationMs * Math.max(0.5d, durationJ));
        ZenithEventBus.getInstance().post(new BreakStartEvent(config.breakDurationMs));
        ZenithChat.getInstance().info("Scheduled break for {} s", (breakUntil - now)/1000);
    }

    private void end() {
        onBreak = false;
        ZenithEventBus.getInstance().post(new BreakEndEvent(0));
        ZenithChat.getInstance().info("Break over — resuming.");
        scheduleNext();
    }

    /** Reset the play timer (e.g. after user manually interacted). */
    public void kick() { startedAt = System.currentTimeMillis(); }
}
