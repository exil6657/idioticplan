package com.zenith.client.core.player;

import com.zenith.client.world.World;

/** Determines the player's high-level state: moving, in combat, in menu, falling, dead, etc. */
public final class PlayerStateDetector {

    private static final PlayerStateDetector INSTANCE = new PlayerStateDetector();
    public static PlayerStateDetector getInstance() { return INSTANCE; }

    public boolean isMoving() { return PlayerPositionTracker.getInstance().speedBps() > 0.05d; }
    public boolean isSprinting() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p != null && p.isSprinting();
    }
    public boolean isInCombat() { return PlayerHealthMonitor.getInstance().recentlyDamaged(2500); }
    public boolean isInMenu() { return World.get().playerInScreen(); }
    public boolean isFalling() { return PlayerPositionTracker.getInstance().verticalSpeed() < -0.5d; }
    public boolean isDead() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p == null || p.isDeadOrDying() || p.getHealth() <= 0;
    }
    public boolean isRiding() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p != null && p.isPassenger();
    }
}
