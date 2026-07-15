package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired when an incoming (S2C) packet is received. Cancel to block processing. */
public class PacketReceiveEvent extends ZenithEvent {
    private Object packet;
    public PacketReceiveEvent(Object packet) { this.packet = packet; }
    public Object getPacket() { return packet; }
}
