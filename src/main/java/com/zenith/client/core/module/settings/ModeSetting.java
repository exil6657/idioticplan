package com.zenith.client.core.module.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> options;

    public ModeSetting(String name, String description, String defaultValue, String... options) {
        super(name, description, defaultValue);
        this.options = Arrays.asList(options);
        if (!this.options.contains(defaultValue)) {
            throw new IllegalArgumentException("Default '" + defaultValue + "' not in options");
        }
    }

    public List<String> getOptions() { return List.copyOf(options); }

    public void next() {
        int idx = options.indexOf(getValue());
        setValue(options.get((idx + 1) % options.size()));
    }

    @Override public Object serialize() { return getValue(); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof String s && options.contains(s)) setValue(s);
    }
    @Override public void reset() { setValue(options.get(0)); }
}
