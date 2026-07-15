package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;

import java.util.Random;

/** A fast (400 ms) snap-jerk to a random direction, as if "what was that?" */
public class PanicLookAction extends ReactionAction {

    private static final long DURATION = 400L;
    private final Random rng = new Random();
    private boolean fired = false;

    @Override
    protected void onStart(long nowMs) {
        var p = Minecraft.getInstance().player;
        if (p == null) { fired = true; return; }
        float currentYaw = p.getYRot();
        float currentPitch = p.getXRot();
        float dyaw = (float) (rng.nextGaussian() * 25d);     // ±~25°
        float dpitch = (float) MathUtils.clamp(rng.nextGaussian() * 8d, -15d, 20d);
        float targetYaw = currentYaw + dyaw;
        float targetPitch = MathUtils.clampf(currentPitch + dpitch, -85f, 85f);
        ZenithEyes.getInstance().setEnabled(true);
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(targetYaw).pitch(targetPitch)
                .priority(RotationRequest.Priority.FAILSAFE)
                .durationMs(DURATION - 50)
                .profile("snappy")
                .tag("failsafe-panic")
                .preemptible(false)
                .build());
        fired = true;
    }

    @Override
    public boolean isDone(long nowMs) { return fired && (nowMs - startedAt) >= DURATION; }

    @Override
    public String label() { return "panic-look"; }
}
