package com.zenith.client.engine.eyes.behavior;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.eyes.RotationRequest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Camera wander is a soft sine/cosine-based yaw/pitch offset applied when
 * ZenithEyes has no active request (idle). The amplitude is small (±1.5°) and
 * the period varies slowly so it looks like idle breathing/micro-movement, not
 * a perfect sine.
 */
public class CameraWander {

    private double phaseYaw, phasePitch;
    private double periodYawMs, periodPitchMs;
    private double ampYaw, ampPitch;
    private long lastRoll;

    public CameraWander() { rollParams(); }

    private void rollParams() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        phaseYaw = r.nextDouble(Math.PI * 2);
        phasePitch = r.nextDouble(Math.PI * 2);
        periodYawMs = 3500 + r.nextDouble(4000);
        periodPitchMs = 4200 + r.nextDouble(5000);
        ampYaw = 0.8 + r.nextDouble(0.8);
        ampPitch = 0.4 + r.nextDouble(0.5);
        lastRoll = System.currentTimeMillis();
    }

    /** @return [yawOffset, pitchOffset] in degrees to apply this frame. */
    public float[] sample(long nowMs) {
        if (nowMs - lastRoll > 30_000) rollParams();
        double t = nowMs;
        float yOff = (float) (Math.sin(t * Math.PI * 2 / periodYawMs + phaseYaw) * ampYaw);
        float pOff = (float) (Math.cos(t * Math.PI * 2 / periodPitchMs + phasePitch) * ampPitch);
        return new float[]{yOff, pOff};
    }
}
