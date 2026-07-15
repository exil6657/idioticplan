package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Reaction for OBSTRUCTION failsafe: when a farming/mining macro's forward
 * progress is blocked by recently-placed blocks (player-placed walls, crops,
 * etc.), pause farming, look at the offending block, optionally send "?" in
 * chat (if a player is nearby to make it look like we noticed), and break
 * the block using the currently held tool. Then resume the macro from where
 * it paused.
 *
 * <p>Sequence (≈2-4 s):</p>
 * <ol>
 *   <li>PAUSE macro ticking for ~600 ms (wiggle).</li>
 *   <li>Look at the blocking block.</li>
 *   <li>Send "?" if a player is nearby.</li>
 *   <li>Left-click the block (with the right tool already selected).</li>
 *   <li>Wait until the block is gone; if it doesn't disappear after ~2.5 s of
 *       breaking, give up and repath around it.</li>
 *   <li>Resume macro.</li>
 * </ol>
 */
public final class RemoveObstructionAction {

    private static boolean active;
    private static long startedAt;
    private static int stage;
    private static BlockPos target;
    private static long breakStartedAt;

    public static void trigger() {
        if (active) return;
        active = true;
        startedAt = System.currentTimeMillis();
        stage = 0;
        target = findBlockingBlock();
        breakStartedAt = 0;
        ZenithClient.LOGGER.debug("[Failsafe] obstruction reaction target={}", target);
    }

    public static boolean isActive() { return active; }

    public static void tick() {
        if (!active) return;
        long now = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { active = false; return; }

        switch (stage) {
            case 0 -> {
                // Short wiggle pause.
                WiggleReactionAction.trigger();
                stage = 1;
                break;
            }
            case 1 -> {
                if (target == null) { active = false; return; }
                // Look at target.
                var eyes = mc.player.getEyePosition(0);
                var center = Vec3.atCenterOf(target);
                double dx = center.x - eyes.x, dy = center.y - eyes.y, dz = center.z - eyes.z;
                float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
                float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz)));
                ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                        .yaw(yaw).pitch(pitch)
                        .priority(RotationRequest.Priority.FAILSAFE)
                        .durationMs(220L)
                        .profile("failsafe-obstruction")
                        .tag("failsafe:obstruction")
                        .build());
                if (now - startedAt > 400) {
                    stage = 2;
                    ChatQuestionMarkAction.send();
                    breakStartedAt = now;
                }
                break;
            }
            case 2 -> {
                // Start breaking the block.
                try {
                    var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
                    if (keys != null) keys.setAttack(true);
                } catch (Throwable t) { ZenithClient.LOGGER.debug("[Failsafe] break hold failed", t); }
                stage = 3;
                break;
            }
            case 3 -> {
                // Wait until block is gone OR timeout.
                if (target != null) {
                    BlockState st = mc.level.getBlockState(target);
                    boolean gone = st.isAir() || !st.blocksMotion();
                    if (gone) {
                        release();
                        active = false;
                        ZenithClient.LOGGER.debug("[Failsafe] obstruction cleared");
                        return;
                    }
                }
                if (now - breakStartedAt > 2500) {
                    release();
                    // Give up breaking — request repath.
                    RepathReactionAction.trigger("obstruction-timeout");
                    ZenithClient.LOGGER.debug("[Failsafe] obstruction didn't break, requesting repath");
                    active = false;
                }
                break;
            }
        }
    }

    private static void release() {
        try {
            var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
            if (keys != null) keys.setAttack(false);
        } catch (Throwable ignored) {}
    }

    /**
     * Raytrace forward up to 4 blocks to find a solid block in front of the
     * player that is likely blocking macro movement.
     */
    private static BlockPos findBlockingBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        var eyes = mc.player.getEyePosition(1f);
        var look = mc.player.getLookAngle();
        var end = eyes.add(look.scale(4d));
        var ctx = new net.minecraft.world.level.ClipContext(eyes, end,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player);
        BlockHitResult hit = mc.level.clip(ctx);
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        return hit.getBlockPos();
    }
}
