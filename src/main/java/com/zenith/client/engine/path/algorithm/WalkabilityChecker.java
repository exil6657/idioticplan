package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;

/**
 * Walkability oracle — answers whether a block is solid, passable, a ladder,
 * etc. The real implementation reads from the client world via the world
 * adapter installed in Phase 6; a stub is provided now so the pathfinder can
 * be unit-tested against a simple boolean[][][] map via {@link #setTestMap}.
 */
public final class WalkabilityChecker {

    private WorldView view = DEFAULT_VIEW;

    public interface WorldView {
        /** @return true if the block at p is fully solid (can stand on). */
        boolean isSolid(BlockPos p);
        /** @return true if the block is entirely air / passable. */
        boolean isPassable(BlockPos p);
        /** @return true if the block is a climbable (ladder/vine/trapdoor climbable surface). */
        boolean isClimbable(BlockPos p);
        /** @return true if the block is water. */
        boolean isWater(BlockPos p);
        /** @return true if the block is lava. */
        boolean isLava(BlockPos p);
    }

    public void setWorldView(WorldView v) { this.view = v == null ? DEFAULT_VIEW : v; }

    /** Can we stand at (pos) with feet at pos and head at pos+1? */
    public boolean canStandAt(BlockPos pos) {
        return view.isPassable(pos)
            && view.isPassable(pos.add(0,1,0))
            && view.isSolid(pos.add(0,-1,0))
            && !view.isLava(pos)
            && !view.isLava(pos.add(0,-1,0));
    }

    /** Can we walk horizontally into pos (requires pos and pos+1 passable and floor solid)? */
    public boolean canWalkInto(BlockPos pos) {
        return canStandAt(pos);
    }

    /** Can we jump from 'from' to 'to' (up 1 or same level with up boost)? */
    public boolean canJumpTo(BlockPos from, BlockPos to) {
        // MC jump gives +1.25y clearance; we allow 1 block up.
        if (to.y - from.y > 1) return false;
        return canStandAt(to);
    }

    /** Can we drop down to 'to' (to.y <= from.y, no lava in between)? */
    public boolean canDropTo(BlockPos from, BlockPos to) {
        if (to.y >= from.y) return false;
        if (to.y - from.y < -6) return false; // avoid big drops
        for (int dy = 0; dy >= to.y - from.y; dy--) {
            BlockPos p = from.add(0, dy, 0);
            if (!view.isPassable(p)) return false;
            if (view.isLava(p)) return false;
        }
        return view.isSolid(to.add(0,-1,0));
    }

    private static final WorldView DEFAULT_VIEW = new WorldView() {
        @Override public boolean isSolid(BlockPos p) { return false; }
        @Override public boolean isPassable(BlockPos p) { return true; }
        @Override public boolean isClimbable(BlockPos p) { return false; }
        @Override public boolean isWater(BlockPos p) { return false; }
        @Override public boolean isLava(BlockPos p) { return false; }
    };
}
