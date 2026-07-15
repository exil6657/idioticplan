package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when a keyboard key is pressed/repeated/released. Cancel to suppress. */
public class KeyInputEvent extends ZenithEvent {
    public final int key;
    public final int scanCode;
    public final int action;
    public final int mods;
    public KeyInputEvent(int key, int scanCode, int action, int mods) {
        this.key = key; this.scanCode = scanCode; this.action = action; this.mods = mods;
    }
}
