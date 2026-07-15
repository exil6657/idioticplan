package com.zenith.client.engine.eyes.humanizer;

/**
 * Holder for situational state that the eyes engine uses to tune behaviour at
 * runtime (whether the player is moving, in combat, in a menu, etc.).
 *
 * <p>Updated each tick by ZenithEyes.tick() from the player state. Profiles
 * read this to modulate speed, jitter, and hesitation.</p>
 */
public final class ContextualBehavior {

    public boolean isMoving;
    public boolean isSprinting;
    public boolean isInCombat;
    public boolean isInMenu;
    public boolean isFalling;
    public float speed; // blocks/sec
    public float yawVelocity; // deg/s
    /** 0..1 multiplier — higher = more tense / more jitter / faster turns. */
    public float alertness = 0.3f;
    /** Session length in minutes; used to scale fatigue. */
    public float sessionMinutes;

    public void set(boolean moving, boolean sprinting, boolean combat, boolean menu,
                    boolean falling, float speed, float yawVel, float alertness, float sessionMin) {
        this.isMoving = moving;
        this.isSprinting = sprinting;
        this.isInCombat = combat;
        this.isInMenu = menu;
        this.isFalling = falling;
        this.speed = speed;
        this.yawVelocity = yawVel;
        this.alertness = alertness;
        this.sessionMinutes = sessionMin;
    }

    /** @return multiplier to apply to rotation duration (higher = slower). */
    public float durationMultiplier() {
        float m = 1f;
        if (isInCombat) m *= 0.75f;         // faster turns in combat
        if (isSprinting) m *= 0.9f;         // slightly faster when sprinting
        if (isInMenu) m *= 1.3f;            // slower, more deliberate while in menu
        m *= (1f + 0.2f * alertness);       // alertness scales up to +20% faster
        return m;
    }

    /** @return fatigue multiplier (>=1) scaling turn time as session grows. */
    public float fatigueMultiplier(float maxFatigue) {
        // Linear ramp to maxFatigue over 120 minutes of continuous play.
        float f = 1f + (maxFatigue - 1f) * Math.min(1f, sessionMinutes / 120f);
        return f;
    }
}
