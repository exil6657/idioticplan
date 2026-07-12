package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;

/**
 * Instant respawn on Hypixel SkyBlock "You died!" screens. In SkyBlock the
 * player is often teleported back to their spawn point rather than seeing a
 * real DeathScreen; in either case, macros should NOT stop — they should
 * respawn and then repath to the active macro destination.
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
                // If a DeathScreen is up, click it (player.isDead() / respawn).
                if (mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen ds) {
                    // Click the "Respawn" button.
                    for (var child : ds.children()) {
                        if (child instanceof net.minecraft.client.gui.components.Button b
                                && b.getMessage() != null
                                && b.getMessage().getString().toLowerCase().contains("respawn")) {
                            b.onPress();
                            return;
                        }
                    }
                }
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
