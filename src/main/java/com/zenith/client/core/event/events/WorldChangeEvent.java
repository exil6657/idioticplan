package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when the player joins a new world/dimension or switches SkyBlock islands. */
public class WorldChangeEvent extends ZenithEvent {

    private final String worldName;

    public WorldChangeEvent(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() { return worldName; }

    @Override public boolean isCancellable() { return false; }
}
