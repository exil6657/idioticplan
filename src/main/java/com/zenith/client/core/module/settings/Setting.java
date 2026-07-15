package com.zenith.client.core.module.settings;

/** Common base for all module setting types. */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;
    private Runnable changeListener;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public T getValue() { return value; }

    public void setValue(T v) {
        if (value != null && value.equals(v)) return;
        this.value = v;
        if (changeListener != null) changeListener.run();
    }

    public void setChangeListener(Runnable r) { this.changeListener = r; }

    /** Serialize to a JSON-friendly primitive/string. */
    public abstract Object serialize();
    /** Deserialize from the output of {@link #serialize()}. */
    public abstract void deserialize(Object raw);

    /** Reset to default (implemented by subclass). */
    public abstract void reset();
}
