package com.zenith.client.gui.animation;

/** 2D animated vector (x, y). */
public final class AnimatedVec2 {
    public final AnimatedValue x = new AnimatedValue(0f);
    public final AnimatedValue y = new AnimatedValue(0f);

    public void animateTo(float nx, float ny, long durationMs, EasingType type) {
        x.animateTo(nx, durationMs, type); y.animateTo(ny, durationMs, type);
    }
    public void setInstant(float nx, float ny) { x.setInstant(nx); y.setInstant(ny); }
    public float getX() { return x.get(); }
    public float getY() { return y.get(); }
    public boolean isAnimating() { return x.isAnimating() || y.isAnimating(); }
}
