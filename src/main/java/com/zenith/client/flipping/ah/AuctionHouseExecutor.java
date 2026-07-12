package com.zenith.client.flipping.ah;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.InventoryOpenEvent;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.SignInputHandler;
import com.zenith.client.core.interaction.skyblock.AuctionHouseGUI;
import com.zenith.client.core.interaction.skyblock.AuctionHouseNavigator;
import com.zenith.client.core.protection.BitsSpendBlocker;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.order.Order;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Drives a single buy→list order through the AH GUI. Uses a lightweight
 * state machine:
 * <pre>
 * IDLE → OPEN_AH → BROWSE_READY → TOGGLE_BIN → SEARCH → WAIT_RESULTS →
 * CLICK_LISTING → WAIT_CONFIRM → CLICK_BUY → WAIT_BOUGHT → OPEN_CREATE →
 * SET_PRICE → CLICK_CREATE → LISTED → IDLE
 * </pre>
 *
 * <p>All GUI clicks go through {@link com.zenith.client.core.interaction.GUIClickExecutor}
 * (humanised delays); all slot lookups go through {@link AuctionHouseGUI}
 * using {@link com.zenith.client.core.interaction.GUISlotFinder} (master rule §1).</p>
 */
public final class AuctionHouseExecutor {

    private static final AuctionHouseExecutor INSTANCE = new AuctionHouseExecutor();
    public static AuctionHouseExecutor getInstance() { return INSTANCE; }

    private final AuctionHouseGUI ah = new AuctionHouseGUI();
    private Order current = null;
    private long stateEnteredMs;
    private String state = "IDLE";
    private String pendingSearch = null;
    private int retries = 0;

    private AuctionHouseExecutor() {
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new Listener());
    }

    public boolean busy() { return current != null; }
    public String state() { return state; }

    public void buy(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[AH] busy, ignoring buy for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        transition("OPEN_AH");
        AuctionHouseNavigator.getInstance().openBrowser();
        retries = 0;
    }

    public void tick() {
        if (current == null) { state = "IDLE"; return; }
        GUIState s;
        try { s = GUIParser.getInstance().read(); } catch (Throwable t) { s = null; }
        if (s == null && !"OPEN_AH".equals(state)) {
            // GUI disappeared mid-order. Retry or fail.
            if (retries++ > 3) fail("gui closed");
            return;
        }
        if (s != null) ah.update(s);
        long now = System.currentTimeMillis();

        switch (state) {
            case "OPEN_AH" -> {
                if (AuctionHouseNavigator.getInstance().isInAH() && ah.detectPage(s) == AuctionHouseGUI.Page.BROWSER) {
                    transition("TOGGLE_BIN");
                } else if (now - stateEnteredMs > 6000) {
                    if (retries++ > 2) fail("timeout opening AH"); else AuctionHouseNavigator.getInstance().openBrowser();
                }
            }
            case "TOGGLE_BIN" -> {
                int bin = ah.findBinToggle(s);
                if (bin >= 0) { ah.clickBinToggle(s); transition("SEARCH"); }
                else if (now - stateEnteredMs > 1500) transition("SEARCH");
            }
            case "SEARCH" -> {
                int ss = ah.findSearchSlot(s);
                if (ss >= 0) {
                    pendingSearch = nameForSearch(current);
                    ah.clickSearch(s);
                    transition("WAIT_SIGN");
                } else if (now - stateEnteredMs > 3000) fail("search button missing");
            }
            case "WAIT_SIGN" -> {
                if (SignInputHandler.getInstance().isInSignScreen()) {
                    SignInputHandler.getInstance().requestType(pendingSearch);
                    transition("WAIT_RESULTS");
                } else if (now - stateEnteredMs > 5000) transition("WAIT_RESULTS");
            }
            case "WAIT_RESULTS" -> {
                List<AuctionHouseGUI.AuctionListing> listings = ah.findListings(s);
                if (now - stateEnteredMs > 6000) fail("search timeout");
                if (!listings.isEmpty()) {
                    // Pick the cheapest matching our candidate.
                    AuctionHouseGUI.AuctionListing best = null;
                    for (var l : listings) {
                        if (matches(l, current) && (best == null || l.price() < best.price())) best = l;
                    }
                    if (best != null && best.price() <= current.candidate.buyPrice * 1.05) {
                        ah.clickListing(s, best.slot());
                        transition("WAIT_CONFIRM");
                    } else {
                        fail("no matching cheap listing");
                    }
                }
            }
            case "WAIT_CONFIRM" -> {
                if (ah.detectPage(s) == AuctionHouseGUI.Page.CONFIRM_BUY) {
                    int buyBtn = ah.findBuyConfirmSlot(s);
                    if (buyBtn >= 0) {
                        // Bits blocker: double-check we're not spending bits.
                        if (!BitsSpendBlocker.isBlocked()) ah.confirmBuy(s);
                        transition("WAIT_BOUGHT");
                    }
                } else if (now - stateEnteredMs > 3000) fail("no confirm screen");
            }
            case "WAIT_BOUGHT" -> {
                // After buying, we return to the browser with the item in cursor/inventory.
                boolean inBrowser = ah.detectPage(s) == AuctionHouseGUI.Page.BROWSER || ah.detectPage(s) == AuctionHouseGUI.Page.RESULTS;
                if (inBrowser && now - stateEnteredMs > 1200) {
                    // Mark bought & move to listing — phase 10 v1 stops here (listing flow comes in next iteration).
                    com.zenith.client.flipping.order.OrderManager.getInstance().markBought(current);
                    ZenithChat.getInstance().success("Bought {} for {} coins (listing flow next phase).",
                            current.candidate.itemName, current.candidate.buyPrice);
                    current = null;
                    state = "IDLE";
                } else if (now - stateEnteredMs > 8000) fail("buy didn't complete");
            }
        }
    }

    private void transition(String to) {
        this.state = to;
        this.stateEnteredMs = System.currentTimeMillis();
        ZenithClient.LOGGER.debug("[AH] → {}", to);
    }

    private void fail(String reason) {
        ZenithClient.LOGGER.warn("[AH] order {} failed: {}", current != null ? current.itemId() : "?", reason);
        if (current != null) com.zenith.client.flipping.order.OrderManager.getInstance().fail(current, reason);
        current = null;
        state = "IDLE";
    }

    private boolean matches(AuctionHouseGUI.AuctionListing l, Order o) {
        // Match by name substring or skyblock id.
        if (l.skyblockId() != null && l.skyblockId().equalsIgnoreCase(o.itemId())) return true;
        String want = o.candidate.itemName.toLowerCase();
        return l.name() != null && l.name().toLowerCase().contains(want);
    }

    private String nameForSearch(Order o) {
        // Use the display name for AH search (not the internal id).
        return o.candidate.itemName == null || o.candidate.itemName.isEmpty() ? o.itemId() : o.candidate.itemName;
    }

    private final class Listener {
        @SubscribeEvent
        public void onInvOpen(InventoryOpenEvent ev) { /* reserved for future page-specific hooks */ }
    }
}
