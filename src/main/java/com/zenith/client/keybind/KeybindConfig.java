package com.zenith.client.keybind;

import java.util.EnumMap;
import java.util.Map;

/**
 * In-memory keybind assignment map. Initialised from MainConfig.keybinds
 * (loaded by ConfigManager) and written back on change via ConfigManager.
 */
public class KeybindConfig {

    private final Map<ZenithKeybinds, String> binds = new EnumMap<>(ZenithKeybinds.class);

    public KeybindConfig() {
        // Defaults: match MainConfig.defaultKeybinds()
        set(ZenithKeybinds.OPEN_DASHBOARD,     "KEY_RIGHT_CONTROL");
        set(ZenithKeybinds.EMERGENCY_STOP,     "KEY_END");
        set(ZenithKeybinds.TOGGLE_HUD,         "KEY_UNKNOWN");
        set(ZenithKeybinds.HUD_EDIT_MODE,      "KEY_UNKNOWN");
        set(ZenithKeybinds.DEBUG_MODE,         "KEY_UNKNOWN");
        set(ZenithKeybinds.MACRO_TOGGLE,       "KEY_UNKNOWN");
        set(ZenithKeybinds.PANIC_BUTTON,       "KEY_UNKNOWN");
        set(ZenithKeybinds.FREECAM,            "KEY_UNKNOWN");
        set(ZenithKeybinds.ZOOM,               "KEY_UNKNOWN");
        set(ZenithKeybinds.SAVE_CLIP,          "KEY_UNKNOWN");
        set(ZenithKeybinds.TOGGLE_FLIPPER,     "KEY_UNKNOWN");
        set(ZenithKeybinds.AUTOPILOT_OVERRIDE, "KEY_UNKNOWN");
    }

    public String get(ZenithKeybinds key) { return binds.get(key); }
    public void set(ZenithKeybinds key, String glfwName) { binds.put(key, glfwName); }

    public Map<ZenithKeybinds, String> snapshot() { return new EnumMap<>(binds); }
}
