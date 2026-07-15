package com.zenith.client.gui.animation;

import com.zenith.client.engine.render.Color4f;

/** Smoothly transitions between two colours. */
public final class AnimatedColor {
    private final AnimatedVec4 rgba = new AnimatedVec4();

    public void animateTo(Color4f target, long dur, EasingType t) {
        rgba.animateTo(target.r(), target.g(), target.b(), target.a(), dur, t);
    }
    public void setInstant(Color4f c) { rgba.setInstant(c.r(), c.g(), c.b(), c.a()); }
    public Color4f get() { return new Color4f(rgba.x.get(), rgba.y.get(), rgba.z.get(), rgba.w.get()); }
}
