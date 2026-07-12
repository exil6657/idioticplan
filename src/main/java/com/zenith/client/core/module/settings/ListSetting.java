package com.zenith.client.core.module.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** String list setting (whitelisted items, ignore lists, etc.). */
public class ListSetting extends Setting<List<String>> {

    public ListSetting(String name, String description, List<String> defaultValue) {
        super(name, description, new ArrayList<>(defaultValue == null ? List.of() : defaultValue));
    }

    public void add(String entry) {
        if (entry == null || entry.isBlank() || value.contains(entry)) return;
        List<String> copy = new ArrayList<>(value);
        copy.add(entry);
        setValue(copy);
    }

    public void remove(String entry) {
        List<String> copy = new ArrayList<>(value);
        if (copy.remove(entry)) setValue(copy);
    }

    public boolean contains(String entry) { return value.contains(entry); }

    @Override public Object serialize() { return new ArrayList<>(value); }

    @SuppressWarnings("unchecked")
    @Override public void deserialize(Object raw) {
        if (raw instanceof Collection<?> c) {
            List<String> out = new ArrayList<>();
            for (Object o : c) if (o instanceof String s) out.add(s);
            setValue(out);
        }
    }

    @Override public void reset() { setValue(new ArrayList<>()); }
}
