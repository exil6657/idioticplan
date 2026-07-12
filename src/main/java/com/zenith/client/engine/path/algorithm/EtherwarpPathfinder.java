package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;
import com.zenith.client.engine.path.PathResult.Status;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Etherwarp pathfinder: builds a path that is mostly walking but may include
 * teleport jumps up to 57 blocks away (Enchanted Etherwarp Transmission maximum
 * with max transmission tuning) through air.
 *
 * <p>Strategy: BFS over "walk segments" connected by etherwarp jumps. For each
 * reachable node we try an etherwarp to any LOS-clear block within 61 blocks.
 * Because etherwarp is expensive in-game (cooldown, mana), we prefer paths
 * with fewer teleports.</p>
 */
public final class EtherwarpPathfinder {

    private static final int MAX_TELEPORT = 57;
    private static final double TELEPORT_COST = 25d; // discourage teleports

    private final WalkabilityChecker walkability = new WalkabilityChecker();
    private final AStarPathfinder walk = new AStarPathfinder();

    public PathResult compute(PathRequest req) {
        BlockPos startPos = BlockPos.containing(req.fromX, req.fromY, req.fromZ);
        BlockPos goalPos  = BlockPos.containing(req.toX, req.toY, req.toZ);

        // Phase 6 LOS (raycast) check for direct etherwarp from start to goal.
        // If direct-LOS and within range, take it immediately.
        if (canTeleport(startPos, goalPos)) {
            PathNode s = new PathNode(startPos);
            PathNode g = new PathNode(goalPos);
            s.parent = null; g.parent = s; g.stepType = PathNode.StepType.ETHERWARP;
            return new PathResult(Status.FOUND, List.of(s, g), TELEPORT_COST, 0, goalPos);
        }

        // Fallback: delegate to normal A* if no etherwarp shortcuts available in this stub
        return walk.compute(req);
    }

    /**
     * @return true if there is line-of-sight from 'from' to 'to' within range and
     *         the destination is a solid standable block.
     * Phase 6 will add a real VoxelStream raycast; stub uses simple bounds.
     */
    private boolean canTeleport(BlockPos from, BlockPos to) {
        if (from.dist(to) > MAX_TELEPORT) return false;
        // Real LOS check implemented in Phase 6 world adapter; for now, range check only.
        return true;
    }
}
