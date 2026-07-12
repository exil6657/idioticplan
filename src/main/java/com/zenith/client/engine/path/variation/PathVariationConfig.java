package com.zenith.client.engine.path.variation;

/** Tunable knobs for path variation. */
public class PathVariationConfig {
    /** Probability (0..1) of taking an alternate route on each compute. */
    public float alternateRouteChance = 0.15f;
    /** Gaussian deviation added to waypoints (blocks). */
    public float waypointJitterSigma = 0.08f;
    /** Number of historical routes to keep per (from, to) pair. */
    public int historySize = 8;
}
