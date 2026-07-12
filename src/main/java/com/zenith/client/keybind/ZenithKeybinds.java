package com.zenith.client.keybind;

/**
 * Enum of all Zenith-registered keybinds.
 *
 * <p>Each entry corresponds to a logical action the user can bind; the actual
 * GLFW key is stored in {@link KeybindConfig} (JSON) and on the Fabric
 * KeyBinding object. The list is intentionally exhaustive so no hard-coded
 * key codes exist elsewhere in the codebase.</p>
 */
public enum ZenithKeybinds {

    OPEN_DASHBOARD("openDashboard", KeybindCategory.GLOBAL),
    EMERGENCY_STOP("emergencyStop", KeybindCategory.GLOBAL),
    TOGGLE_HUD("toggleHUD", KeybindCategory.GLOBAL),
    HUD_EDIT_MODE("hudEditMode", KeybindCategory.GLOBAL),
    DEBUG_MODE("debugMode", KeybindCategory.DEBUG),
    MACRO_TOGGLE("macroToggle", KeybindCategory.MACRO),
    PANIC_BUTTON("panicButton", KeybindCategory.GLOBAL),
    FREECAM("freecam", KeybindCategory.CAMERA),
    ZOOM("zoom", KeybindCategory.CAMERA),
    SAVE_CLIP("saveClip", KeybindCategory.GLOBAL),
    TOGGLE_FLIPPER("toggleFlipper", KeybindCategory.GLOBAL),
    AUTOPILOT_OVERRIDE("autopilotOverride", KeybindCategory.GLOBAL);

    private final String configId;
    private final KeybindCategory category;

    ZenithKeybinds(String configId, KeybindCategory category) {
        this.configId = configId;
        this.category = category;
    }

    public String getConfigId() { return configId; }
    public KeybindCategory getCategory() { return category; }
}
