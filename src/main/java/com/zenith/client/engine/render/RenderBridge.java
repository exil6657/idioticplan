package com.zenith.client.engine.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zenith.client.engine.render.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Thin rendering adapter for GUI/HUD drawing. Wraps GuiGraphics (MC 26.1 Mojang
 * names — {@code GuiGraphics} is the class used for 2D HUD/GUI rendering).
 *
 * <p>If the class is renamed to {@code GuiGraphicsExtractor} in a 26.1 patch, we
 * can update this single class to reflect the new name without touching components.</p>
 */
public final class RenderBridge {

    private RenderBridge() {}

    public static void fillRect(GuiGraphics g, float x, float y, float w, float h, int argb) {
        int x1 = (int)x, y1 = (int)y, x2 = (int)(x+w), y2 = (int)(y+h);
        g.fill(x1, y1, x2, y2, argb);
    }

    public static void drawString(GuiGraphics g, String text, float x, float y, int argb, boolean shadow) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, text, (int)x, (int)y, argb, shadow);
    }

    public static float stringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static void drawRoundedRect(GuiGraphics g, float x, float y, float w, float h, float radius, int argb) {
        // Phase 5: draw as regular rect for now; proper rounded corners via BufferBuilder in Phase 5 polish.
        fillRect(g, x, y, w, h, argb);
    }

    public static void drawOutline(GuiGraphics g, float x, float y, float w, float h, float thickness, int argb) {
        fillRect(g, x, y, w, thickness, argb);                    // top
        fillRect(g, x, y + h - thickness, w, thickness, argb);   // bottom
        fillRect(g, x, y, thickness, h, argb);                   // left
        fillRect(g, x + w - thickness, y, thickness, h, argb);   // right
    }

    public static void drawGradient(GuiGraphics g, float x, float y, float w, float h,
                                    int topArgb, int botArgb) {
        g.fillGradient((int)x, (int)y, (int)(x+w), (int)(y+h), topArgb, botArgb);
    }

    /** Convenience: Color4f → packed ARGB int. */
    public static int pack(Color4f c) {
        int a = (int)(c.a() * 255f) & 0xFF;
        int r = (int)(c.r() * 255f) & 0xFF;
        int g = (int)(c.g() * 255f) & 0xFF;
        int b = (int)(c.b() * 255f) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Enable position-color shader for custom BufferBuilder geometry (ESP etc.). */
    public static void setupPosColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }
    public static void teardownShader() {
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}
