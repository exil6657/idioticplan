package com.zenith.client.core.player;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.world.World;

/** Tracks player motion: position, velocity, yaw/pitch deltas, and "stuck" detection. */
public final class PlayerPositionTracker {

    private static final PlayerPositionTracker INSTANCE = new PlayerPositionTracker();

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private double vx, vy, vz;
    private float yawVel, pitchVel;
    private long lastUpdateMs;
    private long stationaryForMs;
    private double blocksTravelled;

    public static PlayerPositionTracker getInstance() { return INSTANCE; }

    public void tick() {
        var w = World.get();
        double x = w.playerX(), y = w.playerY(), z = w.playerZ();
        float yaw = w.playerYaw(), pitch = w.playerPitch();
        long now = System.currentTimeMillis();
        if (lastUpdateMs != 0) {
            float dt = Math.max(0.001f, (now - lastUpdateMs)/1000f);
            vx = (x - lastX) / dt;
            vy = (y - lastY) / dt;
            vz = (z - lastZ) / dt;
            yawVel = (yaw - lastYaw) / dt;
            pitchVel = (pitch - lastPitch) / dt;
            double moved = Math.hypot(x-lastX, z-lastZ);
            blocksTravelled += moved;
            if (moved < 0.001d) stationaryForMs += now - lastUpdateMs;
            else stationaryForMs = 0;
        }
        lastX=x; lastY=y; lastZ=z; lastYaw=yaw; lastPitch=pitch; lastUpdateMs = now;
    }

    public double speedBps() { return Math.hypot(vx, vz); }
    public double verticalSpeed() { return vy; }
    public float yawDegPerSec() { return yawVel; }
    public boolean isStuck(long thresholdMs) { return stationaryForMs >= thresholdMs; }
    public double blocksTravelled() { return blocksTravelled; }
    public void reset() { blocksTravelled = 0; stationaryForMs = 0; }
}
