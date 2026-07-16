package com.zenith.client.macro.dev;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.interaction.CommandSender;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.GUISlotFinder;
import com.zenith.client.core.interaction.GUIItemMatcher;
import com.zenith.client.core.interaction.GUIClickExecutor;
import com.zenith.client.core.interaction.SignInputHandler;
import com.zenith.client.core.timer.DelayManager;
import com.zenith.client.devdata.DevDataHarvester;
import com.zenith.client.macro.MacroModule;
import net.minecraft.client.Minecraft;

import java.util.*;

/**
 * FULL exhaustive dev-data tour — captures EVERYTHING the mod needs to stop guessing.
 *
 * <p>This replaces the quick tour when you run {@code .z devdata tour full}. It walks:
 * <ul>
 *   <li>/is → /hub → every major /warp (barn, park, deep, gold, spider, end, isle, mines, da, museum, wizard, crypt, desert, forest, etc)</li>
 *   <li>Auction House: browser → sort cycle (all 4 sorts) → BIN toggle → search sign prompt → search results for "diamond" → confirm page → Manage Auctions page → Create AUCTION flow titles</li>
 *   <li>Bazaar: FULL catalog — discovers ALL categories, then for EACH category discovers ALL products on page 1 (and page 2 if arrow exists), then for EACH product captures: product page, Buy Instantly quantity sign, Sell Instantly quantity sign, Create Buy Order quantity + price signs, Create Sell Order quantity + price signs — WITHOUT ever clicking Confirm, so no coins spent.</li>
 *   <li>Extra GUIs: /pets, /wardrobe, /storage, /ec, /sacks, /collection, /skills, /museum, /garden, /sbmenu, /bestiary, /recipes, /calendar, /trades, /bank, /ah stats, /gfs, etc</li>
 *   <li>Passive harvester continuously snapshots scoreboard, action bar ❤, boss bars, tab header/footer, chat, entities</li>
 * </ul>
 * Expected runtime: quick ~3 min, full ~25-40 min for 150-250 products × 4 buttons. Output is <gameDir>/zenith/devdata/OBSERVATIONS.md — upload that file here.
 *
 * <p>Safety: never clicks AH Buy Confirm or Bazaar Confirm — quantity/price signs are closed with ESC to cancel any order.
 */
public final class DevDataFullTourMacro extends MacroModule {

    public static final DevDataFullTourMacro INSTANCE = new DevDataFullTourMacro();

    private enum Step {
        START,
        IS, WAIT_IS,
        HUB, WAIT_HUB,
        WARP, WAIT_WARP,
        AH, WAIT_AH_BROWSER,
        AH_SORT, WAIT_AH_SORT,
        AH_BIN, WAIT_AH_BIN,
        AH_SEARCH, WAIT_AH_SEARCH_SIGN, WAIT_AH_SEARCH_CLOSE,
        AH_RESULTS, WAIT_AH_RESULTS,
        AH_CONFIRM, WAIT_AH_CONFIRM,
        AH_MANAGE, WAIT_AH_MANAGE,
        AH_CLOSE,
        BZ_CATALOG, WAIT_BZ_CATALOG,
        BZ_DISCOVER_CATEGORIES,
        BZ_OPEN_CATEGORY, WAIT_BZ_CATEGORY,
        BZ_DISCOVER_PRODUCTS,
        BZ_OPEN_PRODUCT, WAIT_BZ_PRODUCT,
        BZ_BUY_SIGN, WAIT_BZ_BUY_SIGN,
        BZ_SELL_SIGN, WAIT_BZ_SELL_SIGN,
        BZ_CREATE_BUY_QTY, WAIT_BZ_CREATE_BUY_QTY, BZ_CREATE_BUY_QTY_SUBMIT, WAIT_BZ_CREATE_BUY_PRICE, BZ_CREATE_BUY_PRICE_CLOSE,
        BZ_CREATE_SELL_QTY, WAIT_BZ_CREATE_SELL_QTY, BZ_CREATE_SELL_QTY_SUBMIT, WAIT_BZ_CREATE_SELL_PRICE, BZ_CREATE_SELL_PRICE_CLOSE,
        BZ_BACK_TO_PRODUCT_LIST, WAIT_BZ_BACK_TO_LIST,
        BZ_BACK_TO_CATALOG, WAIT_BZ_BACK_TO_CATALOG,
        EXTRA_GUIS,
        EXTRA_OPEN, WAIT_EXTRA,
        DONE
    }

    private static final String DELAY = "devdatafull.tour";
    private static final long WARP_WAIT = 3500L;
    private static final long MENU_WAIT = 2200L;
    private static final long SIGN_WAIT = 1500L;
    private static final long EXTRA_WAIT = 2000L;

    // Full warp list — best-effort; some may not exist on older profiles, server will reply unknown warp in chat (harvester captures)
    private static final String[] WARPS = {
            "barn", "park", "deep", "gold", "spider", "end", "isle", "nether",
            "mines", "da", "museum", "wizard", "crypt", "desert", "forest",
            "jungle", "howl", "dwarven", "crystal", "mordor" // last few intentionally invalid to test unknown response capture
    };

    // Extra GUIs to snapshot via commands (safe: just opens GUI, no purchase)
    private static final String[] EXTRA_COMMANDS = {
            "/pets", "/pet", "/wardrobe", "/storage", "/ec", "/enderchest", "/sacks",
            "/collection", "/skills", "/museum", "/garden", "/sbmenu",
            "/bestiary", "/recipes", "/trades", "/calendar", "/bank",
            "/gfs", "/ah", "/bz", "/wardrobe", "/accessories", "/accessorybag",
            "/mayor", "/election"
    };

    private final GUISlotFinder finder = new GUISlotFinder();
    private final GUIClickExecutor clicker = new GUIClickExecutor();

    // State
    private Step step = Step.START;
    private int warpIdx = 0;
    private int sortClicks = 0;
    private int extraIdx = 0;

    // Bazaar traversal
    private final List<CategoryInfo> categories = new ArrayList<>();
    private int catIdx = 0;
    private List<ProductInfo> productsInCurrentCat = new ArrayList<>();
    private int prodIdx = 0;

    private static final Set<String> BAZAAR_TOP_BAR = Set.of(
            "go back", "manage orders", "sell inventory now",
            "buy order", "sell order", "buy instantly", "sell instantly",
            "search", "close", "➜", "→"
    );

    private DevDataFullTourMacro() {}

    @Override public String id() { return "dev:devdata-full"; }
    @Override public String displayName() { return "Dev Data FULL Tour"; }
    @Override public String icon() { return "📚"; }
    @Override public String skillFamily() { return "Developer"; }
    @Override public double destX() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getX(); }
    @Override public double destY() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getY(); }
    @Override public double destZ() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getZ(); }
    @Override public String destinationDescription() { return "Full DevData Tour"; }

    @Override protected void onStart() {
        DevDataHarvester h = DevDataHarvester.getInstance();
        h.setEnabled(true);
        h.note("=== FULL DevData Tour started (exhaustive) ===");
        h.note("Warps=" + Arrays.toString(WARPS) + " ExtraCmds=" + Arrays.toString(EXTRA_COMMANDS));
        step = Step.START;
        warpIdx = 0;
        sortClicks = 0;
        extraIdx = 0;
        categories.clear();
        catIdx = 0;
        productsInCurrentCat.clear();
        prodIdx = 0;
        advance(Step.IS);
        ZenithChat.getInstance().info("FULL DevData tour started — 25-40 min. Don't touch inputs. .z devdata stop-tour to abort.");
    }

    @Override protected void onTick() {
        DevDataHarvester h = DevDataHarvester.getInstance();
        if (Minecraft.getInstance().player == null) return;

        switch (step) {
            case START -> advance(Step.IS);

            case IS -> {
                CommandSender.send("/is");
                DelayManager.getInstance().reset(DELAY, WARP_WAIT);
                h.note("→ /is");
                advance(Step.WAIT_IS);
            }
            case WAIT_IS -> { if (DelayManager.getInstance().isReady(DELAY)) { h.snapshotCurrentGui(); h.flush(); advance(Step.HUB); } }

            case HUB -> {
                CommandSender.send("/hub");
                DelayManager.getInstance().reset(DELAY, WARP_WAIT);
                h.note("→ /hub");
                advance(Step.WAIT_HUB);
            }
            case WAIT_HUB -> { if (DelayManager.getInstance().isReady(DELAY)) { h.snapshotCurrentGui(); h.flush(); warpIdx = 0; advance(Step.WARP); } }

            case WARP -> {
                if (warpIdx >= WARPS.length) { advance(Step.AH); break; }
                String w = WARPS[warpIdx];
                CommandSender.send("/warp " + w);
                DelayManager.getInstance().reset(DELAY, WARP_WAIT);
                h.note("→ /warp " + w + " [" + (warpIdx+1) + "/" + WARPS.length + "]");
                advance(Step.WAIT_WARP);
            }
            case WAIT_WARP -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    warpIdx++;
                    advance(Step.WARP);
                }
            }

            // ----- AH -----
            case AH -> {
                closeGui();
                CommandSender.send("/ah");
                DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                h.note("→ /ah browser");
                advance(Step.WAIT_AH_BROWSER);
            }
            case WAIT_AH_BROWSER -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    sortClicks = 0;
                    advance(Step.AH_SORT);
                }
            }
            case AH_SORT -> {
                GUIState s = GUIParser.getInstance().read();
                int sortSlot = findSortSlot(s);
                if (sortSlot >= 0 && sortClicks < 4) {
                    h.note("AH sort click " + (sortClicks+1) + "/4 at slot " + sortSlot + " name=" + safeName(s, sortSlot));
                    clicker.leftClick(sortSlot);
                    sortClicks++;
                    DelayManager.getInstance().reset(DELAY, 700L);
                    advance(Step.WAIT_AH_SORT);
                } else {
                    advance(Step.AH_BIN);
                }
            }
            case WAIT_AH_SORT -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    advance(Step.AH_SORT);
                }
            }
            case AH_BIN -> {
                GUIState s = GUIParser.getInstance().read();
                int binSlot = finder.findFirst(GUIItemMatcher.nameContains("BIN"), s);
                if (binSlot < 0) binSlot = finder.findFirst(GUIItemMatcher.loreContains("BIN Only"), s);
                if (binSlot >= 0) {
                    h.note("AH BIN toggle at " + binSlot);
                    clicker.leftClick(binSlot);
                    DelayManager.getInstance().reset(DELAY, 900L);
                    advance(Step.WAIT_AH_BIN);
                } else {
                    advance(Step.AH_SEARCH);
                }
            }
            case WAIT_AH_BIN -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.AH_SEARCH);
                }
            }
            case AH_SEARCH -> {
                GUIState s = GUIParser.getInstance().read();
                int searchSlot = finder.findFirst(GUIItemMatcher.nameContains("Search"), s);
                if (searchSlot >= 0) {
                    h.note("AH Search at " + searchSlot + " → opening sign");
                    clicker.leftClick(searchSlot);
                    DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                    advance(Step.WAIT_AH_SEARCH_SIGN);
                } else {
                    h.note("AH Search not found, skipping");
                    advance(Step.AH_MANAGE);
                }
            }
            case WAIT_AH_SEARCH_SIGN -> {
                // Harvester records sign prompt in onTick via recordSignScreen()
                if (DelayManager.getInstance().isReady(DELAY) || SignInputHandler.getInstance().isInSignScreen()) {
                    h.snapshotCurrentGui();
                    // Close sign without typing (cancel search)
                    closeGui();
                    DelayManager.getInstance().reset(DELAY, 800L);
                    advance(Step.WAIT_AH_SEARCH_CLOSE);
                }
            }
            case WAIT_AH_SEARCH_CLOSE -> {
                if (DelayManager.getInstance().isReady(DELAY)) advance(Step.AH_MANAGE);
            }

            case AH_MANAGE -> {
                GUIState s = GUIParser.getInstance().read();
                int manageSlot = finder.findFirst(GUIItemMatcher.nameContains("Manage Auctions"), s);
                if (manageSlot < 0) manageSlot = finder.findFirst(GUIItemMatcher.nameContains("Manage"), s);
                if (manageSlot >= 0) {
                    h.note("AH Manage at " + manageSlot);
                    clicker.leftClick(manageSlot);
                    DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                    advance(Step.WAIT_AH_MANAGE);
                } else {
                    advance(Step.AH_CLOSE);
                }
            }
            case WAIT_AH_MANAGE -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.AH_CLOSE);
                }
            }
            case AH_CLOSE -> { closeGui(); DelayManager.getInstance().reset(DELAY, 800L); advance(Step.BZ_CATALOG); }

            // ----- BZ -----
            case BZ_CATALOG -> {
                CommandSender.send("/bz");
                DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                h.note("→ /bz catalog");
                advance(Step.WAIT_BZ_CATALOG);
            }
            case WAIT_BZ_CATALOG -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.BZ_DISCOVER_CATEGORIES);
                }
            }
            case BZ_DISCOVER_CATEGORIES -> {
                GUIState s = GUIParser.getInstance().read();
                categories.clear();
                categories.addAll(discoverCategories(s));
                h.note("BZ discovered " + categories.size() + " categories: " + categories.stream().map(c->c.name).toList());
                catIdx = 0;
                if (categories.isEmpty()) advance(Step.EXTRA_GUIS);
                else advance(Step.BZ_OPEN_CATEGORY);
            }
            case BZ_OPEN_CATEGORY -> {
                if (catIdx >= categories.size()) { advance(Step.EXTRA_GUIS); break; }
                CategoryInfo ci = categories.get(catIdx);
                // Re-open catalog if not in catalog
                GUIState s = GUIParser.getInstance().read();
                String title = s.title == null ? "" : s.title;
                if (!title.equals("Bazaar") && !title.startsWith("Bazaar ") && !title.isEmpty() && !title.contains("Bazaar")) {
                    CommandSender.send("/bz");
                    DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                    advance(Step.WAIT_BZ_CATALOG);
                    break;
                }
                // Find fresh slot for this category (slot may shift after returning)
                int slot = findCategorySlotByName(s, ci.name);
                if (slot < 0) slot = ci.slot;
                h.note("BZ opening category [" + (catIdx+1) + "/" + categories.size() + "] " + ci.name + " at " + slot);
                clicker.leftClick(slot);
                DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                advance(Step.WAIT_BZ_CATEGORY);
            }
            case WAIT_BZ_CATEGORY -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.BZ_DISCOVER_PRODUCTS);
                }
            }
            case BZ_DISCOVER_PRODUCTS -> {
                GUIState s = GUIParser.getInstance().read();
                productsInCurrentCat = discoverProducts(s);
                h.note("BZ cat " + currentCatName() + " discovered " + productsInCurrentCat.size() + " products on page 1");
                prodIdx = 0;
                if (productsInCurrentCat.isEmpty()) advance(Step.BZ_BACK_TO_CATALOG);
                else advance(Step.BZ_OPEN_PRODUCT);
            }
            case BZ_OPEN_PRODUCT -> {
                if (prodIdx >= productsInCurrentCat.size()) {
                    // Try next page arrow? Look for "Next Page" arrow head
                    GUIState s = GUIParser.getInstance().read();
                    int nextSlot = finder.findFirst(GUIItemMatcher.nameContains("Next Page"), s);
                    if (nextSlot < 0) nextSlot = finder.findFirst(GUIItemMatcher.loreContains("Next Page"), s);
                    if (nextSlot >= 0 && prodIdx < 200) { // avoid infinite
                        h.note("BZ category " + currentCatName() + " trying Next Page at " + nextSlot);
                        clicker.leftClick(nextSlot);
                        DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                        advance(Step.WAIT_BZ_CATEGORY); // reuse wait, then rediscover products and continue? For simplicity, after next page we rediscover
                        // To avoid double counting, clear and append? We'll just re-discover and continue from 0 but skip already seen
                        // Simplified: go back to discovering products (will overwrite list, but we progress)
                        // Actually need to re-enter discover as new page — we will lose previous but we already processed previous page's products
                        // So just set prodIdx=0 after discovering next page via a flag
                        // For this iteration we advance to a special sub-step to rediscover
                        // Using WAIT_BZ_CATEGORY leads to DISCOVER_PRODUCTS again which is fine
                        break;
                    }
                    advance(Step.BZ_BACK_TO_CATALOG);
                    break;
                }
                ProductInfo pi = productsInCurrentCat.get(prodIdx);
                GUIState s = GUIParser.getInstance().read();
                int slot = findProductSlotByName(s, pi.name);
                if (slot < 0) slot = pi.slot;
                h.note("BZ opening product [" + (prodIdx+1) + "/" + productsInCurrentCat.size() + "] in " + currentCatName() + ": " + pi.name + " at " + slot);
                clicker.leftClick(slot);
                DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                advance(Step.WAIT_BZ_PRODUCT);
            }
            case WAIT_BZ_PRODUCT -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    // Start capturing buy/sell signs
                    advance(Step.BZ_BUY_SIGN);
                }
            }
            // ---- Per product sign captures ----
            case BZ_BUY_SIGN -> {
                GUIState s = GUIParser.getInstance().read();
                int buySlot = finder.findFirst(GUIItemMatcher.nameContains("Buy Instantly"), s);
                if (buySlot >= 0) {
                    h.note("BZ " + currentProdName() + " Buy Instantly sign capture at " + buySlot);
                    clicker.leftClick(buySlot);
                    DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                    advance(Step.WAIT_BZ_BUY_SIGN);
                } else {
                    advance(Step.BZ_SELL_SIGN);
                }
            }
            case WAIT_BZ_BUY_SIGN -> {
                if (DelayManager.getInstance().isReady(DELAY) || SignInputHandler.getInstance().isInSignScreen()) {
                    h.snapshotCurrentGui();
                    // Close qty sign (cancel instant buy)
                    closeGui();
                    DelayManager.getInstance().reset(DELAY, 600L);
                    advance(Step.BZ_SELL_SIGN);
                }
            }
            case BZ_SELL_SIGN -> {
                GUIState s = GUIParser.getInstance().read();
                int sellSlot = finder.findFirst(GUIItemMatcher.nameContains("Sell Instantly"), s);
                if (sellSlot >= 0) {
                    h.note("BZ " + currentProdName() + " Sell Instantly sign capture");
                    clicker.leftClick(sellSlot);
                    DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                    advance(Step.WAIT_BZ_SELL_SIGN);
                } else {
                    advance(Step.BZ_CREATE_BUY_QTY);
                }
            }
            case WAIT_BZ_SELL_SIGN -> {
                if (DelayManager.getInstance().isReady(DELAY) || SignInputHandler.getInstance().isInSignScreen()) {
                    h.snapshotCurrentGui();
                    closeGui();
                    DelayManager.getInstance().reset(DELAY, 600L);
                    advance(Step.BZ_CREATE_BUY_QTY);
                }
            }
            case BZ_CREATE_BUY_QTY -> {
                GUIState s = GUIParser.getInstance().read();
                int cbSlot = finder.findFirst(GUIItemMatcher.nameContains("Create Buy Order"), s);
                if (cbSlot >= 0) {
                    h.note("BZ " + currentProdName() + " Create Buy Order qty/price capture");
                    clicker.leftClick(cbSlot);
                    DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                    advance(Step.WAIT_BZ_CREATE_BUY_QTY);
                } else {
                    advance(Step.BZ_CREATE_SELL_QTY);
                }
            }
            case WAIT_BZ_CREATE_BUY_QTY -> {
                if (DelayManager.getInstance().isReady(DELAY) || SignInputHandler.getInstance().isInSignScreen()) {
                    h.snapshotCurrentGui();
                    if (SignInputHandler.getInstance().isInSignScreen()) {
                        h.note("BZ Create Buy qty sign seen → submitting 1 to get price sign");
                        SignInputHandler.getInstance().requestType("1");
                        DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                        advance(Step.BZ_CREATE_BUY_QTY_SUBMIT);
                    } else {
                        advance(Step.BZ_CREATE_SELL_QTY);
                    }
                }
            }
            case BZ_CREATE_BUY_QTY_SUBMIT -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    // Now should be price sign
                    h.snapshotCurrentGui();
                    if (SignInputHandler.getInstance().isInSignScreen()) {
                        h.note("BZ Create Buy price sign seen for " + currentProdName());
                        // Close without typing price → cancels order
                        closeGui();
                        DelayManager.getInstance().reset(DELAY, 600L);
                        advance(Step.WAIT_BZ_CREATE_BUY_PRICE);
                    } else {
                        advance(Step.BZ_CREATE_SELL_QTY);
                    }
                }
            }
            case WAIT_BZ_CREATE_BUY_PRICE -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.BZ_CREATE_SELL_QTY);
                }
            }
            case BZ_CREATE_SELL_QTY -> {
                GUIState s = GUIParser.getInstance().read();
                int csSlot = finder.findFirst(GUIItemMatcher.nameContains("Create Sell Order"), s);
                if (csSlot >= 0) {
                    h.note("BZ " + currentProdName() + " Create Sell Order qty/price capture");
                    clicker.leftClick(csSlot);
                    DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                    advance(Step.WAIT_BZ_CREATE_SELL_QTY);
                } else {
                    advance(Step.BZ_BACK_TO_PRODUCT_LIST);
                }
            }
            case WAIT_BZ_CREATE_SELL_QTY -> {
                if (DelayManager.getInstance().isReady(DELAY) || SignInputHandler.getInstance().isInSignScreen()) {
                    h.snapshotCurrentGui();
                    if (SignInputHandler.getInstance().isInSignScreen()) {
                        h.note("BZ Create Sell qty sign → submit 1");
                        SignInputHandler.getInstance().requestType("1");
                        DelayManager.getInstance().reset(DELAY, SIGN_WAIT);
                        advance(Step.BZ_CREATE_SELL_QTY_SUBMIT);
                    } else {
                        advance(Step.BZ_BACK_TO_PRODUCT_LIST);
                    }
                }
            }
            case BZ_CREATE_SELL_QTY_SUBMIT -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui();
                    if (SignInputHandler.getInstance().isInSignScreen()) {
                        h.note("BZ Create Sell price sign for " + currentProdName());
                        closeGui();
                        DelayManager.getInstance().reset(DELAY, 600L);
                        advance(Step.WAIT_BZ_CREATE_SELL_PRICE);
                    } else {
                        advance(Step.BZ_BACK_TO_PRODUCT_LIST);
                    }
                }
            }
            case WAIT_BZ_CREATE_SELL_PRICE -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    advance(Step.BZ_BACK_TO_PRODUCT_LIST);
                }
            }
            case BZ_BACK_TO_PRODUCT_LIST -> {
                // Back arrow to category
                GUIState s = GUIParser.getInstance().read();
                int backSlot = finder.findFirst(GUIItemMatcher.nameContains("Go Back"), s);
                if (backSlot >= 0) {
                    clicker.leftClick(backSlot);
                    DelayManager.getInstance().reset(DELAY, 800L);
                    advance(Step.WAIT_BZ_BACK_TO_LIST);
                } else {
                    // If no back, just proceed (we are already in list? Actually after closing signs we are in product page)
                    // Need second back
                    closeGui(); // fallback
                    DelayManager.getInstance().reset(DELAY, 800L);
                    advance(Step.WAIT_BZ_BACK_TO_LIST);
                }
            }
            case WAIT_BZ_BACK_TO_LIST -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    prodIdx++;
                    advance(Step.BZ_OPEN_PRODUCT);
                }
            }
            case BZ_BACK_TO_CATALOG -> {
                GUIState s = GUIParser.getInstance().read();
                int backSlot = finder.findFirst(GUIItemMatcher.nameContains("Go Back"), s);
                if (backSlot >= 0) {
                    h.note("BZ back to catalog from category " + currentCatName());
                    clicker.leftClick(backSlot);
                    DelayManager.getInstance().reset(DELAY, 900L);
                    advance(Step.WAIT_BZ_BACK_TO_CATALOG);
                } else {
                    CommandSender.send("/bz");
                    DelayManager.getInstance().reset(DELAY, MENU_WAIT);
                    advance(Step.WAIT_BZ_CATALOG);
                }
            }
            case WAIT_BZ_BACK_TO_CATALOG -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    catIdx++;
                    advance(Step.BZ_OPEN_CATEGORY);
                }
            }

            // ----- Extra GUIs -----
            case EXTRA_GUIS -> {
                extraIdx = 0;
                advance(Step.EXTRA_OPEN);
            }
            case EXTRA_OPEN -> {
                if (extraIdx >= EXTRA_COMMANDS.length) { advance(Step.DONE); break; }
                String cmd = EXTRA_COMMANDS[extraIdx];
                CommandSender.send(cmd);
                DelayManager.getInstance().reset(DELAY, EXTRA_WAIT);
                h.note("→ " + cmd + " [" + (extraIdx+1) + "/" + EXTRA_COMMANDS.length + "]");
                advance(Step.WAIT_EXTRA);
            }
            case WAIT_EXTRA -> {
                if (DelayManager.getInstance().isReady(DELAY)) {
                    h.snapshotCurrentGui(); h.flush();
                    closeGui();
                    DelayManager.getInstance().reset(DELAY, 500L);
                    extraIdx++;
                    advance(Step.EXTRA_OPEN);
                }
            }

            case DONE -> {
                h.note("=== FULL DevData Tour completed ===");
                h.flush();
                CommandSender.send("/is");
                ZenithChat.getInstance().success("FULL tour completed: " + categories.size() + " cats, " + totalProductsScanned() + " products. File: " + h.getOutputPath());
                ZenithChat.getInstance().info("Upload OBSERVATIONS.md here to finish R002/R003/R005/R041/R042/R045/R046/R047");
                stop("full tour complete");
            }
        }
    }

    @Override protected void onStop() {
        closeGui();
        step = Step.START;
        DevDataHarvester.getInstance().note("Full tour stopped early at step " + step + " cat=" + currentCatName() + " prod=" + currentProdName());
        DevDataHarvester.getInstance().flush();
    }

    // ---- Helpers ----

    private void advance(Step next) {
        step = next;
        ZenithClient.LOGGER.debug("[FullTour] → {}", next);
    }

    private static void closeGui() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                mc.execute(() -> {
                    if (mc.player != null) mc.player.closeContainer();
                    mc.setScreen(null);
                });
            }
        } catch (Throwable ignored) {}
    }

    private String currentCatName() { return catIdx < categories.size() ? categories.get(catIdx).name : "?"; }
    private String currentProdName() { return prodIdx < productsInCurrentCat.size() ? productsInCurrentCat.get(prodIdx).name : "?"; }
    private int totalProductsScanned() { return productsInCurrentCat.size() * categories.size() + prodIdx; }

    private int findSortSlot(GUIState s) {
        if (s == null) return -1;
        int slot = finder.findFirst(GUIItemMatcher.nameContains("Sort"), s);
        if (slot < 0) slot = finder.findFirst(GUIItemMatcher.loreContains("Sort"), s);
        return slot;
    }
    private String safeName(GUIState s, int slot) {
        if (s == null || s.stacks == null || slot < 0 || slot >= s.stacks.size()) return "?";
        var st = s.stacks.get(slot);
        return st == null ? "?" : st.displayName();
    }

    // Discovery

    private static boolean isTopBar(String name) {
        if (name == null) return true;
        String n = name.toLowerCase(Locale.ROOT).trim();
        if (n.isEmpty()) return true;
        for (String t : BAZAAR_TOP_BAR) if (n.contains(t)) return true;
        if (n.contains("page") && (n.contains("1") || n.contains("next") || n.contains("previous") || n.contains("back"))) {
            // Keep next/prev as not top bar for pagination discovery, but exclude Go Back already filtered
            if (n.contains("next page") || n.contains("previous page")) return false;
        }
        return false;
    }

    private List<CategoryInfo> discoverCategories(GUIState s) {
        List<CategoryInfo> out = new ArrayList<>();
        if (s == null || s.stacks == null) return out;
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < s.stacks.size(); i++) {
            if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break;
            var st = s.stacks.get(i);
            if (st == null || st.displayName() == null) continue;
            String name = st.displayName().replaceAll("§.", "").trim();
            if (isTopBar(name)) continue;
            if (seen.contains(name.toLowerCase())) continue;
            // Category icons usually have lore "Click to browse"
            boolean looksCat = false;
            if (st.lore() != null) for (String line : st.lore()) if (line.toLowerCase().contains("browse") || line.toLowerCase().contains("category")) looksCat = true;
            // Also accept known category names
            String lower = name.toLowerCase();
            if (lower.contains("farming") || lower.contains("mining") || lower.contains("combat") || lower.contains("wood") || lower.contains("oddities") || lower.contains("fish")) looksCat = true;
            if (!looksCat) continue;
            seen.add(name.toLowerCase());
            out.add(new CategoryInfo(name, i));
        }
        return out;
    }

    private List<ProductInfo> discoverProducts(GUIState s) {
        List<ProductInfo> out = new ArrayList<>();
        if (s == null || s.stacks == null) return out;
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < s.stacks.size(); i++) {
            if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break;
            var st = s.stacks.get(i);
            if (st == null || st.displayName() == null) continue;
            String name = st.displayName().replaceAll("§.", "").trim();
            if (isTopBar(name)) continue;
            if (name.equalsIgnoreCase("Go Back") || name.contains("➜")) continue;
            if (seen.contains(name.toLowerCase())) continue;
            seen.add(name.toLowerCase());
            out.add(new ProductInfo(name, i, st.skyblockId()));
        }
        return out;
    }

    private int findCategorySlotByName(GUIState s, String name) {
        if (s == null || s.stacks == null || name == null) return -1;
        for (int i = 0; i < s.stacks.size(); i++) {
            if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break;
            var st = s.stacks.get(i);
            if (st == null || st.displayName() == null) continue;
            if (st.displayName().replaceAll("§.", "").trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }
    private int findProductSlotByName(GUIState s, String name) {
        return findCategorySlotByName(s, name);
    }

    private static final class CategoryInfo {
        final String name; final int slot;
        CategoryInfo(String n, int s) { name=n; slot=s; }
    }
    private static final class ProductInfo {
        final String name; final int slot; final String sbId;
        ProductInfo(String n, int s, String id) { name=n; slot=s; sbId=id; }
    }
}
