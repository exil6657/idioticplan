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
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.order.OrderState;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Drives a single buy→list order through the AH GUI. State machine:
 * <pre>
 * IDLE
 *  ↓
 * OPEN_AH → TOGGLE_BIN → SEARCH → WAIT_SIGN → WAIT_RESULTS
 *  ↓ (cheapest matching listing)
 * WAIT_CONFIRM → CLICK_BUY → WAIT_BOUGHT
 *  ↓ (item in inventory)
 * OPEN_MANAGE → WAIT_MANAGE → CLICK_CREATE_HEAD → WAIT_CHOOSE_ITEM
 *  ↓ (click item in player inv)
 * SET_PRICE (sign) → WAIT_CREATE → CLICK_CREATE → WAIT_LISTED → IDLE
 * </pre>
 *
 * <p>All GUI clicks go through {@link com.zenith.client.core.interaction.GUIClickExecutor}
 * (humanised delays); all slot lookups go through {@link AuctionHouseGUI}
 * using {@link com.zenith.client.core.interaction.GUISlotFinder} (master rule §1).
 * Sign entry goes through {@link SignInputHandler} (reflection-based).</p>
 *
 * <p>Listing notes (Hypixel AH): after opening /ah, the "Manage Auctions" head
 * is in the top row of the browser page. Clicking it takes you to "Manage
 * Auctions"; the "Create Auction" head there opens the "Choose Item" screen
 * (player inventory). Clicking an item opens the "Create Auction" screen
 * where you click the BIN price sign, type the price, confirm, and click
 * "Create Auction" to list.</p>
 */
public final class AuctionHouseExecutor {

    private static final AuctionHouseExecutor INSTANCE = new AuctionHouseExecutor();
    public static AuctionHouseExecutor getInstance() { return INSTANCE; }

    private final AuctionHouseGUI ah = new AuctionHouseGUI();
    private Order current = null;
    private long stateEnteredMs;
    private String state = "IDLE";
    private String pendingSearch = null;
    private String pendingPrice = null;
    private int retries = 0;

    private AuctionHouseExecutor() {
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new Listener());
    }

    public boolean busy() { return current != null; }
    public String state() { return state; }

    /** Queue a BUY for the given order. */
    public void buy(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[AH] busy, ignoring buy for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        transition("OPEN_AH");
        AuctionHouseNavigator.getInstance().openBrowser();
        retries = 0;
    }

    /**
     * Queue a LIST for a held item (post-buy, or a manual ".z flip list" for items
     * already in inventory). If the order is still HOLDING we navigate to /ah
     * and walk the list flow directly.
     */
    public void list(Order o) {
        if (busy()) { ZenithClient.LOGGER.warn("[AH] busy, ignoring list for {}", o.itemId()); return; }
        if (FailsafeManager.getInstance().areMacrosPaused()) return;
        this.current = o;
        // If we're already in the AH, skip straight to OPEN_MANAGE; else open browser.
        if (AuctionHouseNavigator.getInstance().isInAH()) {
            transition("OPEN_MANAGE");
        } else {
            transition("OPEN_AH_FOR_LIST");
            AuctionHouseNavigator.getInstance().openBrowser();
        }
        retries = 0;
    }

    public void tick() {
        if (current == null) { state = "IDLE"; return; }
        GUIState s;
        try { s = GUIParser.getInstance().read(); } catch (Throwable t) { s = null; }
        if (s == null && !state.startsWith("OPEN_")) {
            if (retries++ > 3) fail("gui closed");
            return;
        }
        if (s != null) ah.update(s);
        long now = System.currentTimeMillis();

        switch (state) {
            // ---- BUY PATH ----
            case "OPEN_AH" -> {
                if (AuctionHouseNavigator.getInstance().isInAH() && s != null
                        && ah.detectPage(s) == AuctionHouseGUI.Page.BROWSER) {
                    transition("TOGGLE_BIN");
                } else if (now - stateEnteredMs > 6000) {
                    if (retries++ > 2) fail("timeout opening AH");
                    else AuctionHouseNavigator.getInstance().openBrowser();
                }
            }
            case "TOGGLE_BIN" -> {
                if (s == null) break;
                int bin = ah.findBinToggle(s);
                if (bin >= 0) { ah.clickBinToggle(s); transition("SORT"); }
                else transition("SORT");
            }
            case "SORT" -> {
                if (s == null) break;
                // Critical profitability fix: ensure cheapest first sort.
                // The sort button cycles; we click until lore says "Lowest Price" or "Price: Low -> High".
                int sortSlot = ah.findSortSlot(s);
                if (sortSlot >= 0) {
                    // Check lore if already low->high, if not click and wait 400ms then re-check
                    var stack = s.stacks != null && sortSlot < s.stacks.size() ? s.stacks.get(sortSlot) : null;
                    boolean isLowHigh = false;
                    if (stack != null && stack.lore() != null) {
                        for (String line : stack.lore()) {
                            String ll = line.toLowerCase();
                            if (ll.contains("lowest") || ll.contains("low to high") || ll.contains("price: low") || ll.contains("cheapest")) {
                                // The lore often shows "Currently: ..." or "Sorted by: ..."
                                // If lore contains indicator that low->high is selected, consider sorted.
                                // Heuristic: if lore contains arrow down or "Low" in name
                            }
                        }
                        // Also check display name contains "Lowest" ?
                        if (stack.displayName() != null && stack.displayName().toLowerCase().contains("lowest")) isLowHigh = true;
                    }
                    // Simple strategy: click once per 500ms up to 4 clicks, then proceed.
                    // DevData will capture exact name, but this ensures we at least attempt low-price sort.
                    if (!isLowHigh && now - stateEnteredMs < 3000 && (now - stateEnteredMs) % 600 < 100) {
                        ah.clickSort(s);
                        // stay in SORT to re-evaluate
                        break;
                    }
                }
                // After sort attempts, go to search
                if (now - stateEnteredMs > 800) transition("SEARCH");
            }
            case "SEARCH" -> {
                if (s == null) break;
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
                if (s == null) break;
                if (now - stateEnteredMs > 6000) { fail("search timeout"); break; }
                List<AuctionHouseGUI.AuctionListing> listings = ah.findListings(s);
                if (!listings.isEmpty()) {
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
                if (s == null) break;
                if (ah.detectPage(s) == AuctionHouseGUI.Page.CONFIRM_BUY) {
                    // Profitability guard: re-parse confirm screen price from lore
                    // If price > 105% of expected, abort to avoid overpay.
                    try {
                        var listings = ah.findListings(s);
                        // Confirm screen price is usually in lore of confirm button or middle slot
                        long confirmPrice = -1;
                        for (int i = 0; i < s.stacks.size(); i++) {
                            var st = s.stacks.get(i);
                            if (st == null) continue;
                            // Look for any stack that has "Buy it now:" price
                            for (String line : st.lore() != null ? st.lore() : java.util.List.<String>of()) {
                                String plain = line.replaceAll("§.", "").replace(",", "").trim();
                                if (plain.startsWith("Buy it now:") || plain.toLowerCase().contains("buy it now")) {
                                    confirmPrice = AuctionHouseGUI.parseCoins(plain.replace("Buy it now:", "").trim());
                                    if (confirmPrice > 0) break;
                                }
                            }
                            if (confirmPrice > 0) break;
                        }
                        if (confirmPrice > 0 && current != null && current.candidate != null) {
                            long maxAllowed = (long)(current.candidate.buyPrice * 1.05);
                            if (confirmPrice > maxAllowed) {
                                ZenithClient.LOGGER.warn("[AH] confirm price {} > maxAllowed {} — aborting to avoid overpay", confirmPrice, maxAllowed);
                                fail("confirm price over 105% expected (" + confirmPrice + " > " + maxAllowed + ")");
                                break;
                            }
                        }
                    } catch (Throwable ignored) {}
                    int buyBtn = ah.findBuyConfirmSlot(s);
                    if (buyBtn >= 0) {
                        if (!BitsSpendBlocker.isBlocked()) ah.confirmBuy(s);
                        transition("WAIT_BOUGHT");
                    }
                } else if (now - stateEnteredMs > 3000) fail("no confirm screen");
            }
            case "WAIT_BOUGHT" -> {
                if (s == null) break;
                boolean inBrowserOrResults = ah.detectPage(s) == AuctionHouseGUI.Page.BROWSER
                        || ah.detectPage(s) == AuctionHouseGUI.Page.RESULTS;
                // Wait 1.2 s for the confirm to fire and for us to land back on browser/results
                // with the item in inventory (or on cursor). Then immediately continue into the listing flow.
                if (inBrowserOrResults && now - stateEnteredMs > 1200) {
                    OrderManager.getInstance().markBought(current);
                    ZenithChat.getInstance().success("Bought {} for {} coins — now listing.",
                            current.candidate.itemName, current.candidate.buyPrice);
                    transition("OPEN_MANAGE");
                } else if (now - stateEnteredMs > 8000) fail("buy didn't complete");
            }

            // ---- OPEN FOR LIST (when we entered via list() directly) ----
            case "OPEN_AH_FOR_LIST" -> {
                if (AuctionHouseNavigator.getInstance().isInAH() && s != null
                        && ah.detectPage(s) == AuctionHouseGUI.Page.BROWSER) {
                    transition("OPEN_MANAGE");
                } else if (now - stateEnteredMs > 6000) {
                    if (retries++ > 2) fail("timeout opening AH for list");
                    else AuctionHouseNavigator.getInstance().openBrowser();
                }
            }

            // ---- LIST PATH ----
            case "OPEN_MANAGE" -> {
                if (s == null) {
                    if (now - stateEnteredMs > 4000) fail("not in ah when opening manage");
                    break;
                }
                AuctionHouseGUI.Page page = ah.detectPage(s);
                if (page == AuctionHouseGUI.Page.BROWSER || page == AuctionHouseGUI.Page.RESULTS) {
                    // Click the Manage Auctions head on the browser page.
                    int m = ah.findManageButton(s);
                    if (m >= 0) {
                        ah.clickManage(s);
                        transition("WAIT_MANAGE");
                    } else if (now - stateEnteredMs > 3000) fail("manage button missing from browser");
                } else if (page == AuctionHouseGUI.Page.MANAGE) {
                    transition("CLICK_CREATE_HEAD");
                } else if (page == AuctionHouseGUI.Page.CHOOSE_ITEM || page == AuctionHouseGUI.Page.CREATE) {
                    // Already in the listing flow (race from previous tick); carry on.
                    transition("WAIT_CHOOSE_ITEM");
                } else if (now - stateEnteredMs > 5000) {
                    fail("unexpected page opening manage: " + page);
                }
            }
            case "WAIT_MANAGE" -> {
                if (s == null) break;
                if (ah.detectPage(s) == AuctionHouseGUI.Page.MANAGE) {
                    transition("CLICK_CREATE_HEAD");
                } else if (now - stateEnteredMs > 4000) fail("manage page didn't open");
            }
            case "CLICK_CREATE_HEAD" -> {
                if (s == null) break;
                // Per wiki: from the Manage Auctions page, click the "Create BIN Auction"
                // gold ingot (not the regular "Create Auction" horse armor which starts a
                // normal auction). Fall back to the generic create button if BIN isn't found.
                int cb = ah.findCreateBinButton(s);
                if (cb < 0) cb = ah.findCreateButton(s);
                if (cb >= 0) {
                    ah.clickCreateBin(s);
                    transition("WAIT_CHOOSE_ITEM");
                } else if (now - stateEnteredMs > 3000) {
                    fail("create-auction button missing on manage page");
                }
            }
            case "WAIT_CHOOSE_ITEM" -> {
                if (s == null) break;
                AuctionHouseGUI.Page page = ah.detectPage(s);
                if (page == AuctionHouseGUI.Page.CHOOSE_ITEM) {
                    // Find the item in player inventory by skyblock id or name.
                    int slot = ah.findItemInInventory(s, current.itemId(), current.candidate.itemName);
                    if (slot >= 0) {
                        ah.clickInventorySlot(s, slot);
                        transition("WAIT_CREATE");
                    } else if (now - stateEnteredMs > 4000) {
                        // Give up — item not found in inventory; fail the list portion.
                        // Don't fail the whole order though — mark as HOLDING so manual list works.
                        ZenithClient.LOGGER.warn("[AH] item {} not found in inventory after buy; leaving as HOLDING.", current.itemId());
                        current.transition(OrderState.HOLDING);
                        current = null;
                        state = "IDLE";
                    }
                } else if (page == AuctionHouseGUI.Page.CREATE) {
                    transition("SET_PRICE");
                } else if (now - stateEnteredMs > 4000) fail("choose-item page didn't open");
            }
            case "WAIT_CREATE" -> {
                if (s == null) break;
                if (ah.detectPage(s) == AuctionHouseGUI.Page.CREATE) {
                    // Ensure BIN mode is selected (gold ingot toggle).
                    // Note: the "Create BIN Auction" button leads directly to the
                    // "Choose Item" screen; the price sign appears after choosing an item.
                    transition("SET_PRICE");
                } else if (now - stateEnteredMs > 3000) fail("create page didn't open after choosing item");
            }
            case "SET_PRICE" -> {
                if (s == null) break;
                // On the Create BIN Auction page (after selecting an item), Hypixel shows a
                // Gold Ingot (sign) for setting the BIN price. Lore contains "Buy it now"
                // or "Price per unit". Click it; SignInputHandler types the price.
                // RESEARCH: exact BIN page item labels are approximate; match by lore/name
                // containing "Buy it now", "Price", or per the wiki the gold bar sets the
                // minimum (bid) price; for BIN auctions the gold block next to the arrow
                // sets the BIN price.
                int priceSign = findPriceSign(s);
                if (priceSign >= 0) {
                    pendingPrice = formatPrice(current.listPrice > 0 ? current.listPrice : current.candidate.sellPrice);
                    ah.clickInventorySlot(s, priceSign);
                    transition("WAIT_PRICE_SIGN");
                } else if (SignInputHandler.getInstance().isInSignScreen()) {
                    pendingPrice = formatPrice(current.listPrice > 0 ? current.listPrice : current.candidate.sellPrice);
                    SignInputHandler.getInstance().requestType(pendingPrice);
                    transition("WAIT_PRICE_SUBMIT");
                } else if (now - stateEnteredMs > 4000) {
                    // [RESEARCH NEEDED] exact price sign slot/name for BIN creation.
                    // The button names/lore on the Create BIN Auction page need verification
                    // in a dev environment. For now fail and log.
                    fail("price sign not found on BIN create page [RESEARCH]");
                }
            }
            case "WAIT_PRICE_SIGN" -> {
                if (SignInputHandler.getInstance().isInSignScreen()) {
                    if (pendingPrice != null) SignInputHandler.getInstance().requestType(pendingPrice);
                    transition("WAIT_PRICE_SUBMIT");
                } else if (now - stateEnteredMs > 3000) {
                    // Maybe the sign was skipped? Move on to click Create.
                    transition("CLICK_LIST");
                }
            }
            case "WAIT_PRICE_SUBMIT" -> {
                // After submitting the sign we're back to CREATE screen with price filled in.
                if (s != null && ah.detectPage(s) == AuctionHouseGUI.Page.CREATE
                        && !SignInputHandler.getInstance().isInSignScreen()
                        && now - stateEnteredMs > 800) {
                    transition("CLICK_LIST");
                } else if (now - stateEnteredMs > 5000) {
                    // Maybe sign auto-submitted; move along.
                    transition("CLICK_LIST");
                }
            }
            case "CLICK_LIST" -> {
                if (s == null) break;
                int create = ah.findCreateButton(s);
                if (create >= 0) {
                    // For BIN listings the "Create Auction" button finalises; ensure BIN is toggled.
                    // (Create screen doesn't always present a separate toggle — Hypixel uses the
                    // price sign to set BIN price vs starting bid.)
                    ah.clickCreate(s);
                    transition("WAIT_LISTED");
                } else if (now - stateEnteredMs > 4000) fail("create/list button missing on price page");
            }
            case "WAIT_LISTED" -> {
                if (s == null) break;
                AuctionHouseGUI.Page page = ah.detectPage(s);
                boolean done = page == AuctionHouseGUI.Page.BROWSER
                        || page == AuctionHouseGUI.Page.MANAGE
                        || page == AuctionHouseGUI.Page.RESULTS;
                if (done && now - stateEnteredMs > 1500) {
                    long listPrice = current.listPrice > 0 ? current.listPrice : current.candidate.sellPrice;
                    OrderManager.getInstance().markListed(current, listPrice);
                    ZenithChat.getInstance().success("Listed {} for {} coins.",
                            current.candidate.itemName, listPrice);
                    current = null;
                    state = "IDLE";
                } else if (now - stateEnteredMs > 10000) fail("listing didn't complete");
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
        if (current != null) OrderManager.getInstance().fail(current, reason);
        current = null;
        pendingSearch = null;
        pendingPrice = null;
        state = "IDLE";
    }

    private boolean matches(AuctionHouseGUI.AuctionListing l, Order o) {
        if (l.skyblockId() != null && l.skyblockId().equalsIgnoreCase(o.itemId())) return true;
        String want = o.candidate.itemName == null ? "" : o.candidate.itemName.toLowerCase();
        return l.name() != null && !want.isEmpty() && l.name().toLowerCase().contains(want);
    }

    private String nameForSearch(Order o) {
        return o.candidate.itemName == null || o.candidate.itemName.isEmpty() ? o.itemId() : o.candidate.itemName;
    }

    /**
     * Format a coin value for AH sign input. Hypixel accepts plain integers
     * (with commas stripped) and K/M/B suffixes; we use the plain integer
     * to avoid suffix-parsing edge cases.
     */
    private static String formatPrice(long coins) {
        return Long.toString(coins);
    }

    /**
     * Find the "Buy it now" / price sign on the Create Auction page. Scans
     * the container for an item whose display name or lore contains "Buy it now"
     * or "Price" (rule §1 — no hardcoded slot indices).
     */
    private int findPriceSign(GUIState s) {
        if (s == null || s.stacks == null) return -1;
        for (int i = 0; i < s.stacks.size(); i++) {
            var st = s.stacks.get(i);
            if (st == null) continue;
            if (st.displayName() != null
                    && (st.displayName().toLowerCase().contains("buy it now")
                        || st.displayName().toLowerCase().contains("price per item")
                        || st.displayName().toLowerCase().contains("price:"))) {
                return i;
            }
            if (st.lore() != null) {
                for (String line : st.lore()) {
                    String ll = line.toLowerCase();
                    if (ll.contains("buy it now") || ll.contains("set the price")) return i;
                }
            }
        }
        return -1;
    }

    private final class Listener {
        @SubscribeEvent
        public void onInvOpen(InventoryOpenEvent ev) { /* reserved for future page-specific hooks */ }
    }
}
