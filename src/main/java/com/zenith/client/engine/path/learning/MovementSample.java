package com.zenith.client.engine.path.learning;

/**
 * A single recorded sample of player movement at one point in time. Captured
 * every tick while the {@link MovementRecorder} is active and the player is
 * moving naturally (i.e. not macroing). Used to build a {@link MovementProfile}
 * that mimics the user's style.
 */
public final class MovementSample {

    /** Timestamp in milliseconds. */
    public final long t;
    /** Player position. */
    public final double x, y, z;
    /** Player yaw/pitch in degrees. */
    public final float yaw, pitch;
    /** Inputs pressed (0..1). */
    public final float forward, strafe;
    public final boolean jump, sprint, sneak;
    /** Speed (blocks/sec) this sample. */
    public final double speed;
    /** Angular velocity (deg/sec) this sample. */
    public final float yawVel;
    /** Delta time since previous sample (ms). */
    public final float dtMs;
    /** On-ground flag. */
    public final boolean onGround;

    public MovementSample(long t, double x, double y, double z, float yaw, float pitch,
                          float forward, float strafe, boolean jump, boolean sprint, boolean sneak,
                          double speed, float yawVel, float dtMs, boolean onGround) {
        this.t = t; this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.forward = forward; this.strafe = strafe;
        this.jump = jump; this.sprint = sprint; this.sneak = sneak;
        this.speed = speed; this.yawVel = yawVel; this.dtMs = dtMs;
        this.onGround = onGround;
    }
}
