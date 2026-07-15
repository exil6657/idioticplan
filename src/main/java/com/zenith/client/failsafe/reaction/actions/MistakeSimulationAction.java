package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;

import java.util.Random;

/**
 * Small "I misclicked" look-around: two tiny jerks (3–8°) then back. Total
 * duration ~500 ms. Looks like the player twitched in surprise.
 */
public class MistakeSimulationAction extends ReactionAction {

    private static final long DURATION = 500L;
    private final Random rng = new Random();
    private int step = 0;
    private long stepAt;
    private boolean fired = false;

    @Override
    protected void onStart(long nowMs) {
        var p = Minecraft.getInstance().player;
        if (p == null) { fired = true; return; }
        fireTwitch(p.getYRot(), p.getXRot(), 120L);
        step = 1;
        stepAt = nowMs;
        fired = true;
    }

    @Override
    public void tick(long nowMs) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        if (step == 1 && (nowMs - stepAt) > 150) {
            fireTwitch(p.getYRot(), p.getXRot(), 120L);
            step = 2;
            stepAt = nowMs;
        }
    }

    private void fireTwitch(float baseYaw, float basePitch, long dur) {
        float dyaw = (float) (rng.nextGaussian() * 6d);
        float dpitch = (float) (rng.nextGaussian() * 2.5d);
        ZenithEyes.getInstance().setEnabled(true);
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(baseYaw + dyaw)
                .pitch(basePitch + dpitch)
                .priority(RotationRequest.Priority.FAILSAFE)
                .durationMs(dur)
                .profile("snappy")
                .tag("failsafe-mistake")
                .preemptible(false)
                .build());
    }

    @Override
    public boolean isDone(long nowMs) { return fired && (nowMs - startedAt) >= DURATION; }

    @Override
    public String label() { return "twitch"; }
}
