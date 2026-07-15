package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates all candidate jumps that MC allows:
 * <ul>
 *   <li>Horizontal 1-block (flat walk)</li>
 *   <li>1-up jumps (parkour gap up to 4 blocks, etc.)</li>
 *   <li>Drop descents</li>
 *   <li>Diagonal movement (45° strafes)</li>
 * </ul>
 *
 * <p>All MC jump distances are precomputed as (dx, dy, dz, cost) tuples — a
 * dynamic-programming approach inspired by Baritone. The candidates returned
 * are the superset; {@link WalkabilityChecker} filters them per-world.</p>
 */
public final class JumpChecker {

    /** A motion primitive: offset and cost multiplier. */
    public record Jump(int dx, int dy, int dz, double cost, boolean sprint) {}

    private static final List<Jump> JUMPS = build();

    private JumpChecker() {}

    public static List<Jump> jumps() { return JUMPS; }

    private static List<Jump> build() {
        List<Jump> js = new ArrayList<>();
        // Cardinal & diagonal flat moves (1 block).
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                double cost = (dx != 0 && dz != 0) ? Math.sqrt(2) : 1.0;
                js.add(new Jump(dx, 0, dz, cost, false));
                js.add(new Jump(dx, 1, dz, cost + 0.7, true)); // jump up
                // Small drops.
                for (int drop = 1; drop <= 4; drop++) {
                    js.add(new Jump(dx, -drop, dz, cost + 0.2 * drop, false));
                }
            }
        }
        // 2-4 block parkour sprints (forward jumps while sprinting).
        for (int dist = 2; dist <= 4; dist++) {
            js.add(new Jump(dist, 0, 0, dist * 0.95d, true));
            js.add(new Jump(-dist, 0, 0, dist * 0.95d, true));
            js.add(new Jump(0, 0, dist, dist * 0.95d, true));
            js.add(new Jump(0, 0, -dist, dist * 0.95d, true));
        }
        return List.copyOf(js);
    }

    public static List<BlockPos> expand(BlockPos from, WalkabilityChecker wc) {
        List<BlockPos> out = new ArrayList<>();
        for (Jump j : JUMPS) {
            BlockPos target = from.add(j.dx, j.dy, j.dz);
            if (j.dy > 0) {
                if (!wc.canJumpTo(from, target)) continue;
            } else if (j.dy < 0) {
                if (!wc.canDropTo(from, target)) continue;
            } else {
                if (!wc.canWalkInto(target)) continue;
            }
            out.add(target);
        }
        return out;
    }
}
