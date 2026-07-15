package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.interaction.CommandSender;
import com.zenith.client.core.interaction.GUIWaiter;
import net.minecraft.client.Minecraft;

/**
 * Opens the Bazaar by sending {@code /bz}, then waits for the Bazaar GUI
 * ("Bazaar ➜") to appear.
 */
public final class BazaarNavigator {

    private static final BazaarNavigator INSTANCE = new BazaarNavigator();
    public static BazaarNavigator getInstance() { return INSTANCE; }

    private static final String BAZAAR_TITLE_PREFIX = "Bazaar";
    private long lastOpenAttemptAt;

    private BazaarNavigator() {}

    public void openBazaar() {
        long now = System.currentTimeMillis();
        if (now - lastOpenAttemptAt < 2_000) return;
        lastOpenAttemptAt = now;
        CommandSender.send("/bz");
        GUIWaiter.waitForTitle(BAZAAR_TITLE_PREFIX, 6_000L);
    }

    public boolean isInBazaar() {
        var mc = Minecraft.getInstance();
        if (mc.screen == null || mc.screen.getTitle() == null) return false;
        String t = mc.screen.getTitle().getString();
        return t.startsWith("Bazaar") || t.startsWith("Bazaar ➜") || t.contains("Order Options")
                || t.contains("Buy Order") || t.contains("Sell Order") || t.startsWith("How much");
    }
}
