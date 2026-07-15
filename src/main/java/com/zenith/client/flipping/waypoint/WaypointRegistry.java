package com.zenith.client.flipping.waypoint;

import com.zenith.client.engine.path.BlockPos;

import java.util.*;

/** Known SkyBlock hub waypoints the flipper navigates to. Phase 10 populates island positions. */
public final class WaypointRegistry {
    private static final WaypointRegistry INSTANCE = new WaypointRegistry();
    public static WaypointRegistry getInstance() { return INSTANCE; }
    private final Map<String, Waypoint> waypoints = new HashMap<>();
    private WaypointRegistry() {
        register(new Waypoint("auction_house", WaypointType.AUCTION_HOUSE, new BlockPos(16, 71, 104)));
        register(new Waypoint("bazaar", WaypointType.BAZAAR, new BlockPos(-50, 71, -70)));
        register(new Waypoint("bank", WaypointType.BANK, new BlockPos(20, 71, 80)));
    }
    public void register(Waypoint w) { waypoints.put(w.name, w); }
    public Waypoint get(String name) { return waypoints.get(name); }
    public Collection<Waypoint> all() { return Collections.unmodifiableCollection(waypoints.values()); }
}
