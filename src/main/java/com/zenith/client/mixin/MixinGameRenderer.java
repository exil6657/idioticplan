package com.zenith.client.mixin;

import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.RenderWorldEvent;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Fires {@link RenderWorldEvent} after the level is rendered so ESP/route
 * visualisers can draw world-space overlays. Uses Mojang names for 26.1.
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "renderLevel(FJZLnet/minecraft/client/Camera;)V", at = @At("RETURN"), require = 0)
    private void zenith$postRenderWorld(float partialTick, long finishNanoTime, boolean blockOutlines,
                                        net.minecraft.client.Camera camera, CallbackInfo ci) {
        // PoseStack supplied by GameRenderer's render context is available via Minecraft.getInstance().gameRenderer...
        // Phase 5 will capture the PoseStack via a local capture; for now pass a new one for safety.
        ZenithEventBus.getInstance().post(new RenderWorldEvent(new PoseStack(), partialTick));
    }
}
