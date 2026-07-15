package com.zenith.client.gui.animation;

public final class AnimatedVec4 {
    public final AnimatedValue x = new AnimatedValue(0);
    public final AnimatedValue y = new AnimatedValue(0);
    public final AnimatedValue z = new AnimatedValue(0);
    public final AnimatedValue w = new AnimatedValue(0);
    public void animateTo(float nx, float ny, float nz, float nw, long dur, EasingType t) {
        x.animateTo(nx, dur, t); y.animateTo(ny, dur, t); z.animateTo(nz, dur, t); w.animateTo(nw, dur, t);
    }
    public void setInstant(float nx, float ny, float nz, float nw) {
        x.setInstant(nx); y.setInstant(ny); z.setInstant(nz); w.setInstant(nw);
    }
}
