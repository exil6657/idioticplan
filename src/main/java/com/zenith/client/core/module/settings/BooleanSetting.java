package com.zenith.client.core.module.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }
    public void toggle() { setValue(!getValue()); }
    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof Boolean b) setValue(b);
    }
    @Override public void reset() { setValue(false); }
}
