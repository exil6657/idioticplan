package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when a mouse button is pressed/released. Cancel to suppress. */
public class MouseInputEvent extends ZenithEvent {
    public final int button;
    public final int action; // GLFW_PRESS=1, GLFW_RELEASE=0, GLFW_REPEAT=2
    public final int mods;
    public MouseInputEvent(int button, int action, int mods) {
        this.button = button; this.action = action; this.mods = mods;
    }
}
