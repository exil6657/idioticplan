package com.zenith.client.engine.eyes;

/** Snapshot of the eyes engine state for the Brain View debug HUD. */
public record RotationDebugData(
        String state,
        String activeTag,
        String profileId,
        float targetYaw,
        float targetPitch,
        float currentYaw,
        float currentPitch,
        float angularVelocityDegPerSec,
        float progress,
        int queued,
        boolean overshooting,
        boolean correcting,
        boolean hesitating,
        float fatigue,
        String priority
) {}
