package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;

/** Heuristic functions for A*. All return a lower bound on cost (admissible). */
public final class PathHeuristic {

    private PathHeuristic() {}

    /** Octile distance in 3D: accurate for grid movement with diagonals. */
    public static double octile(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        int dz = Math.abs(a.z - b.z);
        double d13 = Math.sqrt(2) - 1;     // 2D diagonal cost
        double d23 = Math.sqrt(3) - Math.sqrt(2); // 3D diagonal cost
        double max = Math.max(dx, dz);
        double min = Math.min(dx, dz);
        double horiz = (max + d13 * min) + d23 * Math.min(max, dy);
        double vert  = Math.abs(dy);
        return horiz + vert;
    }

    /** Manhattan (used for flying pathfinder). */
    public static double manhattan(BlockPos a, BlockPos b) {
        return a.distManhattan(b);
    }

    /** Euclidean (underestimate for most grids but admissible for teleporting). */
    public static double euclidean(BlockPos a, BlockPos b) {
        return a.dist(b);
    }
}
