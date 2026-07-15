package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.core.interaction.GUIClickExecutor;
import com.zenith.client.core.interaction.GUIItemMatcher;
import com.zenith.client.core.interaction.GUISlotFinder;
import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;
import com.zenith.client.core.interaction.GUIState;

import java.util.ArrayList;
import java.util.List;

/**
 * State-aware wrapper over the Auction House GUI. Tracks which page the player is
 * on (browser / search / manage / create / confirm) and provides rule §1-compliant
 * slot lookup for buttons/items.
 *
 * <p>Hypixel AH slot layout (54-slot double chest):
 * <ul>
 *   <li>Rows 1–4 (slots 0–35, with hot-bar at playerInventoryStart): auction result items.</li>
 *   <li>Top bar (around slot 47–53): navigation arrows, search (gold block with anvil), sort, BIN-only toggle.</li>
 *   <li>Close button at top-right (inventory close).</li>
 * </ul>
 * Rather than hard-coding these indices, we use GUIItemMatcher by name/lore.</p>
 */
public final class AuctionHouseGUI {

    /** Identifies which AH screen the player is on. */
    public enum Page {
        BROWSER,        // Main Auctions Browser — top-level (title "Auctions" or "Auctions Browser")
        SEARCH_SIGN,    // Typing in the sign input
        RESULTS,        // Search results or BIN-only list ("Auctions: \"<query>\"" or "Search:")
        CONFIRM_BUY,    // "Purchase item?" confirmation (title "Confirm")
        MANAGE,         // "Manage Auctions" / "Your Auctions"
        COLLECT,        // Sold/expired — collect items/coins
        CREATE,         // "Create Auction" price entry
        CHOOSE_ITEM,    // Pre-create item pick (inventory) — title "Choose Item"
        UNKNOWN
    }

    private final GUIClickExecutor clicker = new GUIClickExecutor();
    private final GUISlotFinder finder = new GUISlotFinder();

    private Page lastPage = Page.UNKNOWN;

    public Page detectPage(GUIState s) {
        if (s == null || s.title == null) return Page.UNKNOWN;
        String t = s.title;
        if (t.equals("Auctions") || t.equals("Auctions Browser")) return Page.BROWSER;
        if (t.equals("Confirm")) return Page.CONFIRM_BUY;
        if (t.equals("Manage Auctions") || t.equals("Your Auctions")) return Page.MANAGE;
        if (t.startsWith("Auctions:") || t.startsWith("Search:")) return Page.RESULTS;
        if (t.equals("Create Auction")) return Page.CREATE;
        if (t.equals("Choose Item")) return Page.CHOOSE_ITEM;
        if (t.contains("Collect")) return Page.COLLECT;
        return Page.UNKNOWN;
    }

    /** Finds the search button (display name contains "Search"). */
    public int findSearchSlot(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Search"), s);
    }

    /** Finds the BIN-only toggle (gold block/dye, lore says "BIN Only") in the BROWSER screen. */
    public int findBinToggle(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.loreContains("BIN Only"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("BIN Only"), s);
        return slot;
    }

    /**
     * Finds the "Create BIN Auction" / BIN-mode toggle on the Create Auction page.
     * Per wiki: a gold ingot next to an arrow toggles between normal auction and BIN.
     */
    public int findBinToggleOnCreate(GUIState s) {
        // Gold ingot on create page named after the BIN toggle; lore typically says "Buy it now" or "Switch".
        int slot = finder.findFirst(GUIItemMatcher.loreContains("Buy it now"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Buy it now"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("BIN"), s);
        return slot;
    }

    /** Finds the sort button. */
    public int findSortSlot(GUIState s) {
        return finder.findFirst(GUIItemMatcher.byNameContains("Sort"), s);
    }

    /** Finds the "Buy Item" / "Buy it now" button in the confirmation page. */
    public int findBuyConfirmSlot(GUIState s) {
        // Confirm screen has a "Buy Item" or "Buy it now" button; try exact first then contains.
        int slot = finder.findFirst(GUIItemMatcher.byName("Buy Item"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Buy"), s);
        return slot;
    }

    /** Finds the gold/diamond head labelled exactly "Manage Auctions". */
    public int findManageButton(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Manage Auctions"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Manage"), s);
        return slot;
    }

    /** Finds the "Create Auction" (regular auction) button. */
    public int findCreateButton(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Create Auction"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Create Auction"), s);
        return slot;
    }

    /** Finds the "Create BIN Auction" gold-ingot button. */
    public int findCreateBinButton(GUIState s) {
        int slot = finder.findFirst(GUIItemMatcher.byName("Create BIN Auction"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Create BIN"), s);
        return slot;
    }

    /** Finds the "Go Back" arrow (name contains "Back"). */
    public int findBackButton(GUIState s) {
        return finder.findFirst(GUIItemMatcher.nameContains("Back"), s);
    }

    /**
     * Find a player-inventory slot that matches a skyblock id OR display-name substring.
     * Used in CHOOSE_ITEM / CREATE to locate the item we're about to list.
     */
    public int findItemInInventory(GUIState s, String skyblockId, String displayNamePart) {
        if (s == null || s.stacks == null) return -1;
        for (int i = s.playerInventoryStart; i < s.stacks.size(); i++) {
            GUIItemStack stack = s.stacks.get(i);
            if (stack == null) continue;
            if (skyblockId != null && !skyblockId.isEmpty()
                    && skyblockId.equalsIgnoreCase(stack.skyblockId())) return i;
            if (displayNamePart != null && !displayNamePart.isEmpty() && stack.displayName() != null
                    && stack.displayName().toLowerCase().contains(displayNamePart.toLowerCase())) return i;
        }
        return -1;
    }

    /** Returns slots that appear to be auction items (stacks with "Buy it now" / "Current Bid" in lore). */
    public List<AuctionListing> findListings(GUIState s) {
        List<AuctionListing> out = new ArrayList<>();
        if (s == null || s.stacks == null) return out;
        for (int i = 0; i < s.stacks.size(); i++) {
            GUIItemStack stack = s.stacks.get(i);
            if (stack == null) continue;
            if (i >= s.playerInventoryStart) break; // stop at player inv
            boolean isBin = stackHasLoreContaining(stack, "Buy it now");
            boolean isAuctionHead = stackHasLoreContaining(stack, "Current Bid") || isBin;
            if (!isAuctionHead) continue;
            long price = parseBinPrice(stack);
            if (price <= 0) price = parseBidPrice(stack);
            out.add(new AuctionListing(i, stack.displayName(), stack.skyblockId(), price, isBin, stack.stackSize()));
        }
        return out;
    }

    // ---- Click helpers (all through GUIClickExecutor — rule §1 + humanised delays §5) ----

    public void clickSearch(GUIState s)   { int slot = findSearchSlot(s);   if (slot >= 0) clicker.leftClick(slot); }
    public void clickBinToggle(GUIState s){ int slot = findBinToggle(s);    if (slot >= 0) clicker.leftClick(slot); }
    public void clickSort(GUIState s)     { int slot = findSortSlot(s);     if (slot >= 0) clicker.leftClick(slot); }
    public void clickListing(GUIState s, int slot) { clicker.leftClick(slot); }
    public void confirmBuy(GUIState s)    { int slot = findBuyConfirmSlot(s); if (slot >= 0) clicker.leftClick(slot); }
    public void clickBack(GUIState s)     { int slot = findBackButton(s);   if (slot >= 0) clicker.leftClick(slot); }
    public void clickManage(GUIState s)   { int slot = findManageButton(s); if (slot >= 0) clicker.leftClick(slot); }
    public void clickCreate(GUIState s)   { int slot = findCreateButton(s); if (slot >= 0) clicker.leftClick(slot); }
    public void clickCreateBin(GUIState s){ int slot = findCreateBinButton(s); if (slot >= 0) clicker.leftClick(slot); }
    public void clickBinToggleOnCreate(GUIState s) { int slot = findBinToggleOnCreate(s); if (slot >= 0) clicker.leftClick(slot); }
    public void clickInventorySlot(GUIState s, int slot) { clicker.leftClick(slot); }

    public Page lastPage() { return lastPage; }

    public void update(GUIState s) { lastPage = detectPage(s); }

    // ---- Internals ----

    private boolean stackHasLoreContaining(GUIItemStack s, String substr) {
        if (s == null || s.lore == null) return false;
        for (String line : s.lore) if (line.contains(substr)) return true;
        return false;
    }

    private long parseBinPrice(GUIItemStack s) {
        if (s == null || s.lore == null) return -1L;
        for (String line : s.lore) {
            String plain = line.replaceAll("§.", "").replace(",", "").trim();
            if (plain.startsWith("Buy it now:")) return parseCoins(plain.substring("Buy it now:".length()).trim());
            if (plain.startsWith("Price:"))      return parseCoins(plain.substring("Price:".length()).trim());
            if (plain.startsWith("Buy it now ")) return parseCoins(plain.substring("Buy it now".length()).trim());
        }
        return -1L;
    }

    private long parseBidPrice(GUIItemStack s) {
        if (s == null || s.lore == null) return -1L;
        for (String line : s.lore) {
            String plain = line.replaceAll("§.", "").replace(",", "").trim();
            if (plain.startsWith("Top bid:"))     return parseCoins(plain.substring("Top bid:".length()).trim());
            if (plain.startsWith("Current bid:")) return parseCoins(plain.substring("Current bid:".length()).trim());
        }
        return -1L;
    }

    /** Parses "12.3M" / "450k" / "125" coin strings. */
    public static long parseCoins(String text) {
        if (text == null) return -1L;
        String t = text.replace("coins", "").replace("coin", "").trim();
        if (t.isEmpty()) return -1L;
        try {
            char last = Character.toUpperCase(t.charAt(t.length() - 1));
            double mult = 1d;
            String num = t;
            if (last == 'K' || last == 'M' || last == 'B') {
                num = t.substring(0, t.length() - 1);
                mult = switch (last) {
                    case 'K' -> 1_000d;
                    case 'M' -> 1_000_000d;
                    case 'B' -> 1_000_000_000d;
                    default -> 1d;
                };
            }
            return (long) (Double.parseDouble(num) * mult);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    public record AuctionListing(int slot, String name, String skyblockId, long price, boolean bin, int count) {}
}
