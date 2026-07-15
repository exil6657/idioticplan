package com.zenith.client.engine.path;

import java.util.List;

/** Result of a pathfinding call. */
public final class PathResult {

    public enum Status { FOUND, PARTIAL, UNREACHABLE, TIMEOUT }

    public final Status status;
    public final List<PathNode> nodes;
    public final double totalCost;
    public final long computeMs;
    public final BlockPos finalPos;

    public PathResult(Status status, List<PathNode> nodes, double totalCost, long computeMs, BlockPos finalPos) {
        this.status = status;
        this.nodes = nodes;
        this.totalCost = totalCost;
        this.computeMs = computeMs;
        this.finalPos = finalPos;
    }

    public boolean isSuccess() { return status == Status.FOUND || status == Status.PARTIAL; }
}
