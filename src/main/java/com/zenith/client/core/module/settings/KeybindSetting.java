package com.zenith.client.core.module.settings;

/** GLFW key code for a module toggle. 0 means unbound. */
public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
    }
    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof Number n) setValue(n.intValue());
    }
    @Override public void reset() { setValue(0); }
    public boolean isBound() { return getValue() != null && getValue() != 0; }
}
