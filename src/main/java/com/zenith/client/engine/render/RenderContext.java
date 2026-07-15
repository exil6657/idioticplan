package com.zenith.client.engine.render;

/** Frame/render context snapshot: partial tick, window size, tick count. */
public final class RenderContext {
    public final float tickDelta;
    public final int screenWidth;
    public final int screenHeight;
    public final long tickCount;
    public RenderContext(float tickDelta, int w, int h, long ticks) {
        this.tickDelta = tickDelta; this.screenWidth = w; this.screenHeight = h; this.tickCount = ticks;
    }
}
