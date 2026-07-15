package com.zenith.client.core.module.settings;

/**
 * Float-based slider setting intended for GUI sliders (ESP line width, opacity, etc.).
 * Semantically similar to NumberSetting but uses float throughout for render pipelines.
 */
public class SliderSetting extends Setting<Float> {
    private final float min, max, step;

    public SliderSetting(String name, String description, float defaultValue, float min, float max, float step) {
        super(name, description, clamp(defaultValue, min, max));
        this.min = min; this.max = max; this.step = step;
    }

    @Override public void setValue(Float v) {
        if (v == null) return;
        super.setValue(clamp(v, min, max));
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getStep() { return step; }

    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof Number n) setValue(n.floatValue());
    }
    @Override public void reset() { setValue(min); }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
