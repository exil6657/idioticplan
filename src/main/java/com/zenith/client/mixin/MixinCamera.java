package com.zenith.client.mixin;

import com.zenith.client.engine.eyes.ZenithEyes;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Camera mixin — the actuator for ZenithEyes.
 * <p>
 * MC 26.1 unobfuscated: Camera.setup(BlockGetter, Entity, boolean, boolean, float)
 * sets yRot/xRot from the entity. We inject AFTER vanilla setup and override
 * with ZenithEyes's smoothed values if eyes are enabled.
 * Also syncs game→eyes each tick so the engine knows external changes.
 */
@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow private float yRot;
    @Shadow private float xRot;
    @Shadow private Entity entity;

    @Inject(method = "tick()V", at = @At("HEAD"), require = 0)
    private void zenith$onTickHead(CallbackInfo ci) {
        ZenithEyes eyes = ZenithEyes.getInstance();
        if (eyes == null) return;
        if (!eyes.isEnabled()) {
            eyes.syncFromGame(yRot, xRot);
            return;
        }
        // When eyes are enabled, drive camera from eyes engine (per-frame).
        // This makes rotation requests actually move the camera.
        try {
            float[] r = eyes.onCameraRender(1.0f);
            // Only override if we have a request queued/executing OR we are walking.
            // If no request, let vanilla look stand but keep eyes in sync via return path.
            if (r != null) {
                // If executor has something, apply it. Otherwise keep vanilla but update eyes.
                // We check if queue has pending or executor running via debugData state.
                var dbg = eyes.debugData();
                boolean hasTarget = dbg != null && !"IDLE".equals(dbg.state());
                if (hasTarget) {
                    this.yRot = r[0];
                    this.xRot = r[1];
                }
                // Always sync eyes from whatever final rotation we end up with
                eyes.syncFromGame(this.yRot, this.xRot);
            }
        } catch (Throwable t) {
            // don't crash camera thread
        }
    }

    @Inject(method = "setup", at = @At("TAIL"), require = 0)
    private void zenith$onSetup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        ZenithEyes eyes = ZenithEyes.getInstance();
        if (eyes == null || !eyes.isEnabled() || eyes.isFreecam()) return;
        try {
            float[] r = eyes.onCameraRender(partialTick);
            var dbg = eyes.debugData();
            boolean hasTarget = dbg != null && !"IDLE".equals(dbg.state());
            if (hasTarget && r != null) {
                this.yRot = r[0];
                this.xRot = r[1];
                if (this.entity != null) {
                    this.entity.setYRot(this.yRot);
                    this.entity.setXRot(this.xRot);
                }
            }
        } catch (Throwable ignored) {}
    }
}
