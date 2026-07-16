package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import com.zenith.client.engine.eyes.ZenithEyes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

/**
 * Reaction for LOW_HEALTH / LOW_HUNGER / mob-aggro situations: instead of
 * warping home (which doesn't exist in SkyBlock and would get us banned for
 * teleporting out of combat), we:
 * <ol>
 *   <li>Aim at the closest damaging entity (if any).</li>
 *   <li>Swing the currently held item (left-click attack).</li>
 *   <li>Eat / drink if a food/potion is in the hotbar (Phase 14+).</li>
 *   <li>Only escalate to /is → /hub → disconnect if health keeps dropping
 *       for several seconds despite fighting back.</li>
 * </ol>
 *
 * <p>NOTE: Hypixel uses a custom health system; effective HP is read from
 * the ActionBar/Scoreboard rather than mc.player.getHealth() when available.</p>
 */
public final class CombatReactionAction {

    private static boolean active;
    private static long startedAt;
    private static int swings;

    public static void trigger() {
        active = true;
        startedAt = System.currentTimeMillis();
        swings = 0;
        ZenithClient.LOGGER.debug("[Failsafe] combat reaction started (low health/aggro)");
    }

    public static boolean isActive() { return active; }
    public static void cancel() {
        active = false;
        try {
            var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
            if (keys != null) keys.setAttack(false);
        } catch (Throwable ignored) {}
    }

    public static void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { active = false; return; }

        // Find nearest hostile within 6 blocks.
        LivingEntity target = null;
        double best = 36d; // 6 blocks squared
        for (var e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == mc.player) continue;
            if (le.isDeadOrDying()) continue;
            // Heuristic: treat non-player, non-villager, non-friendly living entities
            // as attackable. Refinement happens in Phase 15 combat macros.
            if (le instanceof net.minecraft.world.entity.npc.AbstractVillager) continue;
            if (le instanceof net.minecraft.world.entity.decoration.ArmorStand) continue;
            double d = le.distanceToSqr(mc.player);
            if (d < best) { best = d; target = le; }
        }

        if (target != null) {
            // Aim at the target — correct MC yaw formula: yaw = atan2(-dx, dz) in degrees
            double dx = target.getX() - mc.player.getX();
            double dy = (target.getY() + target.getEyeHeight()/2.0) - mc.player.getEyeY();
            double dz = target.getZ() - mc.player.getZ();
            double distXZ = Math.sqrt(dx*dx + dz*dz);
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(0.1, distXZ)));
            // Clamp pitch
            pitch = Math.max(-90f, Math.min(90f, pitch));
            ZenithEyes.getInstance().requestRotation(
                    com.zenith.client.engine.eyes.RotationRequest.builder()
                            .yaw(yaw).pitch(pitch)
                            .priority(com.zenith.client.engine.eyes.RotationRequest.Priority.COMBAT)
                            .durationMs(180L) // slightly longer for human-like
                            .profile("failsafe-combat")
                            .tag("failsafe:combat")
                            .tracking(true) // track moving target if engine supports it
                            .build());
        }


        // Hold attack while a target is in range (KeySimulator drives left-click pulses).
        try {
            var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
            if (keys != null) keys.setAttack(target != null);
        } catch (Throwable t) { ZenithClient.LOGGER.debug("[Failsafe] combat swing failed", t); }
        swings++;

        // Auto-clear after 4 seconds — if health has recovered the detector clears.
        long now = System.currentTimeMillis();
        if (now - startedAt > 4000) {
            try {
                var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
                if (keys != null) keys.setAttack(false);
            } catch (Throwable ignored) {}
            active = false;
        }
    }
}
