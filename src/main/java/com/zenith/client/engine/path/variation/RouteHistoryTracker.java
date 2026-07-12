package com.zenith.client.engine.path.variation;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.postprocess.NaturalDeviator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tracks recent paths per (from, to) so we don't walk exactly the same route every time. */
public final class RouteHistoryTracker {

    private static final int PER_ROUTE = 8;

    private final Map<Long, List<List<NaturalDeviator.Waypoint>>> history = new LinkedHashMap<>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, List<List<NaturalDeviator.Waypoint>>> e) {
            return size() > 128;
        }
    };

    public void record(BlockPos from, BlockPos to, List<NaturalDeviator.Waypoint> waypoints) {
        long key = key(from, to);
        List<List<NaturalDeviator.Waypoint>> list = history.computeIfAbsent(key, k -> new ArrayList<>());
        if (list.size() >= PER_ROUTE) list.remove(0);
        list.add(List.copyOf(waypoints));
    }

    public List<List<NaturalDeviator.Waypoint>> recent(BlockPos from, BlockPos to) {
        return history.getOrDefault(key(from, to), List.of());
    }

    private static long key(BlockPos from, BlockPos to) {
        long h1 = com.zenith.client.engine.path.postprocess.RouteMemory.hash(from);
        long h2 = com.zenith.client.engine.path.postprocess.RouteMemory.hash(to);
        return h1 ^ (h2 << 17);
    }
}
