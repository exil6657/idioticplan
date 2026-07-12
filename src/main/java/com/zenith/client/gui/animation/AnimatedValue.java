package com.zenith.client.gui.animation;

/** Float value that animates between two values over a duration. */
public final class AnimatedValue {
    private float value;
    private float from, to;
    private long startMs;
    private long durationMs;
    private EasingType easing = EasingType.EASE_OUT;
    private boolean animating;

    public AnimatedValue(float initial) { this.value = this.from = this.to = initial; }

    public void animateTo(float target, long durationMs, EasingType type) {
        this.from = value; this.to = target;
        this.startMs = System.currentTimeMillis();
        this.durationMs = durationMs;
        this.easing = type;
        this.animating = true;
    }

    public void animateTo(float target, long durationMs) { animateTo(target, durationMs, EasingType.EASE_OUT); }

    public void setInstant(float v) { this.value = this.from = this.to = v; this.animating = false; }

    public float get() {
        if (!animating) return value;
        long now = System.currentTimeMillis();
        float t = (float) (now - startMs) / Math.max(1, durationMs);
        if (t >= 1f) { value = to; animating = false; return value; }
        value = from + (to - from) * EasingFunctions.ease(easing, t);
        return value;
    }

    public float getTarget() { return to; }
    public boolean isAnimating() {
        if (animating) get(); return animating;
    }
}
