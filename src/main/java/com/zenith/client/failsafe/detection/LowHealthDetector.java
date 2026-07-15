package com.zenith.client.failsafe.detection;

import com.zenith.client.core.player.PlayerHealthMonitor;
import com.zenith.client.failsafe.FailsafeType;

/**
 * Triggers LOW_HEALTH (COMBAT reaction: look at attacker, swing to fight back)
 * when HP drops below 3 hearts (6 HP) or when hunger hits zero while a macro is
 * active. Does NOT warp home — per user direction we fight back rather than
 * fleeing. Hypixel's custom HP system (Defense / ❤ / Absorption) is read via
 * ActionBar/Scoreboard in PlayerHealthMonitor where available.
 */
public class LowHealthDetector extends AbstractDetector {

    private static final float LOW_HP_THRESHOLD = 6.0f;
    private long lastLowHpMs;
    private long lastLowHungerMs;

    @Override
    public void tick(long nowMs) {
        var h = PlayerHealthMonitor.getInstance();
        float hp = h.health();
        int food = h.food();
        boolean lowHp = hp > 0 && hp <= LOW_HP_THRESHOLD;
        boolean lowFood = food <= 0;
        if (lowHp) {
            lastLowHpMs = nowMs;
            trigger(FailsafeType.LOW_HEALTH, String.format("hp=%.1f (<=%.0f)", hp, LOW_HP_THRESHOLD));
        }
        if (lowFood) {
            lastLowHungerMs = nowMs;
            trigger(FailsafeType.LOW_HUNGER, "hunger=0");
        }
        if (!lowHp && nowMs - lastLowHpMs > 2500) clear(FailsafeType.LOW_HEALTH);
        if (!lowFood && nowMs - lastLowHungerMs > 2500) clear(FailsafeType.LOW_HUNGER);
    }
}
