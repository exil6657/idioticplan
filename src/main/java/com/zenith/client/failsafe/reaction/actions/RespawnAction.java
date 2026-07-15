package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/**
 * Instant respawn on Hypixel SkyBlock.
 *
 * <p>Research notes (R045): in most SkyBlock areas death shows no vanilla
 * DeathScreen — the player is teleported to area spawn / island with a chat
 * message; player.isAlive() stays true. In the rare cases where a DeathScreen
 * does appear (e.g. Dungeons / Crimson Isle fights), click the Respawn button
 * then call player.respawn() as a fallback.</p>
 */
public final class RespawnAction {

    private static boolean triggered;

    public static void trigger() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        triggered = true;
        ZenithClient.LOGGER.info("[Failsafe] instant respawn");
        mc.execute(() -> {
            try {
                if (mc.screen instanceof DeathScreen ds) {
                    for (var child : ds.children()) {
                        if (child instanceof net.minecraft.client.gui.components.Button b
                                && b.getMessage() != null
                                && b.getMessage().getString().toLowerCase().contains("respawn")) {
                            b.onPress();
                            return;
                        }
                    }
                }
                // In SkyBlock most deaths don't show a DeathScreen; respawn() will reconnect
                // if needed, otherwise it's a no-op.
                mc.player.respawn();
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Failsafe] respawn failed", t);
            }
        });
    }

    public static boolean consumeTriggered() {
        boolean t = triggered;
        triggered = false;
        return t;
    }
}
