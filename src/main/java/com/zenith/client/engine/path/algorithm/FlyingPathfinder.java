package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;
import com.zenith.client.engine.path.PathResult.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Straight-line "flying" path used only in debug mode (never on Hypixel) —
 * generates evenly-spaced waypoints directly from start to goal 1 block apart
 * without walkability checks. Useful for the route editor / debug camera.
 */
public final class FlyingPathfinder {

    public PathResult compute(PathRequest req) {
        BlockPos from = BlockPos.containing(req.fromX, req.fromY, req.fromZ);
        BlockPos to   = BlockPos.containing(req.toX, req.toY, req.toZ);
        List<PathNode> nodes = new ArrayList<>();
        double dist = Math.sqrt(from.distSq(to));
        int steps = Math.max(1, (int) Math.ceil(dist));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(req.fromX + (req.toX - req.fromX) * t);
            int y = (int) Math.round(req.fromY + (req.toY - req.fromY) * t);
            int z = (int) Math.round(req.fromZ + (req.toZ - req.fromZ) * t);
            PathNode n = new PathNode(new BlockPos(x,y,z));
            n.stepType = PathNode.StepType.WALK;
            if (!nodes.isEmpty()) n.parent = nodes.get(nodes.size()-1);
            nodes.add(n);
        }
        return new PathResult(Status.FOUND, nodes, dist, 0, to);
    }
}
