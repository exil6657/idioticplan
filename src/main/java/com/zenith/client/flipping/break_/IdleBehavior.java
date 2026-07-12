package com.zenith.client.flipping.break_;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import net.minecraft.client.Minecraft;

import java.util.Random;

/**
 * Idle-looking camera/body behaviour during scheduled breaks: slow lookarounds,
 * occasional camera wiggles, no movement. Makes the player look AFK rather
 * than bot-paused.
 */
public final class IdleBehavior {

    private static final IdleBehavior INSTANCE = new IdleBehavior();
    public static IdleBehavior getInstance() { return INSTANCE; }

    private final Random rng = new Random();
    private long nextLookAtMs;

    private IdleBehavior() {}

    public void tick(long nowMs) {
        if (!BreakScheduler.getInstance().isOnBreak()) return;
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        if (nowMs >= nextLookAtMs) {
            nextLookAtMs = nowMs + 1500L + (long) (rng.nextDouble() * 3500d);
            float curYaw = p.getYRot();
            float curPitch = p.getXRot();
            float targetYaw = curYaw + (rng.nextFloat() - 0.5f) * 80f;
            float targetPitch = Math.max(-45f, Math.min(50f, curPitch + (rng.nextFloat() - 0.5f) * 20f));
            ZenithEyes.getInstance().setEnabled(true);
            ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                    .yaw(targetYaw).pitch(targetPitch)
                    .priority(RotationRequest.Priority.BACKGROUND)
                    .durationMs(1200L + (long) (rng.nextDouble() * 1200))
                    .profile("legit")
                    .tag("idle-break")
                    .build());
        }
    }
}
