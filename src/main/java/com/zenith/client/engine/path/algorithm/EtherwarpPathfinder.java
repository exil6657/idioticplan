package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;
import com.zenith.client.engine.path.PathResult.Status;
import com.zenith.client.world.World;

import java.util.List;

/**
 * Etherwarp pathfinder: computes a path that may include a single direct
 * Etherwarp transmission up to 57 blocks (max Enchanted Etherwarp Transmission
 * tuning distance), falling back to walking when no warp shortcut is viable.
 *
 * <p>Warping requires line-of-sight, a standable destination block, and no solid
 * blocks in the way (checked via {@link com.zenith.client.world.WorldAdapter#raycastClear}).</p>
 */
public final class EtherwarpPathfinder {

    /** Maximum Etherwarp range (blocks). */
    public static final int MAX_TELEPORT = 57;
    private static final double TELEPORT_COST = 25d;

    private final WalkabilityChecker walkability = new WalkabilityChecker();
    private final AStarPathfinder walk = new AStarPathfinder();

    public PathResult compute(PathRequest req) {
        BlockPos startPos = BlockPos.containing(req.fromX, req.fromY, req.fromZ);
        BlockPos goalPos  = BlockPos.containing(req.toX, req.toY, req.toZ);

        // Direct etherwarp shortcut.
        if (canTeleport(req.fromX, req.fromY + 1.62, req.fromZ, req.toX, req.toY + 0.2, req.toZ)) {
            PathNode s = new PathNode(startPos);
            PathNode g = new PathNode(goalPos);
            g.parent = s; g.stepType = PathNode.StepType.ETHERWARP;
            return new PathResult(Status.FOUND, List.of(s, g), TELEPORT_COST, 0, goalPos);
        }
        return walk.compute(req);
    }

    /** Check LOS from eyes (y+1.62) to destination feet (y+0.2). */
    private boolean canTeleport(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dist = Math.hypot(Math.hypot(x2-x1, z2-z1), y2-y1);
        if (dist > MAX_TELEPORT) return false;
        try { return World.get().raycastClear(x1, y1, z1, x2, y2, z2); }
        catch (Throwable t) { return false; }
    }
}
