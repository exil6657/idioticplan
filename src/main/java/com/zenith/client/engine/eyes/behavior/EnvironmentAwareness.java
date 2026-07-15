package com.zenith.client.engine.eyes.behavior;

import com.zenith.client.engine.eyes.RotationRequest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tracks "interesting" entities/positions nearby (players, mobs, dropped rare
 * items) and occasionally fires a quick glance rotation toward them. This
 * mimics a real player noticing something out of the corner of their eye.
 *
 * <p>Phase 2: stub — actual entity scanning is wired in Phase 6 (World subsystem).
 * For now, the API is in place so later phases can push {@link InterestPoint}s.</p>
 */
public class EnvironmentAwareness {

    public record InterestPoint(float yaw, float pitch, double distance, String tag) {}

    private InterestPoint lastPoint;
    private long nextEligibleAt;

    public void pushInterest(InterestPoint p) { this.lastPoint = p; }

    public RotationRequest maybeLook(long nowMs) {
        if (lastPoint == null || nowMs < nextEligibleAt) return null;
        if (lastPoint.distance() > 18) return null;
        // 15% chance of glancing each 2s window after a push.
        if (ThreadLocalRandom.current().nextFloat() > 0.15f) return null;
        nextEligibleAt = nowMs + 2500 + ThreadLocalRandom.current().nextLong(3500);
        return RotationRequest.builder()
                .yaw(lastPoint.yaw()).pitch(lastPoint.pitch())
                .priority(RotationRequest.Priority.WANDER)
                .durationMs(350 + ThreadLocalRandom.current().nextLong(300))
                .profile("snappy")
                .tag("env:" + lastPoint.tag())
                .preemptible(true)
                .build();
    }
}
