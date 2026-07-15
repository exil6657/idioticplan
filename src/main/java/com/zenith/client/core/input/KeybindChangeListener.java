package com.zenith.client.core.input;

/** Listener notified when Minecraft keybinds are changed in controls menu. */
@FunctionalInterface
public interface KeybindChangeListener {
    void onKeybindsChanged();
}
