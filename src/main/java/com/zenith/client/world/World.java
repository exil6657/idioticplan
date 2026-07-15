package com.zenith.client.world;

import com.zenith.client.engine.path.BlockPos;

/**
 * Static access to the current {@link WorldAdapter}. Installed when MC loads
 * a world; cleared on disconnect. All engine/macro code calls {@code World.get()}
 * instead of importing MC classes directly.
 */
public final class World {

    private static WorldAdapter adapter = EmptyWorldAdapter.INSTANCE;

    private World() {}

    public static void install(WorldAdapter a) { adapter = a != null ? a : EmptyWorldAdapter.INSTANCE; }
    public static void reset() { adapter = EmptyWorldAdapter.INSTANCE; }
    public static WorldAdapter get() { return adapter; }

    public static BlockPos playerPos() { return adapter.playerBlockPos(); }
    public static boolean hasLineOfSight(double x1, double y1, double z1, double x2, double y2, double z2) {
        return adapter.raycastClear(x1, y1, z1, x2, y2, z2);
    }
    public static boolean canStandAt(BlockPos p) {
        return adapter.isPassable(p) && adapter.isPassable(p.add(0,1,0)) && adapter.isSolid(p.add(0,-1,0))
                && !adapter.isLava(p) && !adapter.isWater(p);
    }
}
