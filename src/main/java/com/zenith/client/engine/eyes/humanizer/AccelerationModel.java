package com.zenith.client.engine.eyes.humanizer;

/**
 * Models human-like angular acceleration / deceleration.
 *
 * <p>Real mouse movement starts slow (hand overcoming inertia), peaks mid-turn,
 * and decelerates as the target is approached (fitts's law). This model applies
 * a gain factor to the instantaneous desired velocity based on distance
 * remaining and current speed.</p>
 */
public final class AccelerationModel {

    private float velYaw = 0f, velPitch = 0f;

    /**
     * @param desiredYaw    desired yaw step in degrees for this tick
     * @param desiredPitch  desired pitch step in degrees for this tick
     * @param errorYaw      remaining yaw distance to target (degrees)
     * @param errorPitch    remaining pitch distance to target (degrees)
     * @param accelGain     profile acceleration constant (~0.15 normal, ~0.25 snappy)
     * @param dtMs          delta time in ms
     */
    public float[] apply(float desiredYaw, float desiredPitch,
                         float errorYaw, float errorPitch, float accelGain, float dtMs) {
        float dt = dtMs / 1000f;
        float maxAccel = accelGain * 180f; // deg/s²
        // Deceleration: stronger when close to target
        float decelGain = accelGain * 2.5f;
        velYaw   = approach(velYaw,   desiredYaw / dt,   maxAccel, decelGain, Math.abs(errorYaw),   dt);
        velPitch = approach(velPitch, desiredPitch / dt, maxAccel, decelGain, Math.abs(errorPitch), dt);
        return new float[]{ velYaw * dt, velPitch * dt };
    }

    public void reset() { velYaw = 0f; velPitch = 0f; }

    private float approach(float current, float target, float maxAccel, float decelGain, float error, float dt) {
        float diff = target - current;
        float limit;
        if (Math.abs(target) > Math.abs(current) && error > 5f) {
            limit = maxAccel * dt;
        } else {
            // decelerate proportionally to remaining error (prevents overshoot)
            limit = Math.max(maxAccel * dt, Math.abs(diff)) * decelGain * dt;
        }
        if (Math.abs(diff) < limit) return target;
        return current + Math.copySign(limit, diff);
    }

    public float currentYawVel() { return velYaw; }
    public float currentPitchVel() { return velPitch; }
}
