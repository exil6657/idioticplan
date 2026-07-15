package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.BlockPos;

/**
 * Ensures post-processed waypoints are pushed away from walls (so the player
 * doesn't scrape along a wall — real humans leave a 0.1-0.3 block gap).
 *
 * <p>Phase 2: API surface; actual collision queries added in Phase 6 world layer.</p>
 */
public final class WallAwareness {

    private float offsetFromWall = 0.18f;

    public void setOffset(float v) { this.offsetFromWall = Math.max(0f, Math.min(0.5f, v)); }
    public float getOffset() { return offsetFromWall; }

    /** Adjust a waypoint position away from nearby walls. Returns adjusted [x,y,z]. */
    public double[] adjust(double x, double y, double z) {
        // Phase 6: look at solid blocks around the player AABB and push inward.
        return new double[]{x, y, z};
    }
}
