package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.InventoryCloseEvent;
import com.zenith.client.core.event.events.InventoryOpenEvent;
import com.zenith.client.failsafe.FailsafeType;

/**
 * Triggers when a GUI the macro was interacting with closes unexpectedly.
 *
 * <p>The macro must call {@link #expectClose()} before performing an action
 * that legitimately closes a menu (e.g. buying an item, confirming a trade).
 * If a close event arrives outside that window we assume a kick/closed-by-staff
 * /wrong-click and notify.</p>
 */
public class GUICloseDetector extends AbstractDetector {

    private static long expectCloseUntil = 0;
    private long lastCloseMs;
    private String currentScreen;

    public static void expectClose() {
        expectCloseUntil = System.currentTimeMillis() + 1500L;
    }

    @SubscribeEvent
    public void onOpen(InventoryOpenEvent ev) {
        currentScreen = ev.getTitle();
    }

    @SubscribeEvent
    public void onClose(InventoryCloseEvent ev) {
        long now = System.currentTimeMillis();
        boolean expected = now <= expectCloseUntil;
        expectCloseUntil = 0;
        if (!expected && currentScreen != null) {
            lastCloseMs = now;
            trigger(FailsafeType.GUI_CLOSE, "closed " + ev.getTitle() + " (was in " + currentScreen + ")");
        }
        currentScreen = null;
    }

    @Override
    public void tick(long nowMs) {
        if ((nowMs - lastCloseMs) > 2500) clear(FailsafeType.GUI_CLOSE);
    }
}
