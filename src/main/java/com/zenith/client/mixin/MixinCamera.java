package com.zenith.client.mixin;

import com.zenith.client.engine.eyes.ZenithEyes;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Camera mixin:
 * <ul>
 *   <li>Syncs game rotation back to ZenithEyes each tick (so the eyes engine knows where we are looking).</li>
 *   <li>Phase 18 will add Freecam/DroneCam position/rotation overrides here.</li>
 * </ul>
 */
@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Inject(method = "tick()V", at = @At("TAIL"), require = 0)
    private void zenith$onTick(CallbackInfo ci) {
        ZenithEyes.getInstance().syncFromGame(yRot, xRot);
    }
}
