package com.zenith.client.engine.path.speed;

/** Detects armor pieces with speed bonuses (Farmer Boots, etc.). Stubs until Phase 6. */
public final class ArmorSpeedDetector implements SpeedSource {
    private double currentBonusMultiplier;
    public void setBonus(double mult) { this.currentBonusMultiplier = mult; }
    @Override public double currentSpeedBps() { return currentBonusMultiplier; }
}
