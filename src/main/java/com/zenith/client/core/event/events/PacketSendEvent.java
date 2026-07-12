package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired before an outgoing (C2S) packet is sent. Cancel to block it. */
public class PacketSendEvent extends ZenithEvent {
    private Object packet;
    public PacketSendEvent(Object packet) { this.packet = packet; }
    public Object getPacket() { return packet; }
}
