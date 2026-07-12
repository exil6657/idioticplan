package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.BlockPos;

/**
 * Reactive course correction: if the player drifts more than {@code threshold}
 * blocks from the planned segment, we request a full recompute rather than
 * trying to micro-adjust back (which produces robotic rubberbanding).
 */
public final class CourseCorrector {

    private double threshold = 0.8d;

    public void setThreshold(double t) { this.threshold = t; }

    /** @return distance from (px,pz) to the line segment (ax,az)-(bx,bz). */
    public static double distanceToSegment(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax, dz = bz - az;
        double lenSq = dx*dx + dz*dz;
        if (lenSq < 1e-6) return Math.hypot(px-ax, pz-az);
        double t = ((px - ax) * dx + (pz - az) * dz) / lenSq;
        t = Math.max(0d, Math.min(1d, t));
        double cx = ax + t * dx, cz = az + t * dz;
        return Math.hypot(px - cx, pz - cz);
    }

    public boolean shouldRecompute(double playerX, double playerY, double playerZ,
                                   NaturalDeviator.Waypoint segStart, NaturalDeviator.Waypoint segEnd) {
        double d = distanceToSegment(playerX, playerZ, segStart.x(), segStart.z(), segEnd.x(), segEnd.z());
        return d > threshold;
    }
}
