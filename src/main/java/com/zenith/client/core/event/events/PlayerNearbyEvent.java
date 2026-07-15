package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class PlayerNearbyEvent extends ZenithEvent {
    private final String playerName;
    private final double distance;
    public PlayerNearbyEvent(String playerName, double distance) { this.playerName = playerName; this.distance = distance; }
    public String getPlayerName() { return playerName; }
    public double getDistance() { return distance; }
    @Override public boolean isCancellable() { return false; }
}
