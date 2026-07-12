package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.time.Instant;

/**
 * Performs the escape/travel actions when a failsafe has escalated — sending
 * {@code /is}, {@code /hub}, triggering respawn, or disconnecting the client.
 *
 * <p><b>SkyBlock note:</b> the user-specified safe anchor is the player's
 * private island ({@code /is}), NOT {@code /home} (which does not exist in
 * SkyBlock). {@code /hub} is the fallback if {@code /is} fails.</p>
 *
 * <p>Combat (low health) and obstruction (block in the way) do NOT reach this
 * handler — they are handled inline by the reaction engine, which fights back
 * or breaks the block instead of fleeing.</p>
 *
 * <p>Every outgoing command is sent via the real MC packet pipeline (not
 * chat-intercepted) because dot-commands are the only thing we intercept, and
 * {@code /is} / {@code /hub} must reach the server to get the player out.</p>
 *
 * <p>We guard each action with a cooldown to avoid spamming the server with
 * duplicate packets if the failsafe stays triggered.</p>
 */
public final class BanActionHandler {

    private static final long COOLDOWN_MS = 1500;

    private long lastIslandAt;
    private long lastHubAt;
    private long lastRespawnAt;
    private long lastDisconnectAt;

    public void init() {
        // no-op for now — reserved for future phase wiring.
    }

    /** Send /is (private island) — the SkyBlock "safe" anchor per user direction. */
    public void sendIsland() {
        long now = System.currentTimeMillis();
        if (now - lastIslandAt < COOLDOWN_MS) return;
        lastIslandAt = now;
        sendCommand("is");
        ZenithChat.getInstance().error("Failsafe → /is");
    }

    /** @deprecated Use {@link #sendIsland()} — /home does not exist in SkyBlock. */
    @Deprecated
    public void sendHome() { sendIsland(); }

    public void sendHub() {
        long now = System.currentTimeMillis();
        if (now - lastHubAt < COOLDOWN_MS) return;
        lastHubAt = now;
        // SkyBlock /hub command; falls through to lobby if already in hub.
        sendCommand("hub");
        ZenithChat.getInstance().error("Failsafe → /hub");
    }

    /** Instant respawn (Hypixel SkyBlock death screen auto-dismisses). */
    public void respawn() {
        long now = System.currentTimeMillis();
        if (now - lastRespawnAt < 500) return;
        lastRespawnAt = now;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> {
            try {
                mc.player.respawn();
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Failsafe] respawn() failed", t);
            }
        });
        ZenithChat.getInstance().info("Respawning…");
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
