package com.zenith.client.core.module.settings;

public class NumberSetting extends Setting<Double> {
    private final double min, max, step;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description, clamp(defaultValue, min, max));
        this.min = min; this.max = max; this.step = step;
    }

    @Override public void setValue(Double v) {
        if (v == null) return;
        super.setValue(clamp(v, min, max));
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public float getFloat() { return getValue().floatValue(); }
    public int getInt() { return getValue().intValue(); }

    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof Number n) setValue(n.doubleValue());
    }
    @Override public void reset() { setValue(min); }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
