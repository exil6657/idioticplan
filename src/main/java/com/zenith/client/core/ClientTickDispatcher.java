package com.zenith.client.core;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.ClientTickEvent;
import com.zenith.client.core.module.ModuleManager;
import com.zenith.client.core.player.PlayerHealthMonitor;
import com.zenith.client.core.player.PlayerPositionTracker;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.path.learning.MovementLearner;
import com.zenith.client.core.interaction.GUIInteractionEngine;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.FlipEngine;
import com.zenith.client.macro.MacroManager;
import com.zenith.client.world.World;

/**
 * Central tick subscriber: dispatches the ClientTickEvent to every subsystem that
 * needs per-tick updates. Subscribed once during init.
 */
public final class ClientTickDispatcher {

    private static boolean registered;

    public static void register() {
        if (registered) return;
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new ClientTickDispatcher());
        GUIInteractionEngine.getInstance().register();
        registered = true;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        // Player state trackers require an active world.
        if (World.get().playerReady()) {
            PlayerPositionTracker.getInstance().tick();
            PlayerHealthMonitor.getInstance().tick();
        }
        ModuleManager.getInstance().tickAll();
        InputEngine.getInstance().tick();
        MovementLearner.getInstance().tick();
        FailsafeManager.getInstance().tick();
        MacroManager.getInstance().tick();
        FlipEngine.getInstance().tick();
    }
}
