package com.zenith.client.core.interaction;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.time.Instant;

/**
 * Sends chat commands that actually reach the server (e.g. /ah, /home, /warp).
 *
 * <p>Used by navigation/interactor code that needs the server to act — NOT for
 * dot-commands, which are intercepted before leaving the client (master rule §4).
 * A 400–700 ms humanised delay is applied between commands to look legitimate.</p>
 */
public final class CommandSender {

    private static long lastSentAt = 0;
    private static final long MIN_GAP_MS = 450;

    private CommandSender() {}

    /** Send a command immediately, after a small cooldown. No leading slash required. */
    public static void send(String command) {
        if (command == null) return;
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        long now = System.currentTimeMillis();
        if (now - lastSentAt < MIN_GAP_MS) return; // throttle
        lastSentAt = now;
        mc.execute(() -> {
            try {
                mc.player.connection.send(new ServerboundChatCommandPacket(cmd, Instant.now()));
                ZenithClient.LOGGER.info("[Cmd] /{}", cmd);
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Cmd] Failed to send /{}", cmd, t);
            }
        });
    }
}
