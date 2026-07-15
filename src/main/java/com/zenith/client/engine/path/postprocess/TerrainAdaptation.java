package com.zenith.client.engine.path.postprocess;

/**
 * Adjusts waypoint y coordinates to match terrain (half slabs, stairs, farmland,
 * paths) so the player lands on the correct surface instead of snapping to integer
 * block y. Phase 6 fills in real collision height queries.
 */
public final class TerrainAdaptation {

    /** @return adjusted y for a waypoint above the given block, or y unchanged. */
    public double adjustedY(double x, double y, double z) {
        // Phase 6: sample BlockState collision shape height.
        return Math.floor(y);
    }
}
