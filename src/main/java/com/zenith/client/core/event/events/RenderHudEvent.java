package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;
import net.minecraft.client.gui.GuiGraphics;

/** Fired each frame when the HUD is being rendered (for Zenith HUD panels). */
public class RenderHudEvent extends ZenithEvent {
    public final float tickDelta;
    public final GuiGraphics graphics;
    public final int screenWidth, screenHeight;
    public RenderHudEvent(GuiGraphics g, float tickDelta, int w, int h) {
        this.graphics = g; this.tickDelta = tickDelta; this.screenWidth = w; this.screenHeight = h;
    }
    @Override public boolean isCancellable() { return false; }
}
