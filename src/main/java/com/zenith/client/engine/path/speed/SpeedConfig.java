package com.zenith.client.engine.path.speed;

/** Speed constants and configuration. */
public final class SpeedConfig {
    /** Base walk speed in blocks/s (MC default is 4.317 bps). */
    public static final double BASE_WALK_BPS = 4.317;
    /** Sprint multiplier (1.3x base). */
    public static final double SPRINT_MULT = 1.3d;
    /** Speed potion per level multiplier. */
    public static final double SPEED_POT_PER_LEVEL = 0.2d;
    /** Rancher boots multiplier (when on garden). */
    public static final double RANCHER_MULT = 2.0d;
    private SpeedConfig() {}
}
