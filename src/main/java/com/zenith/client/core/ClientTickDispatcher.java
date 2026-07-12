package com.zenith.client.core;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.ClientTickEvent;
import com.zenith.client.core.module.ModuleManager;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.path.learning.MovementLearner;

/**
 * Central tick subscriber that dispatches the ClientTickEvent to subsystems.
 *
 * <p>Subscribed to the event bus during ZenithClient init. Keeps per-tick wiring
 * in one place instead of scattered across the mod entrypoint.</p>
 */
public final class ClientTickDispatcher {

    private static boolean registered;

    public static void register() {
        if (registered) return;
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new ClientTickDispatcher());
        registered = true;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        ModuleManager.getInstance().tickAll();
        InputEngine.getInstance().tick();
        MovementLearner.getInstance().tick();
    }
}
