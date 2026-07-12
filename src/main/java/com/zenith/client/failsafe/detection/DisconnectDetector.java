package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.DisconnectEvent;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;

/**
 * Detects disconnection — either through a {@link DisconnectEvent} from chat
 * patterns or a {@link DisconnectedScreen} appearing.
 *
 * <p>Triggers PAUSE, not DISCONNECT (we're already disconnected). Halts all
 * macro activity so that on reconnect we don't immediately resume.</p>
 */
public class DisconnectDetector extends AbstractDetector {

    private boolean wasDisconnected = false;

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent ev) {
        trigger(FailsafeType.CUSTOM, "disconnected: " + ev.getReason(), FailsafeStrictness.PAUSE);
    }

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        boolean disconnected = mc.screen instanceof DisconnectedScreen || (mc.getConnection() == null);
        if (disconnected && !wasDisconnected) {
            trigger(FailsafeType.CUSTOM, "client disconnect screen", FailsafeStrictness.PAUSE);
        }
        if (!disconnected && wasDisconnected) {
            clear(FailsafeType.CUSTOM);
        }
        wasDisconnected = disconnected;
    }
}
