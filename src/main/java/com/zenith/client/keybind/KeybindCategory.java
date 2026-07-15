package com.zenith.client.keybind;

/** Grouping for Zenith's custom keybinds in the controls screen. */
public enum KeybindCategory {

    GLOBAL("Zenith Client"),
    MACRO("Zenith Macros"),
    DEBUG("Zenith Debug"),
    CAMERA("Zenith Camera");

    private final String displayName;
    KeybindCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
