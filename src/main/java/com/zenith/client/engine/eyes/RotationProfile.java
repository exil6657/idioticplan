package com.zenith.client.engine.eyes;

/**
 * A bundle of curve/humanizer parameters that defines a rotation style.
 *
 * <p>Profiles are loaded from {@code data/rotation_profiles/*.json} (Phase 11)
 * or constructed programmatically. All parameters are in sensible physical
 * units (degrees, ms, Hz).</p>
 */
public class RotationProfile {

    public final String id;
    public final RotationProfileType type;

    // --- Curve ---
    /** Base duration for a 90° turn; other distances scale from this. */
    public float base90Ms = 180f;
    /** Min/max turn time caps (so tiny flicks aren't instant, huge sweeps aren't impossibly slow). */
    public float minTurnMs = 60f;
    public float maxTurnMs = 1200f;
    /** Bezier control point offsets as fractions of angle distance. */
    public float cp1x = 0.25f, cp1y = 0.10f, cp2x = 0.75f, cp2y = 0.95f;
    /** Amount of cubic smoothing applied on top of the bezier. 0 = pure bezier. */
    public float cubicBlend = 0.35f;

    // --- Humanizer ---
    /** Stddev of micro-jitter injected at tick quantisation (degrees). */
    public float jitterSigma = 0.08f;
    /** Reaction/acceleration model params. */
    public float accelK = 0.18f;          // acceleration gain (higher = snappier)
    public float overshootMax = 2.5f;     // max overshoot in degrees
    public float overshootChance = 0.35f; // probability of overshoot occurring
    public float microCorrectGain = 0.4f; // gain of post-overshoot micro-correction
    /** Hesitation probability per 100ms (probability curve pauses briefly mid-turn). */
    public float hesitationChance = 0.08f;
    public float hesitationMaxMs = 120f;
    /** Gaze-history aware smoothness (higher = smoother but slower). */
    public float historySmooth = 0.3f;
    /** Fatigue multiplier applied as session length grows. */
    public float fatigueMax = 1.4f;

    // --- Tick quantisation ---
    /** Output is quantised to this rate (Hz). 20 matches MC tick; 60 matches render FPS. */
    public int outputHz = 120;

    // --- Behaviour ---
    /** If true, inject idle micro-wanders during straight-line pathing. */
    public boolean idleWander = true;
    public boolean walkingGlance = true;
    public boolean environmentAware = true;

    public RotationProfile(String id, RotationProfileType type) {
        this.id = id;
        this.type = type;
    }

    /** Duration (ms) for a given angular distance (degrees), log-scaled. */
    public float durationForAngle(float degrees) {
        float ratio = Math.abs(degrees) / 90f;
        if (ratio < 0.05f) ratio = 0.05f;
        float ms = base90Ms * (0.35f + 0.65f * (float) Math.pow(ratio, 0.7));
        return Math.max(minTurnMs, Math.min(maxTurnMs, ms));
    }

    // ---- Preset factory methods -----------------------------------------

    public static RotationProfile smooth() {
        RotationProfile p = new RotationProfile("smooth", RotationProfileType.SMOOTH);
        p.base90Ms = 260f; p.jitterSigma = 0.04f; p.overshootChance = 0.12f;
        p.accelK = 0.13f; p.hesitationChance = 0.03f; return p;
    }

    public static RotationProfile snappy() {
        RotationProfile p = new RotationProfile("snappy", RotationProfileType.SNAPPY);
        p.base90Ms = 110f; p.jitterSigma = 0.12f; p.overshootChance = 0.55f;
        p.overshootMax = 4f; p.accelK = 0.25f; p.hesitationChance = 0.02f; return p;
    }

    public static RotationProfile legit() {
        RotationProfile p = new RotationProfile("legit", RotationProfileType.LEGIT);
        p.base90Ms = 220f; p.jitterSigma = 0.09f; p.overshootChance = 0.35f;
        p.accelK = 0.16f; p.hesitationChance = 0.10f; p.walkingGlance = true;
        p.fatigueMax = 1.5f; return p;
    }

    public static RotationProfile silent() {
        RotationProfile p = new RotationProfile("silent", RotationProfileType.SILENT);
        p.base90Ms = 0f; p.minTurnMs = 0f; p.maxTurnMs = 0f; p.jitterSigma = 0f;
        p.overshootChance = 0f; p.hesitationChance = 0f; return p;
    }
}
