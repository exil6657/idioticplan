package com.zenith.client.engine.render;

import com.zenith.client.ZenithClient;

/**
 * MC 26.1-specific renderer bindings (Blaze3D).
 *
 * <p>Research note (R001): 26.1 still uses OpenGL/Blaze3D (Vulkan migration is
 * scheduled for 26.2). All rendering goes through {@code PoseStack} +
 * {@code MultiBufferSource} as before — no new rendering API was introduced
 * in 26.1, but {@code HudRenderCallback} was removed in favor of
 * {@code HudElementRegistry}. This class is a placeholder; concrete draw calls
 * land in Phase 5.</p>
 */
public final class MC26Renderer {

    public static void drawRect(float x, float y, float w, float h, Color4f c) {
        // Phase 5: blit with GuiGraphics.fill / PositionColorVertexConsumer.
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float thickness, Color4f c) {
        ZenithClient.LOGGER.debug("[MC26Renderer] drawLine NYI");
    }

    private MC26Renderer() {}
}
