package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.BlockPos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LRU memory of recently travelled routes. When pathfinding from A to B, if
 * we've done it recently we prefer the remembered segment (which was already
 * human-shaped) over a fresh A* result, while still applying small variation.
 */
public final class RouteMemory {

    private static final int MAX_ROUTES = 64;

    private final LinkedHashMap<RouteKey, List<NaturalDeviator.Waypoint>> routes = new LinkedHashMap<>(MAX_ROUTES, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<RouteKey, List<NaturalDeviator.Waypoint>> e) {
            return size() > MAX_ROUTES;
        }
    };

    public record RouteKey(long fromHash, long toHash) {}

    public static long hash(BlockPos p) {
        return ((long) p.x & 0x3FFFFFFL) | (((long) p.z & 0x3FFFFFFL) << 26) | (((long) p.y & 0xFFFL) << 52);
    }

    public void remember(BlockPos from, BlockPos to, List<NaturalDeviator.Waypoint> path) {
        routes.put(new RouteKey(hash(from), hash(to)), List.copyOf(path));
    }

    public List<NaturalDeviator.Waypoint> recall(BlockPos from, BlockPos to) {
        return routes.get(new RouteKey(hash(from), hash(to)));
    }

    public void clear() { routes.clear(); }
    public int size() { return routes.size(); }
}
