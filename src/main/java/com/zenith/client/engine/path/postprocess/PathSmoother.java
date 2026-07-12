package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.algorithm.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a modified Chaikin/forward-line-of-sight smoother to collapse redundant
 * A* waypoints into longer straight-line segments (so movement doesn't look
 * blocky). We don't over-smooth — real paths still have slight kinks.
 */
public final class PathSmoother {

    public List<PathNode> smooth(List<PathNode> input, SmoothPassable passable) {
        if (input.size() <= 2) return new ArrayList<>(input);
        List<PathNode> out = new ArrayList<>();
        out.add(input.get(0));
        int i = 0;
        while (i < input.size() - 1) {
            int farthest = i + 1;
            for (int j = input.size() - 1; j > i; j--) {
                if (lineClear(input.get(i), input.get(j), passable)) { farthest = j; break; }
            }
            out.add(input.get(farthest));
            i = farthest;
        }
        return out;
    }

    private boolean lineClear(PathNode a, PathNode b, SmoothPassable p) {
        // Bresenham for 3D line, checking every block along the player's bounding box.
        int x1 = a.pos.x, y1 = a.pos.y, z1 = a.pos.z;
        int x2 = b.pos.x, y2 = b.pos.y, z2 = b.pos.z;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;
        int x = x1, y = y1, z = z1;
        int xD = dy + dz, yD = dx + dz, zD = dx + dy;
        int steps = dx + dy + dz;
        for (int s = 0; s <= steps; s++) {
            if (!p.passable(x, y, z)) return false;
            if ((xD << 1) >= -dx - dy - dz + xD) { xD -= dy + dz; x += sx; }
            if ((yD << 1) >= -dx - dy - dz + yD) { yD -= dx + dz; y += sy; }
            if ((zD << 1) >= -dx - dy - dz + zD) { zD -= dx + dy; z += sz; }
        }
        return true;
    }

    @FunctionalInterface
    public interface SmoothPassable {
        boolean passable(int x, int y, int z);
    }
}
