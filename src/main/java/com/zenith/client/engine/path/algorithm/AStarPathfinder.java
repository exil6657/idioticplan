package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;
import com.zenith.client.engine.path.PathResult.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A* pathfinder on a block grid.
 *
 * <p>Supports:
 * <ul>
 *   <li>Walk/jump/drop primitives from {@link JumpChecker}</li>
 *   <li>Octile heuristic</li>
 *   <li>Timeout-bounded search (max compute ms per request)</li>
 *   <li>Open/closed sets implemented with a min-heap and HashMap</li>
 *   <li>Partial results: if search exhausts, returns the best node we found
 *       so the executor can keep walking in a useful direction</li>
 * </ul>
 *
 * <p>Intentionally single-threaded and garbage-light (per-request allocations
 * only). Called from the macro/tick thread.</p>
 */
public final class AStarPathfinder {

    private final WalkabilityChecker walkability = new WalkabilityChecker();

    public WalkabilityChecker getWalkability() { return walkability; }

    public PathResult compute(PathRequest req) {
        long start = System.nanoTime();
        long maxNs = req.maxComputeMs * 1_000_000L;

        BlockPos startPos = BlockPos.containing(req.fromX, req.fromY, req.fromZ);
        BlockPos goalPos  = BlockPos.containing(req.toX, req.toY, req.toZ);

        if (startPos.equals(goalPos)) {
            return new PathResult(Status.FOUND, List.of(new PathNode(startPos)), 0, 0, startPos);
        }

        PriorityQueue<PathNode> open = new PriorityQueue<>((a,b) -> Double.compare(a.f, b.f));
        Map<BlockPos, PathNode> allNodes = new HashMap<>(4096);
        java.util.Set<BlockPos> closed = new java.util.HashSet<>(4096);

        PathNode startNode = new PathNode(startPos);
        startNode.g = 0;
        startNode.h = PathHeuristic.octile(startPos, goalPos);
        startNode.f = startNode.g + startNode.h;
        open.add(startNode);
        allNodes.put(startPos, startNode);

        PathNode best = startNode;

        while (!open.isEmpty()) {
            if ((System.nanoTime() - start) > maxNs) {
                List<PathNode> path = reconstruct(best);
                return new PathResult(Status.TIMEOUT, path, best.g, (System.nanoTime()-start)/1_000_000L, best.pos);
            }
            PathNode cur = open.poll();
            if (cur == null) break;
            if (closed.contains(cur.pos)) continue;
            closed.add(cur.pos);
            if (cur.pos.dist(goalPos) < best.pos.dist(goalPos)) best = cur;

            if (cur.pos.dist(goalPos) <= req.stopDistance + 0.5d) {
                List<PathNode> path = reconstruct(cur);
                return new PathResult(Status.FOUND, path, cur.g, (System.nanoTime()-start)/1_000_000L, cur.pos);
            }

            for (BlockPos next : JumpChecker.expand(cur.pos, walkability)) {
                if (closed.contains(next)) continue;
                double stepCost = stepCost(cur.pos, next);
                double tentativeG = cur.g + stepCost;
                PathNode existing = allNodes.get(next);
                if (existing == null || tentativeG < existing.g) {
                    PathNode n = existing != null ? existing : new PathNode(next);
                    n.parent = cur;
                    n.g = tentativeG;
                    n.h = PathHeuristic.octile(next, goalPos);
                    n.f = n.g + n.h;
                    if (next.y > cur.pos.y) n.stepType = PathNode.StepType.JUMP;
                    else if (next.y < cur.pos.y) n.stepType = PathNode.StepType.DROP;
                    else n.stepType = PathNode.StepType.WALK;
                    if (existing == null) {
                        allNodes.put(next, n);
                        open.add(n);
                    }
                }
            }
        }

        List<PathNode> partial = best != startNode ? reconstruct(best) : List.of();
        Status st = partial.isEmpty() ? Status.UNREACHABLE : Status.PARTIAL;
        return new PathResult(st, partial, best.g, (System.nanoTime()-start)/1_000_000L, best.pos);
    }

    private double stepCost(BlockPos from, BlockPos to) {
        double horiz = Math.hypot(to.x - from.x, to.z - from.z);
        double vert = Math.abs(to.y - from.y);
        double cost = horiz + vert * 1.5;
        // Penalty for drops (falls look suspicious).
        if (to.y < from.y) cost += (from.y - to.y) * 0.4;
        if (to.y > from.y) cost += (to.y - from.y) * 1.2; // jumps cost more
        return cost;
    }

    private List<PathNode> reconstruct(PathNode end) {
        List<PathNode> out = new ArrayList<>();
        PathNode cur = end;
        while (cur != null) {
            out.add(cur);
            cur = cur.parent;
        }
        Collections.reverse(out);
        return out;
    }
}
