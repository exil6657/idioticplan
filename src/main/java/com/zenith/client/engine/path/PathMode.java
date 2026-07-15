package com.zenith.client.engine.path;

/** Locomotion mode for the pathfinder. */
public enum PathMode {
    /** On-foot walking; standard walkability checks. */
    WALK,
    /** Short-range teleport via Etherwarp (Aspect of the Void / Etherwarp conduit). */
    ETHERWARP,
    /** Flight mode (for creative-style flight in certain situations — NOT on Hypixel, kept for debug). */
    FLY,
    /** Pure rotation-only walk (rotate on the spot — used by no-movement interactions). */
    ROTATE_ONLY;

    public boolean teleports() { return this == ETHERWARP; }
}
