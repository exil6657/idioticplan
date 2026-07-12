package com.zenith.client.gui.component;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.engine.render.RenderBridge;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing context for GUI/HUD rendering.
 *
 * <p>Wraps MC's {@link GuiGraphics} (26.1 Mojang name) via {@link RenderBridge}.
 * Component code should use this context exclusively, not MC classes directly.</p>
 */
public final class GuiDrawContext {

    public final int screenWidth, screenHeight;
    public final float tickDelta;
    private final GuiGraphics graphics;

    public GuiDrawContext(GuiGraphics g, int w, int h, float tickDelta) {
        this.graphics = g;
        this.screenWidth = w; this.screenHeight = h; this.tickDelta = tickDelta;
    }

    public GuiGraphics graphics() { return graphics; }

    public void fillRect(float x, float y, float w, float h, Color4f color) {
        RenderBridge.fillRect(graphics, x, y, w, h, RenderBridge.pack(color));
    }
    public void drawRoundedRect(float x, float y, float w, float h, float radius, Color4f color) {
        RenderBridge.drawRoundedRect(graphics, x, y, w, h, radius, RenderBridge.pack(color));
    }
    public void drawOutline(float x, float y, float w, float h, float thickness, Color4f color) {
        RenderBridge.drawOutline(graphics, x, y, w, h, thickness, RenderBridge.pack(color));
    }
    public void drawGradient(float x, float y, float w, float h, Color4f top, Color4f bottom) {
        RenderBridge.drawGradient(graphics, x, y, w, h, RenderBridge.pack(top), RenderBridge.pack(bottom));
    }
    public void drawString(String text, float x, float y, Color4f color, boolean shadow) {
        RenderBridge.drawString(graphics, text, x, y, RenderBridge.pack(color), shadow);
    }
    public float stringWidth(String text) {
        return RenderBridge.stringWidth(text);
    }
}
