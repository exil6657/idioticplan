package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Detects when the player's forward path is blocked by a newly-placed (non-
 * natural) block — e.g. another player placing blocks in front of our farming
 * row to try and stop us. When this fires, triggers OBSTRUCTION at
 * REMOVE_OBSTRUCTION severity; the reaction engine looks at the block,
 * optionally sends "?" in chat, breaks it, and resumes.
 *
 * <p>Phase 10 provides the skeleton — actual "was this block just placed by a
 * player?" heuristic (recent block-change tracking, block material filter)
 * will be tightened in Phase 13+.</p>
 */
public class ObstructionDetector extends AbstractDetector {

    private static final long STUCK_MS_THRESHOLD = 1200L;
    private long stuckSinceMs;

    @Override
    public void tick(long nowMs) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { stuckSinceMs = 0; return; }
        // Skip if player is in water/air/ladder/etc. (legitimate movement).
        if (!mc.player.onGround() || mc.player.isPassenger() || mc.player.isSwimming()) {
            stuckSinceMs = 0;
            return;
        }
        // Check if player has tried to move forward (horizontal input present)
        // but hasn't moved horizontally for STUCK_MS_THRESHOLD.
        Vec3 v = mc.player.getDeltaMovement();
        boolean hasHorizInput = mc.player.input != null
                && (mc.player.input.leftImpulse != 0 || mc.player.input.forwardImpulse != 0);
        boolean stuckHoriz = Math.hypot(v.x, v.z) < 0.003d;
        // Also check for a solid block directly in front of eyes/feet.
        boolean blockAhead = solidBlockAhead();
        if (hasHorizInput && stuckHoriz && blockAhead) {
            if (stuckSinceMs == 0) stuckSinceMs = nowMs;
            if (nowMs - stuckSinceMs >= STUCK_MS_THRESHOLD) {
                trigger(FailsafeType.OBSTRUCTION, "stuck against block (horizontal v≈0)");
                stuckSinceMs = nowMs; // re-arm after cooldown
            }
        } else {
            stuckSinceMs = 0;
            clear(FailsafeType.OBSTRUCTION);
        }
    }

    private boolean solidBlockAhead() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        var look = mc.player.getLookAngle();
        // Sample 0.6..1.2 blocks ahead at feet and eye height.
        for (double d = 0.6d; d <= 1.4d; d += 0.3d) {
            BlockPos bp = BlockPos.containing(mc.player.position().add(look.scale(d)));
            BlockState s = mc.level.getBlockState(bp);
            if (s.isSolid() && s.blocksMotion()) return true;
        }
        // Feetsies: if the block immediately in front of feet is solid.
        Vec3 feet = mc.player.position();
        double yaw = Math.toRadians(mc.player.getYRot());
        double fx = feet.x - Math.sin(yaw);
        double fz = feet.z + Math.cos(yaw);
        BlockPos bpFeet = BlockPos.containing(fx, feet.y + 0.2d, fz);
        BlockState sFeet = mc.level.getBlockState(bpFeet);
        return sFeet.isSolid() && sFeet.blocksMotion()
                && mc.level.getBlockState(bpFeet.above()).blocksMotion();
    }
}
