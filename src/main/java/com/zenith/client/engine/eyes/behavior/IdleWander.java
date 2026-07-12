package com.zenith.client.engine.eyes.behavior;

import com.zenith.client.core.timer.RandomDelay;
import com.zenith.client.core.timer.Timer;
import com.zenith.client.engine.eyes.RotationRequest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * When idle (no macro, no combat, standing still), performs large, slow
 * gaze shifts: looks at a random nearby yaw/pitch, pauses, slowly shifts again.
 * Simulates a bored player glancing around.
 */
public class IdleWander {

    private final Timer nextShiftTimer = new Timer();
    private boolean shifting = false;
    private float yawCenter, pitchCenter;

    public IdleWander() {
        nextShiftTimer.reset();
    }

    public void setCenter(float yaw, float pitch) {
        this.yawCenter = yaw;
        this.pitchCenter = pitch;
    }

    /**
     * @param currentYaw   player's current yaw
     * @param currentPitch player's current pitch
     * @param idleForMs    how long the player has been idle
     * @return a new RotationRequest if it's time to shift; null otherwise.
     */
    public RotationRequest maybeRequest(float currentYaw, float currentPitch, long idleForMs) {
        if (idleForMs < 4_000) return null;
        long delayMs = RandomDelay.gaussian(8000, 3000);
        if (shifting) return null;
        if (nextShiftTimer.hasElapsed(delayMs)) {
            nextShiftTimer.reset();
            ThreadLocalRandom r = ThreadLocalRandom.current();
            float targetYaw = yawCenter + (r.nextFloat() - 0.5f) * 60f;
            float targetPitch = Math.max(-85f, Math.min(85f, currentPitch + (r.nextFloat() - 0.5f) * 20f));
            long duration = 700 + r.nextLong(900);
            return RotationRequest.builder()
                    .yaw(targetYaw).pitch(targetPitch)
                    .priority(RotationRequest.Priority.WANDER)
                    .durationMs(duration)
                    .profile("legit")
                    .tag("idleWander")
                    .preemptible(true)
                    .build();
        }
        return null;
    }
}
