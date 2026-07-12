package com.zenith.client.engine.path;

/**
 * A request for a path from (fromX, fromY, fromZ) to (toX, toY, toZ).
 *
 * <p>Set {@code allowEtherwarp} to allow short teleports over gaps/upwards;
 * set {@code stopDistance} to the radius (blocks) at which the executor
 * should consider the target "reached" (used when arriving near a point
 * of interest like a crop row end).</p>
 */
public final class PathRequest {

    public final double fromX, fromY, fromZ;
    public final double toX, toY, toZ;
    public final BlockPos targetPos;
    public final PathMode mode;
    public final boolean allowEtherwarp;
    public final boolean allowSprint;
    public final double stopDistance;
    public final long maxComputeMs;
    public final String tag;

    public PathRequest(double fx, double fy, double fz, double tx, double ty, double tz,
                       PathMode mode, boolean allowEtherwarp, boolean allowSprint,
                       double stopDistance, long maxComputeMs, String tag) {
        this.fromX = fx; this.fromY = fy; this.fromZ = fz;
        this.toX = tx; this.toY = ty; this.toZ = tz;
        this.targetPos = BlockPos.containing(tx, ty, tz);
        this.mode = mode;
        this.allowEtherwarp = allowEtherwarp;
        this.allowSprint = allowSprint;
        this.stopDistance = stopDistance;
        this.maxComputeMs = maxComputeMs;
        this.tag = tag;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private double fx, fy, fz, tx, ty, tz;
        private PathMode mode = PathMode.WALK;
        private boolean etherwarp, sprint = true;
        private double stop = 0.5;
        private long maxMs = 30;
        private String tag = "";

        public Builder from(double x, double y, double z) { fx=x;fy=y;fz=z; return this; }
        public Builder to(double x, double y, double z)   { tx=x;ty=y;tz=z; return this; }
        public Builder mode(PathMode m) { this.mode = m; return this; }
        public Builder allowEtherwarp(boolean v) { this.etherwarp = v; return this; }
        public Builder allowSprint(boolean v) { this.sprint = v; return this; }
        public Builder stopDistance(double d) { this.stop = d; return this; }
        public Builder maxComputeMs(long l) { this.maxMs = l; return this; }
        public Builder tag(String t) { this.tag = t; return this; }
        public PathRequest build() {
            return new PathRequest(fx, fy, fz, tx, ty, tz, mode, etherwarp, sprint, stop, maxMs, tag);
        }
    }
}
