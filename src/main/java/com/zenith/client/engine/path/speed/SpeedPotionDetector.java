package com.zenith.client.engine.path.speed;

/** Detects active Speed potion effect. Phase 6 wires to mob-effect tracker. */
public final class SpeedPotionDetector implements SpeedSource {
    private int level = 0;
    public void setLevel(int level) { this.level = Math.max(0, level); }
    public int getLevel() { return level; }
    @Override public double currentSpeedBps() { return level > 0 ? SpeedConfig.SPEED_POT_PER_LEVEL * level : 0d; }
}
