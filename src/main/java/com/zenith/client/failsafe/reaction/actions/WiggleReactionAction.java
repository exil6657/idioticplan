package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;

import java.util.Random;

/**
 * Plays a short human-looking camera wiggle ("oops, did I look that way?")
 * for ~700–1200 ms after a rotation snapback, player-nearby glance, or
 * wrong-item deselect. Looks like a real player resetting their view.
 *
 * <p>Uses {@link ZenithEyes#requestRotation} exclusively (master rule §2 — no
 * instantaneous rotations). After the wiggle the macro's normal rotation
 * profile is re-enabled by the caller.</p>
 */
public final class WiggleReactionAction {

    private static final Random RNG = new Random();

    private static boolean active;
    private static long startedAt;
    private static long durationMs;
    private static int step;
    private static int totalSteps;
    private static float baseYaw, basePitch;

    public static void trigger() {
        if (active) return;
        active = true;
        startedAt = System.currentTimeMillis();
        durationMs = 700L + RNG.nextInt(500);
        step = 0;
        totalSteps = 4;
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            baseYaw = mc.player.getYRot();
            basePitch = mc.player.getXRot();
        }
        ZenithClient.LOGGER.debug("[Failsafe] wiggle reaction started ({} ms)", durationMs);
    }

    public static boolean isActive() { return active; }

    public static void cancel() { active = false; }

    public static void tick() {
        if (!active) return;
        long now = System.currentTimeMillis();
        if (now - startedAt > durationMs) {
            active = false;
            return;
        }
        int shouldBeAtStep = (int) (((now - startedAt) * totalSteps) / durationMs);
        while (step <= shouldBeAtStep && step < totalSteps) {
            // Small ±(8-20)° yaw jitter with mild pitch wobble, short duration, preemptible.
            float yawOff = (RNG.nextFloat() - 0.5f) * 40f;
            float pitchOff = (RNG.nextFloat() - 0.5f) * 14f;
            ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                    .yaw(baseYaw + yawOff)
                    .pitch(Math.max(-90, Math.min(90, basePitch + pitchOff)))
                    .durationMs(180L + RNG.nextInt(120))
                    .priority(RotationRequest.Priority.FAILSAFE)
                    .preemptible(true)
                    .profile("failsafe-wiggle")
                    .tag("failsafe:wiggle")
                    .build());
            step++;
        }
    }
}
