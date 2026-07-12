package com.zenith.client.engine.eyes;

/** Named rotation style — selects curve + humanizer parameters. */
public enum RotationProfileType {

    /** Fast snappy turn (combat, high-alert failsafe). */
    SNAPPY,
    /** Smooth cinematic arc (mining, farming). */
    SMOOTH,
    /** Legit-looking casual mouse movement (idle, pathing). */
    LEGIT,
    /** Human-recorded profile loaded from disk (movement learner). */
    RECORDED,
    /** Silent/none — used when ZenithEyes is disabled or bypassed. */
    SILENT
}
