package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class PurseChangeEvent extends ZenithEvent {
    private final long oldCoins;
    private final long newCoins;
    public PurseChangeEvent(long oldCoins, long newCoins) { this.oldCoins = oldCoins; this.newCoins = newCoins; }
    public long getOldCoins() { return oldCoins; }
    public long getNewCoins() { return newCoins; }
    public long getDelta() { return newCoins - oldCoins; }
    @Override public boolean isCancellable() { return false; }
}
