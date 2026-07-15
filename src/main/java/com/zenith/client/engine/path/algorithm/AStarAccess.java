package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;

/** Package-private bridge so PathNavigator can create a fresh A* for a request. */
public final class AStarAccess {
    private static final AStarPathfinder INSTANCE = new AStarPathfinder();
    public static AStarPathfinder astar() { return INSTANCE; }
    public static PathResult compute(PathRequest req) { return INSTANCE.compute(req); }
    private AStarAccess() {}
}
