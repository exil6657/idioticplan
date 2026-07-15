package com.zenith.client.gui.animation;

/** 3D animated vector (e.g. colour). */
public final class AnimatedVec3 {
    public final AnimatedValue x = new AnimatedValue(0);
    public final AnimatedValue y = new AnimatedValue(0);
    public final AnimatedValue z = new AnimatedValue(0);
    public void animateTo(float nx, float ny, float nz, long dur, EasingType t) {
        x.animateTo(nx, dur, t); y.animateTo(ny, dur, t); z.animateTo(nz, dur, t);
    }
}
