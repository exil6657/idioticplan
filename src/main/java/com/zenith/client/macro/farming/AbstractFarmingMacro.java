package com.zenith.client.macro.farming;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.macro.MacroModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Base for all row-based farming macros (pumpkin, melon, wheat, carrot, potato,
 * sugarcane, cocoa, cactus, wart, mushroom, cactus). Provides the common
 * row-walking + look-down-at-block + left-click-to-break loop.
 *
 * <p>Concrete subclasses supply {@link #targetYaw()} for row direction,
 * {@link #cropMatcher(BlockState)} for which blocks to break, and their
 * id/displayName/icon/skillFamily/dest.</p>
 */
public abstract class AbstractFarmingMacro extends MacroModule {

    protected int forwardTicks;
    protected boolean returning;

    @Override protected void onStart() {
        forwardTicks = 0;
        returning = false;
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            ZenithEyes.getInstance().setEnabled(true);
            // Pitch down to look at crops (about -50°) and yaw aligned with the row.
            ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                    .yaw(targetYaw()).pitch(-50f)
                    .priority(RotationRequest.Priority.MACRO)
                    .durationMs(220L)
                    .profile("farming-lookdown")
                    .tag("farming:start")
                    .build());
        }
    }

    @Override protected void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        var keys = InputEngine.getInstance().keys();
        if (keys == null) return;

        // Simple: hold W; look slightly down; attack when a crop is in front.
        keys.setForward(true);
        keys.setBack(false);
        keys.setLeft(false);
        keys.setRight(false);
        keys.setSprint(false);

        Vec3 eyes = mc.player.getEyePosition(1f);
        // Look at block in front at crop level — compute direction manually to avoid
        // signature mismatches across 26.x versions.
        float yawRad = (float) Math.toRadians(-targetYaw());
        float pitchRad = (float) Math.toRadians(50f); // 50° down
        float xLook = (float) (-Math.sin(yawRad) * Math.cos(pitchRad));
        float zLook = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        float yLook = (float) -Math.sin(pitchRad);
        Vec3 look = new Vec3(xLook, yLook, zLook);
        BlockPos target = BlockPos.containing(eyes.add(look.scale(1.5d)));
        BlockState st = mc.level.getBlockState(target);
        if (cropMatcher(st)) {
            keys.setAttack(true);
        } else {
            keys.setAttack(false);
        }

        // Keep yaw/pitch on target (smooth).
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(targetYaw()).pitch(-50f)
                .priority(RotationRequest.Priority.MACRO_TICK)
                .durationMs(120L)
                .profile("farming-row")
                .tag("farming:row")
                .preemptible(true)
                .build());
        forwardTicks++;
    }

    @Override protected void onStop() {
        var keys = InputEngine.getInstance().keys();
        if (keys != null) keys.halt();
        ZenithEyes.getInstance().setEnabled(false);
    }

    @Override protected void onFailsafePause() {
        var keys = InputEngine.getInstance().keys();
        if (keys != null) { keys.setForward(false); keys.setAttack(false); }
    }

    /** Yaw (degrees) the player should face to walk along the crop row. */
    protected abstract float targetYaw();

    /** @return true when the given state is a crop this macro should break. */
    protected abstract boolean cropMatcher(BlockState state);
}
