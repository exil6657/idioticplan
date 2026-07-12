package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class GardenLevelUpEvent extends ZenithEvent {
    private final int newGardenLevel;
    public GardenLevelUpEvent(int newGardenLevel) { this.newGardenLevel = newGardenLevel; }
    public int getNewGardenLevel() { return newGardenLevel; }
    @Override public boolean isCancellable() { return false; }
}
