package com.zenith.client.failsafe.detection;

import com.zenith.client.core.player.PlayerHealthMonitor;
import com.zenith.client.failsafe.FailsafeType;

/**
 * Escalates to {@code WARP_HOME} if the player's health drops below 3 hearts
 * (6 HP) or if hunger hits zero while a macro is active.
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
            trigger(FailsafeType.NO_HUNGER, "hunger=0");
        }
        if (!lowHp && nowMs - lastLowHpMs > 2500) clear(FailsafeType.LOW_HEALTH);
        if (!lowFood && nowMs - lastLowHungerMs > 2500) clear(FailsafeType.NO_HUNGER);
    }
}
