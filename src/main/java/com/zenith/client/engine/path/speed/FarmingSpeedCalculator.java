package com.zenith.client.engine.path.speed;

/**
 * Calculates effective speed while farming, accounting for crop type,
 * reforge bonuses, Elephant pet, etc. Stub until Phase 10.
 */
public final class FarmingSpeedCalculator implements SpeedSource {
    private double bonus;
    public void setBonus(double mult) { this.bonus = mult; }
    @Override public double currentSpeedBps() { return bonus; }
}
