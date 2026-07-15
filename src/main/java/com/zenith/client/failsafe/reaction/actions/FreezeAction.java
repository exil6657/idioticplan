package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.failsafe.reaction.ReactionAction;

import java.util.Random;

/** Short "frozen" pause — the player stops dead for 300–800 ms as if surprised. */
public class FreezeAction extends ReactionAction {

    private long durationMs;
    private final Random rng = new Random();

    @Override
    protected void onStart(long nowMs) {
        // Gaussian-distributed freeze centred on 500 ms with ±200 ms sigma, clamped 250..900.
        durationMs = (long) MathUtils.clamp(500d + rng.nextGaussian() * 150d, 250d, 900d);
    }

    @Override
    public void tick(long nowMs) { /* do nothing — input is already frozen by the manager */ }

    @Override
    public boolean isDone(long nowMs) { return (nowMs - startedAt) >= durationMs; }

    @Override
    public String label() { return "freeze"; }
}
