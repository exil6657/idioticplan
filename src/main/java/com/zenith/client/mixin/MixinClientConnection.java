package com.zenith.client.mixin;

import com.zenith.client.command.CommandInterceptor;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.PacketSendEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts outgoing/incoming packets.
 *
 * <p>Purposes:
 * <ul>
 *   <li>Block dot-command chat packets (".z ...") client-side so they never reach Hypixel
 *       — master rule §4 (server invisibility) and §2 (commands). Implemented via
 *       {@link CommandInterceptor#onOutgoingChat(String)}.</li>
 *   <li>Fire {@link PacketSendEvent} / {@link PacketReceiveEvent} for failsafe,
 *       anticheat simulation, and chat-pattern detectors.</li>
 * </ul>
 */
@Mixin(Connection.class)
public abstract class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void zenith$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        ZenithEventBus.getInstance().post(event);
        if (event.isCancelled()) { ci.cancel(); return; }

        // Dot-command interception:
        if (packet instanceof ServerboundChatPacket chat) {
            String message = chat.message();
            if (CommandInterceptor.onOutgoingChat(message)) {
                ci.cancel();
                return;
            }
        }
    }

    // Packet receive is wired at the PacketListener level in Phase 7 (failsafe);
    // the netty-channel method name varies across MC versions, so we leave this
    // injection as a no-op target via require=0 to avoid class-load failures if
    // the exact signature shifts between 26.1 patches.
    @Inject(method = "handleInboundPacket(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onReceivePacket(CallbackInfo ci) {
        // Filled in Phase 7 failsafe pipeline.
    }
}
