package com.zenith.client.engine.eyes.behavior;

import com.zenith.client.core.timer.Timer;
import com.zenith.client.engine.eyes.RotationRequest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * While the player is walking/running and no other rotation is active, injects
 * small random glances ±20° off-path for short periods, simulating looking
 * around while walking rather than staring dead-ahead like a robot.
 */
public class WalkingGlance {

    private final Timer glanceTimer = new Timer();
    private boolean glancing;
    private long glanceEndAt;

    public RotationRequest maybeGlance(float currentYaw, float movementYaw, boolean moving, long nowMs) {
        if (!moving) { glancing = false; return null; }
        if (glancing) {
            if (nowMs >= glanceEndAt) {
                // Glance back toward movement heading
                glancing = false;
                return RotationRequest.builder()
                        .yaw(movementYaw)
                        .pitch(0f)
                        .priority(RotationRequest.Priority.WANDER)
                        .durationMs(600 + ThreadLocalRandom.current().nextLong(400))
                        .profile("smooth")
                        .tag("glanceReturn")
                        .preemptible(true)
                        .build();
            }
            return null;
        }
        if (glanceTimer.hasElapsed(3500 + ThreadLocalRandom.current().nextLong(5000))) {
            glanceTimer.reset();
            if (ThreadLocalRandom.current().nextFloat() < 0.55f) {
                glancing = true;
                glanceEndAt = nowMs + 700 + ThreadLocalRandom.current().nextLong(900);
                float side = ThreadLocalRandom.current().nextBoolean() ? 1f : -1f;
                float targetYaw = currentYaw + side * (10f + ThreadLocalRandom.current().nextFloat() * 20f);
                return RotationRequest.builder()
                        .yaw(targetYaw)
                        .pitch(-5f + ThreadLocalRandom.current().nextFloat() * 10f)
                        .priority(RotationRequest.Priority.WANDER)
                        .durationMs(500 + ThreadLocalRandom.current().nextLong(400))
                        .profile("legit")
                        .tag("walkingGlance")
                        .preemptible(true)
                        .build();
            }
        }
        return null;
    }
}
