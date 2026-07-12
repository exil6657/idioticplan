package com.zenith.client.flipping.waypoint;

import com.zenith.client.engine.path.BlockPos;

public final class Waypoint {
    public final String name;
    public final WaypointType type;
    public final BlockPos pos;
    public Waypoint(String name, WaypointType type, BlockPos pos) {
        this.name = name; this.type = type; this.pos = pos;
    }
}
