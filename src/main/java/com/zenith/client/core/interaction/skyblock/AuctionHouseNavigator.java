package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.interaction.CommandSender;
import com.zenith.client.core.interaction.GUIWaiter;
import net.minecraft.client.Minecraft;

/**
 * Opens the Auction House by sending /ah, then waits for the Auction House GUI
 * to appear. Also handles returning to the AH lobby from any screen (e.g. the
 * create-listing screen, confirmation screen, collect screen).
 */
public final class AuctionHouseNavigator {

    private static final AuctionHouseNavigator INSTANCE = new AuctionHouseNavigator();
    public static AuctionHouseNavigator getInstance() { return INSTANCE; }

    private static final String AH_TITLE  = "Auctions";      // top-level browser
    private static final String AH_MAIN   = "Auctions Browser";
    private static final String AH_BUY    = "Confirm";        // buy confirmation
    private static final String AH_CREATE = "Create Auction";
    private static final String AH_SELL   = "Choose Item";
    private static final String AH_YOURS  = "Manage Auctions";

    private long lastOpenAttemptAt = 0;

    private AuctionHouseNavigator() {}

    /** Send /ah and wait for the browser to open. */
    public void openBrowser() {
        if (System.currentTimeMillis() - lastOpenAttemptAt < 2_000) return;
        lastOpenAttemptAt = System.currentTimeMillis();
        CommandSender.send("/ah");
        GUIWaiter.waitForTitle(AH_TITLE, 6_000L);
    }

    public boolean isInAH() {
        var mc = Minecraft.getInstance();
        if (mc.screen == null) return false;
        var t = mc.screen.getTitle();
        if (t == null) return false;
        String title = t.getString();
        return title.contains("Auction") || title.equals(AH_BUY) || title.equals(AH_CREATE)
                || title.equals(AH_SELL) || title.equals(AH_YOURS) || title.startsWith(AH_TITLE);
    }

    /** Press ESC to exit any sub-menu (if already in AH, no need to /ah again). */
    public void returnToBrowser() {
        if (!isInAH()) { openBrowser(); return; }
        // Close current screen to back out.
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().player.closeContainer());
    }
}
