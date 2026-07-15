package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;

import java.util.Random;

/**
 * Slow 600–1000 ms look around (head sweep), as if the player is scanning the
 * environment to see what startled them.
 */
public class SlowLookAroundAction extends ReactionAction {

    private static final long DURATION = 900L;
    private final Random rng = new Random();
    private boolean fired = false;

    @Override
    protected void onStart(long nowMs) {
        var p = Minecraft.getInstance().player;
        if (p == null) { fired = true; return; }
        float yaw = p.getYRot();
        float pitch = p.getXRot();
        float delta = (rng.nextBoolean() ? 1f : -1f) * (20f + rng.nextFloat() * 25f);
        ZenithEyes.getInstance().setEnabled(true);
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(yaw + delta)
                .pitch(pitch + (rng.nextFloat() * 6f - 3f))
                .priority(RotationRequest.Priority.FAILSAFE)
                .durationMs(DURATION - 100)
                .profile("legit")
                .tag("failsafe-scan")
                .preemptible(false)
                .build());
        fired = true;
    }

    @Override
    public boolean isDone(long nowMs) { return fired && (nowMs - startedAt) >= DURATION; }

    @Override
    public String label() { return "slow-look"; }
}
