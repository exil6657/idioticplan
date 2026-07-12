package com.zenith.client.failsafe;

/**
 * Severity ladder for failsafe triggers. The {@link FailsafeManager} escalates
 * up the ladder when a condition persists, giving the player (and the
 * mistake-simulation reactions) a chance to resolve the situation before we
 * disconnect.
 *
 * <p>Severity is monotonic within a single trigger; once a failsafe has
 * escalated to {@code DISCONNECT} it cannot downgrade without a full reset.</p>
 */
public enum FailsafeStrictness {

    /** Nothing is wrong, or the condition has cleared. */
    NONE(0),

    /** Advisory only — log + HUD toast, no action. */
    NOTIFY(1),

    /** Pause all macros and input simulation; wait for user input. */
    PAUSE(2),

    /** Warp home (execute {@code /home}) then pause. */
    WARP_HOME(3),

    /** Warp to spawn/hub then pause. */
    WARP_SPAWN(4),

    /** Full disconnect — kill the connection to the server. */
    DISCONNECT(5);

    private final int level;

    FailsafeStrictness(int level) {
        this.level = level;
    }

    public int level() { return level; }

    public boolean atLeast(FailsafeStrictness other) {
        return this.level >= other.level;
    }
}
