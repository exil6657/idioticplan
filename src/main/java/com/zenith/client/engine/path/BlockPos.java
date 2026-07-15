package com.zenith.client.engine.path;

import java.util.Objects;

/**
 * Internal integer block-position. Phase 3 wiring will convert to/from
 * {@code net.minecraft.core.BlockPos} at the world-adapter boundary so the
 * engine doesn't depend on MC classes being on the classpath during core dev.
 */
public final class BlockPos {
    public final int x, y, z;
    public BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

    public static BlockPos of(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
    public static BlockPos containing(double x, double y, double z) { return of(x,y,z); }

    public BlockPos add(int dx, int dy, int dz) { return new BlockPos(x+dx, y+dy, z+dz); }
    public double distSq(BlockPos o) {
        double dx = x-o.x, dy=y-o.y, dz=z-o.z; return dx*dx+dy*dy+dz*dz;
    }
    public double dist(BlockPos o) { return Math.sqrt(distSq(o)); }
    public double distManhattan(BlockPos o) {
        return Math.abs(x-o.x) + Math.abs(y-o.y) + Math.abs(z-o.z);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof BlockPos b)) return false;
        return b.x == x && b.y == y && b.z == z;
    }
    @Override public int hashCode() { return Objects.hash(x,y,z); }
    @Override public String toString() { return "(" + x + "," + y + "," + z + ")"; }
}
