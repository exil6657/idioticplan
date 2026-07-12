package com.zenith.client.core.module.settings;

public class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue == null ? "" : defaultValue);
        this.maxLength = maxLength;
    }

    public StringSetting(String name, String description, String defaultValue) {
        this(name, description, defaultValue, 128);
    }

    @Override public void setValue(String v) {
        if (v == null) v = "";
        if (v.length() > maxLength) v = v.substring(0, maxLength);
        super.setValue(v);
    }

    public int getMaxLength() { return maxLength; }

    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof String s) setValue(s);
    }
    @Override public void reset() { setValue(""); }
}
