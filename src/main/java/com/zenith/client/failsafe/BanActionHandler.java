package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.time.Instant;

/**
 * Performs the "escape" actions when a failsafe has escalated high enough —
 * sending {@code /home}, {@code /hub}, or disconnecting the client.
 *
 * <p>Every outgoing command is sent via the real MC packet pipeline (not
 * chat-intercepted) because dot-commands are the only thing we intercept, and
 * {@code /home} / {@code /hub} must reach the server to get the player out.</p>
 *
 * <p>We guard each action with a cooldown to avoid spamming the server with
 * duplicate {@code /home} packets if the failsafe stays triggered.</p>
 */
public final class BanActionHandler {

    private static final long COOLDOWN_MS = 1500;

    private long lastHomeAt;
    private long lastHubAt;
    private long lastDisconnectAt;

    public void init() {
        // no-op for now — reserved for future phase wiring.
    }

    public void sendHome() {
        long now = System.currentTimeMillis();
        if (now - lastHomeAt < COOLDOWN_MS) return;
        lastHomeAt = now;
        sendCommand("home");
        ZenithChat.getInstance().error("Failsafe → /home");
    }

    public void sendHub() {
        long now = System.currentTimeMillis();
        if (now - lastHubAt < COOLDOWN_MS) return;
        lastHubAt = now;
        // SkyBlock /hub command; falls through to lobby if already in hub.
        sendCommand("hub");
        ZenithChat.getInstance().error("Failsafe → /hub");
    }

    public void sendWarp(String warpName) {
        sendCommand("warp " + warpName);
    }

    public void disconnect(String reason) {
        long now = System.currentTimeMillis();
        if (now - lastDisconnectAt < 2000) return;
        lastDisconnectAt = now;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        ZenithChat.getInstance().error("Failsafe disconnect: {}", reason);
        ZenithClient.LOGGER.warn("[Failsafe] Disconnecting: {}", reason);
        mc.execute(() -> {
            try {
                mc.getConnection().getConnection().disconnect(Component.literal("[Zenith] " + reason));
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Failsafe] Disconnect threw", t);
            }
        });
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) return;
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        try {
            mc.execute(() -> {
                try {
                    // 26.1 constructor: ServerboundChatCommandPacket(String command, Instant timeStamp)
                    mc.player.connection.send(new ServerboundChatCommandPacket(cmd, Instant.now()));
                } catch (Throwable t) {
                    ZenithClient.LOGGER.error("[Failsafe] Failed to send /{}", cmd, t);
                }
            });
        } catch (Throwable t) {
            ZenithClient.LOGGER.error("[Failsafe] sendCommand schedule failed", t);
        }
    }
}
