package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.BanDetectedEvent;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Detects ban/mute screens and Hypixel ban chat.
 *
 * <p>Hypixel sends ban screens as {@link DisconnectedScreen} with specific text;
 * we also listen for the {@link BanDetectedEvent} fired from chat pattern
 * matching ("You are banned", "Muted for Xm/h", etc.).</p>
 */
public class BanDetector extends AbstractDetector {

    private volatile boolean fired = false;

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        if (mc.screen instanceof DisconnectedScreen || mc.screen instanceof TitleScreen) {
            if (mc.screen instanceof DisconnectedScreen) {
                if (!fired) {
                    fired = true;
                    trigger(FailsafeType.BAN_DETECTED, "disconnect screen", FailsafeStrictness.DISCONNECT);
                }
            }
        } else {
            // Only reset fired flag if we're back in a world (avoids re-firing during the same disconnect).
            if (mc.player != null && mc.level != null && !(mc.screen instanceof DisconnectedScreen)) {
                fired = false;
            }
        }
    }

    @SubscribeEvent
    public void onBan(BanDetectedEvent ev) {
        fired = true;
        trigger(FailsafeType.BAN_DETECTED,
                (ev.isMute() ? "muted" : "banned") + ": " + ev.getReason(),
                FailsafeStrictness.DISCONNECT);
    }
}
