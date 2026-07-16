package com.zenith.client.mixin;

import com.zenith.client.ZenithClient;
import com.zenith.client.command.CommandInterceptor;
import com.zenith.client.core.chat.ChatPatternEngine;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.ChatReceivedEvent;
import com.zenith.client.core.event.events.PacketSendEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts outgoing/incoming packets:
 * <ul>
 *   <li>Outgoing chat packets starting with "." are intercepted client-side (dot-commands).</li>
 *   <li>Incoming chat packets fire ChatReceivedEvent → ChatPatternEngine.</li>
 *   <li>All packets fire PacketSendEvent/PacketReceiveEvent for subscribers.</li>
 * </ul>
 */
@Mixin(Connection.class)
public abstract class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void zenith$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        ZenithEventBus.getInstance().post(event);
        if (event.isCancelled()) { ci.cancel(); return; }

        if (packet instanceof ServerboundChatPacket chat) {
            String message = chat.message();
            if (CommandInterceptor.onOutgoingChat(message)) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onSendPacketWithListener(Packet<?> packet, Object listener, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        ZenithEventBus.getInstance().post(event);
        if (event.isCancelled()) { ci.cancel(); return; }
        if (packet instanceof ServerboundChatPacket chat) {
            if (CommandInterceptor.onOutgoingChat(chat.message())) ci.cancel();
        }
    }

    /** Incoming packet: fire PacketReceiveEvent and convert chat packets to ChatReceivedEvent. */
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onRead(io.netty.channel.ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        var ev = new com.zenith.client.core.event.events.PacketReceiveEvent(packet);
        ZenithEventBus.getInstance().post(ev);
        if (ev.isCancelled()) { ci.cancel(); return; }

        String text = null;
        int type = 0;
        String packetClass = packet.getClass().getSimpleName();
        if (packet instanceof ClientboundSystemChatPacket sys) {
            Component c = sys.content();
            if (c != null) text = c.getString();
        } else if (packet instanceof ClientboundPlayerChatPacket p) {
            Component c = p.body().content();
            if (c != null) text = c.getString();
            type = 0;
        } else if (packetClass.equals("ClientboundSetActionBarTextPacket") || packetClass.contains("ActionBar")) {
            // MC 26.1 moj: ClientboundSetActionBarTextPacket contains Component text
            try {
                var m = packet.getClass().getMethod("text");
                Object comp = m.invoke(packet);
                if (comp instanceof Component c) text = c.getString();
                else if (comp != null) text = comp.toString();
                type = 2; // action bar
            } catch (Throwable t) {
                // reflective fallback for field named 'text' or 'content'
                try {
                    var f = packet.getClass().getDeclaredField("text");
                    f.setAccessible(true);
                    Object comp = f.get(packet);
                    if (comp instanceof Component c) text = c.getString();
                } catch (Throwable ignored) {}
            }
        }
        // Also handle ClientboundSetActionBarTextPacket via instanceof if class exists
        if (text == null) {
            try {
                Class<?> abClz = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
                if (abClz.isInstance(packet)) {
                    var m = abClz.getMethod("text");
                    Object comp = m.invoke(packet);
                    if (comp instanceof Component c) text = c.getString();
                    type = 2;
                }
            } catch (Throwable ignored) {}
        }
        if (text != null) {
            String raw = text;
            String plain = raw.replaceAll("§.", "");
            ChatReceivedEvent chatEvt = new ChatReceivedEvent(plain, raw, type);
            ZenithEventBus.getInstance().post(chatEvt);
            // Also feed directly into PlayerHealthMonitor if contains ❤
            if (plain.contains("❤")) {
                try {
                    com.zenith.client.core.player.PlayerHealthMonitor.getInstance().parseActionBar(plain);
                } catch (Throwable ignored) {}
            }
            if (chatEvt.isCancelled()) ci.cancel();
        }
    }
}
