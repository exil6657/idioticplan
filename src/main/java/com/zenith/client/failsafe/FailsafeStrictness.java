package com.zenith.client.failsafe;

/**
 * Severity ladder for failsafe triggers. The {@link FailsafeManager} escalates
 * up the ladder when a condition persists, giving the reaction engine a chance
 * to resolve the situation before we disconnect.
 *
 * <p>Design (revised per user direction — Hypixel SkyBlock):</p>
 * <ul>
 *   <li>{@code NONE / NOTIFY} — advisory only.</li>
 *   <li>{@code PAUSE} — freeze key simulation, pause ticks, let reactions wiggle/look.</li>
 *   <li>{@code WIGGLE_REACT} — short "oops" camera wiggle + chat "?" then resume
 *       (e.g. for rotation snaps, player-nearby glances).</li>
 *   <li>{@code REMOVE_OBSTRUCTION} — break the blocks in the way (farming/mining)
 *       rather than fleeing.</li>
 *   <li>{@code REPATH} — on death/teleport/world-change, compute a fresh path
 *       back to the macro location (supports jump pads, NPCs, cross-server
 *       travel); never pause the macro.</li>
 *   <li>{@code WARP_ISLAND} — send {@code /is} then repath; the user's safe
 *       anchor is their private island, NOT {@code /home} (which doesn't exist
 *       in SkyBlock).</li>
 *   <li>{@code WARP_HUB} — {@code /hub} fallback if {@code /is} fails.</li>
 *   <li>{@code COMBAT} — swing weapon, eat, drink potions rather than fleeing
 *       (used by LOW_HEALTH).</li>
 *   <li>{@code INSTANT_RESPAWN} — on death, immediately respawn and repath
 *       (Hypixel doesn't show a real death screen in many SkyBlock modes).</li>
 *   <li>{@code DISCONNECT} — last-resort disconnect.</li>
 * </ul>
 *
 * <p>Severity is monotonic within a single trigger; once a failsafe has
 * escalated to {@code DISCONNECT} it cannot downgrade without a full reset.</p>
 */
public enum FailsafeStrictness {

    NONE(0),
    NOTIFY(1),
    PAUSE(2),
    WIGGLE_REACT(3),
    COMBAT(4),
    REMOVE_OBSTRUCTION(5),
    INSTANT_RESPAWN(6),
    REPATH(7),
    WARP_ISLAND(8),
    WARP_HUB(9),
    DISCONNECT(10);

    private final int level;

    FailsafeStrictness(int level) { this.level = level; }

    public int level() { return level; }

    public boolean atLeast(FailsafeStrictness other) {
        return this.level >= other.level;
    }
}
