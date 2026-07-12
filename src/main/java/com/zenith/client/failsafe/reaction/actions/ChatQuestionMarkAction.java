package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

import java.time.Instant;

/**
 * Sends a single "?" in chat (all-chat) when the player is caught looking in
 * the "wrong" direction — i.e. when our auto-rotation has been snapped. Looks
 * like a confused real player rather than a disconnected bot.
 *
 * <p>Sent via {@link ServerboundChatPacket} (real chat packet) because it needs
 * to be visible to other players; rate-limited to one "?" every 30 s to avoid
 * spam.</p>
 */
public final class ChatQuestionMarkAction {

    private static long lastSentAt;
    private static final long COOLDOWN_MS = 30_000L;

    public static void send() {
        long now = System.currentTimeMillis();
        if (now - lastSentAt < COOLDOWN_MS) return;
        lastSentAt = now;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        mc.execute(() -> {
            try {
                // 26.1 ServerboundChatPacket(String content, Instant timeStamp, long salt, byte[] signature,
                //                           boolean signedPreview, ...) — for unsigned vanilla chat
                // use the simple unsigned constructor where available; fall back to reflection.
                mc.player.connection.send(new ServerboundChatPacket("?", Instant.now(), 0L, null, 0, new java.util.BitSet()));
            } catch (Throwable t) {
                ZenithClient.LOGGER.debug("[Failsafe] chat '?' send failed", t);
            }
        });
    }
}
