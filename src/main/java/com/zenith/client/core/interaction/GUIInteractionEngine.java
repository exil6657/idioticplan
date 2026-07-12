package com.zenith.client.core.interaction;

import com.zenith.client.core.ClientTickDispatcher;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.ClientTickEvent;

/**
 * Drives the GUI-interaction subsystem every client tick: parses the currently
 * open screen into {@link GUIState}, fires InventoryOpen/Close events, services
 * {@link SignInputHandler} and {@link GUIWaiter} timeouts, and advances any
 * queued click delays in {@link GUIClickExecutor}.
 *
 * <p>Registered from {@link ClientTickDispatcher}.</p>
 */
public final class GUIInteractionEngine {

    private static final GUIInteractionEngine INSTANCE = new GUIInteractionEngine();
    public static GUIInteractionEngine getInstance() { return INSTANCE; }

    private boolean registered;

    private GUIInteractionEngine() {}

    public void register() {
        if (registered) return;
        registered = true;
        ZenithEventBus.getInstance().register(new Listener());
    }

    void tick() {
        GUIParser.getInstance().tick();
        SignInputHandler.getInstance().tick();
        GUIWaiter.checkAll();
        GUIClickExecutor.tick();
    }

    private final class Listener {
        @SubscribeEvent
        public void onTick(ClientTickEvent ev) { tick(); }
    }
}
