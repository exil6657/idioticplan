package com.zenith.client.core.player;

import com.zenith.client.world.World;

/** Tracks health/food and fires failsafe triggers when critically low. Phase 7 consumes the readings. */
public final class PlayerHealthMonitor {
    private static final PlayerHealthMonitor INSTANCE = new PlayerHealthMonitor();
    public static PlayerHealthMonitor getInstance() { return INSTANCE; }
    private float lastHealth;
    private int lastFood;
    private long lastDamageAt;

    public void tick() {
        float h = World.get().playerHealth();
        int f = World.get().playerFood();
        if (h < lastHealth - 0.5f) lastDamageAt = System.currentTimeMillis();
        lastHealth = h; lastFood = f;
    }

    public float health() { return lastHealth; }
    public int food() { return lastFood; }
    public boolean recentlyDamaged(long windowMs) { return System.currentTimeMillis() - lastDamageAt < windowMs; }
}
