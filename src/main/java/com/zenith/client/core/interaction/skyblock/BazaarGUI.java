package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.core.interaction.GUIClickExecutor;
import com.zenith.client.core.interaction.GUIItemMatcher;
import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;
import com.zenith.client.core.interaction.GUISlotFinder;
import com.zenith.client.core.interaction.GUIState;

import java.util.ArrayList;
import java.util.List;

/**
 * State-aware wrapper over the Hypixel Bazaar GUI. Identifies pages:
 * <ul>
 *   <li>CATALOG — the top-level Bazaar "Browse categories" page.</li>
 *   <li>CATEGORY — a specific category page (e.g. "Farming", "Mining").</li>
 *   <li>PRODUCT — a product page showing Buy Instantly / Sell Instantly /
 *       Create Buy Order / Create Sell Order.</li>
 *   <li>QUANTITY_SIGN / PRICE_SIGN — sign input for quantity/price.</li>
 *   <li>CONFIRM — "Buy" / "Sell" confirmation pane.</li>
 *   <li>MANAGE_ORDERS — "Manage Orders" page showing outstanding buy/sell orders.</li>
 *   <li>UNKNOWN</li>
 * </ul>
 *
 * <p>All slot lookups go through {@link GUISlotFinder} (rule §1).</p>
 */
public final class BazaarGUI {

    public enum Page {
        CATALOG,
        CATEGORY,
        PRODUCT,
        QUANTITY_SIGN,
        PRICE_SIGN,
        CONFIRM_BUY,
        CONFIRM_SELL,
        MANAGE_ORDERS,
        UNKNOWN
    }

    private final GUIClickExecutor clicker = new GUIClickExecutor();
    private final GUISlotFinder finder = new GUISlotFinder();
    private Page lastPage = Page.UNKNOWN;

    public Page detectPage(GUIState s) {
        if (s == null || s.title == null) return Page.UNKNOWN;
        String t = s.title;
        if (t.startsWith("Bazaar ➜") && !t.contains("Buy") && !t.contains("Sell")) {
            // "Bazaar ➜ Farming" is a category; "Bazaar ➜ Wheat" is a product.
            // Distinguish by presence of product-specific buy/sell buttons.
            if (hasProductButtons(s)) return Page.PRODUCT;
            return Page.CATEGORY;
        }
        if (t.equals("Bazaar") || t.startsWith("Bazaar ")) return Page.CATALOG;
        if (t.startsWith("Buy ") && t.contains("instantly")) return Page.CONFIRM_BUY;
        if (t.startsWith("Sell ") && t.contains("instantly")) return Page.CONFIRM_SELL;
        if (t.contains("Manage Orders")) return Page.MANAGE_ORDERS;
        if (t.startsWith("How much do you want")) return Page.QUANTITY_SIGN;
        if (t.startsWith("At what price")) return Page.PRICE_SIGN;
        return Page.UNKNOWN;
    }

    /** Finds the "Buy Instantly" button on a product page. */
    public int findBuyInstantly(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Buy Instantly"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Buy Instantly"), s);
        return slot;
    }

    /** Finds the "Sell Instantly" button on a product page. */
    public int findSellInstantly(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Sell Instantly"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Sell Instantly"), s);
        return slot;
    }

    /** Finds the "Create Buy Order" button. */
    public int findCreateBuyOrder(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Create Buy Order"), s);
    }

    /** Finds the "Create Sell Order" button. */
    public int findCreateSellOrder(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Create Sell Order"), s);
    }

    /** Finds the "Manage Orders" button (top of catalog page). */
    public int findManageOrders(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Manage Orders"), s);
    }

    /** Finds the "Go Back" arrow. */
    public int findBackButton(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Back"), s);
    }

    /** Finds the confirmation button (usually a stained-glass "Confirm"). */
    public int findConfirmButton(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Confirm"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Confirm"), s);
        return slot;
    }

    /** Finds a product entry by skyblock id or display name (in category/product pages). */
    public int findProduct(GUIState s, String productId, String displayName) {
        if (s == null || s.stacks == null) return -1;
        for (int i = 0; i < Math.min(s.playerInventoryStart, s.stacks.size()); i++) {
            GUIItemStack stack = s.stacks.get(i);
            if (stack == null) continue;
            if (productId != null && productId.equalsIgnoreCase(stack.skyblockId())) return i;
            if (displayName != null && stack.displayName() != null
                    && stack.displayName().toLowerCase().contains(displayName.toLowerCase())) return i;
        }
        return -1;
    }

    /** Parse a lore line like "Price per unit: 12.5k coins" → 12_500. */
    public static long parsePriceLine(String line) {
        if (line == null) return -1L;
        String plain = line.replaceAll("§.", "").replace(",", "").trim();
        int colon = plain.indexOf(':');
        String after = colon >= 0 ? plain.substring(colon + 1) : plain;
        after = after.replace("coins", "").replace("coin", "").trim();
        return AuctionHouseGUI.parseCoins(after);
    }

    // ---- Click helpers ----

    public void clickBuyInstantly(GUIState s)  { int sl = findBuyInstantly(s);  if (sl >= 0) clicker.leftClick(sl); }
    public void clickSellInstantly(GUIState s) { int sl = findSellInstantly(s); if (sl >= 0) clicker.leftClick(sl); }
    public void clickCreateBuyOrder(GUIState s){ int sl = findCreateBuyOrder(s); if (sl >= 0) clicker.leftClick(sl); }
    public void clickCreateSellOrder(GUIState s){int sl = findCreateSellOrder(s);if (sl >= 0) clicker.leftClick(sl);}
    public void clickManageOrders(GUIState s)  { int sl = findManageOrders(s);  if (sl >= 0) clicker.leftClick(sl); }
    public void clickBack(GUIState s)          { int sl = findBackButton(s);   if (sl >= 0) clicker.leftClick(sl); }
    public void clickConfirm(GUIState s)       { int sl = findConfirmButton(s);if (sl >= 0) clicker.leftClick(sl); }
    public void clickProduct(GUIState s, int slot) { clicker.leftClick(slot); }

    public Page lastPage() { return lastPage; }
    public void update(GUIState s) { lastPage = detectPage(s); }

    // ---- Internals ----

    private boolean hasProductButtons(GUIState s) {
        return findBuyInstantly(s) >= 0 || findSellInstantly(s) >= 0;
    }
}
