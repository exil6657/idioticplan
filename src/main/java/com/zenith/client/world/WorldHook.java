package com.zenith.client.world;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.WorldChangeEvent;
import com.zenith.client.core.event.events.DisconnectEvent;
import net.minecraft.client.Minecraft;

/**
 * Installs/resets the MCWorldAdapter on world join/disconnect. Subscribes to
 * events fired by the packet/tick mixins. Also sets up the adapter immediately
 * at client init if a world already exists (single-player / integrated server).
 */
public final class WorldHook {

    private static boolean installed;

    public static void init() {
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new WorldHook());
        // If we're already in-game (singleplayer), install immediately.
        if (Minecraft.getInstance().level != null) install();
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent e) { install(); }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent e) { World.reset(); installed = false; }

    private void install() {
        World.install(MCWorldAdapter.INSTANCE);
        installed = true;
    }

    public static boolean isInstalled() { return installed; }
}
