package com.zenith.client.macro.dev;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.interaction.CommandSender;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.GUIItemMatcher;
import com.zenith.client.core.interaction.GUISlotFinder;
import com.zenith.client.core.timer.DelayManager;
import com.zenith.client.devdata.DevDataHarvester;
import com.zenith.client.macro.MacroModule;
import net.minecraft.client.Minecraft;

/**
 * Developer-only macro that autonomously tours SkyBlock to feed the passive
 * {@link DevDataHarvester}. Rather than collecting data from a single farming
 * session, it teleports to common warps, opens the Auction House and Bazaar
 * (including sub-pages such as Manage Auctions and the first Farming category
 * product), records scoreboard/action-bar/chat/boss-bar/tab data at each stop,
 * then returns to the private island.
 *
 * <p><b>Design:</b>
 * <ul>
 *   <li>Never clicks items to buy/sell/list — opens menus so the passive
 *       harvester can snapshot their slot layouts, then closes them.</li>
 *   <li>Uses humanised delays ({@link DelayManager}) per rule §5 — no
 *       {@code Thread.sleep}.</li>
 *   <li>State machine per rule §6; transitions driven from {@link #onTick()}.</li>
 *   <li>Can be started from the dashboard or {@code .z devdata tour}.</li>
 * </ul>
 *
 * <p>This macro does NOT move the player or rotate the camera; it only sends
 * chat commands and closes screens. It is safe to run on a freshly-logged in
 * character in the Hub. Pathfinding/NPC walks are a future extension.
 */
public final class DevDataMacro extends MacroModule {

    public static final DevDataMacro INSTANCE = new DevDataMacro();

    /**
     * Tour stops. Each stop is a step in the state machine.
     */
    private enum Step {
        START,
        ISLAND,
        WAIT_ISLAND,
        HUB,
        WAIT_HUB,
        WARP_BARN, WARP_BARN_WAIT,
        WARP_PARK, WARP_PARK_WAIT,
        WARP_DEEP, WARP_DEEP_WAIT,
        WARP_GOLD, WARP_GOLD_WAIT,
        WARP_SPIDER, WARP_SPIDER_WAIT,
        WARP_END, WARP_END_WAIT,
        WARP_MINES, WARP_MINES_WAIT,
        WARP_DA, WARP_DA_WAIT,
        WARP_MUSEUM, WARP_MUSEUM_WAIT,
        WARP_WIZARD, WARP_WIZARD_WAIT,
        WARP_CRYPT, WARP_CRYPT_WAIT,
        OPEN_AH, AH_WAIT_BROWSER,
        AH_MANAGE, AH_WAIT_MANAGE,
        AH_CLOSE_MANAGE, AH_CLOSE_MANAGE_WAIT,
        AH_SEARCH, AH_WAIT_SEARCH,
        AH_CLOSE_SEARCH_WAIT, AH_CLOSE_SEARCH,
        OPEN_BZ, BZ_WAIT_CATALOG,
        BZ_OPEN_CATEGORY, BZ_WAIT_CATEGORY,
        BZ_OPEN_PRODUCT, BZ_WAIT_PRODUCT,
        BZ_CLOSE,
        DONE,
    }

    private static final String DELAY_TAG = "devdata.tour";
    private static final long WARP_WAIT_MS = 3_500L;
    private static final long MENU_OPEN_WAIT_MS = 2_000L;
    private static final long MENU_BACK_WAIT_MS = 1_200L;

    private Step step = Step.START;
    private long stepStartedAt;
    private int attemptsThisStep;
    private String firstCategoryName = null;
    private int firstCategorySlot = -1;
    private String firstProductName = null;
    private int firstProductSlot = -1;

    private final GUISlotFinder finder = new GUISlotFinder();
    private final com.zenith.client.core.interaction.GUIClickExecutor clicker =
            new com.zenith.client.core.interaction.GUIClickExecutor();

    private DevDataMacro() {}

    // --- MacroModule metadata ---

    @Override public String id()           { return "dev:devdata"; }
    @Override public String displayName()  { return "Dev Data Tour"; }
    @Override public String icon()         { return "📝"; }
    @Override public String skillFamily()  { return "Developer"; }

    @Override public double destX() { return pX(); }
    @Override public double destY() { return pY(); }
    @Override public double destZ() { return pZ(); }
    @Override public String destinationDescription() { return "Dev Data Tour (no anchor)"; }

    // --- Lifecycle ---

    @Override
    protected void onStart() {
        DevDataHarvester.getInstance().setEnabled(true);
        DevDataHarvester.getInstance().note("=== Dev Data Tour started ===");
        step = Step.START;
        attemptsThisStep = 0;
        firstCategoryName = null; firstCategorySlot = -1;
        firstProductName = null; firstProductSlot = -1;
        advance(Step.ISLAND);
        ZenithChat.getInstance().info("Dev data tour started — passive harvester ON.");
    }

    @Override
    protected void onTick() {
        DevDataHarvester h = DevDataHarvester.getInstance();
        // The passive 2.5-second snapshot timer fires from DevDataHarvester itself; here we
        // only advance the tour state machine when per-step delays have elapsed.
        switch (step) {
            case START -> advance(Step.ISLAND);

            // --- Island / Hub baseline --------------------------------------
            case ISLAND -> {
                CommandSender.send("/is");
                DelayManager.getInstance().reset(DELAY_TAG, WARP_WAIT_MS);
                h.note("→ /is (private island baseline)");
                advance(Step.WAIT_ISLAND);
            }
            case WAIT_ISLAND -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    advance(Step.HUB);
                }
            }
            case HUB -> {
                CommandSender.send("/hub");
                DelayManager.getInstance().reset(DELAY_TAG, WARP_WAIT_MS);
                h.note("→ /hub (hub baseline)");
                advance(Step.WAIT_HUB);
            }
            case WAIT_HUB -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    advance(Step.WARP_BARN);
                }
            }

            // --- Warps: send /warp X, wait, snapshot, next ---------------
            case WARP_BARN, WARP_PARK, WARP_DEEP, WARP_GOLD, WARP_SPIDER, WARP_END,
                 WARP_MINES, WARP_DA, WARP_MUSEUM, WARP_WIZARD, WARP_CRYPT -> {
                String warp = warpNameFor(step);
                CommandSender.send("/warp " + warp);
                DelayManager.getInstance().reset(DELAY_TAG, WARP_WAIT_MS);
                h.note("→ /warp " + warp + " (scoreboard/location snapshot)");
                Step next = nextWarp(step);
                advanceWithState(Step.valueOf(step.name() + "_WAIT"), next);
                // Store the "next" step as user-object via field:
                pendingAfterWait = next;
            }
            case WARP_BARN_WAIT, WARP_PARK_WAIT, WARP_DEEP_WAIT, WARP_GOLD_WAIT, WARP_SPIDER_WAIT,
                 WARP_END_WAIT, WARP_MINES_WAIT, WARP_DA_WAIT, WARP_MUSEUM_WAIT, WARP_WIZARD_WAIT,
                 WARP_CRYPT_WAIT -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    h.flush();
                    advance(pendingAfterWait != null ? pendingAfterWait : Step.OPEN_AH);
                    pendingAfterWait = null;
                }
            }

            // --- Auction House: browser → manage → search sign → close ---
            case OPEN_AH -> {
                closeAnyOpenGui();
                CommandSender.send("/ah");
                DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                h.note("→ /ah (Auction Browser snapshot)");
                advance(Step.AH_WAIT_BROWSER);
            }
            case AH_WAIT_BROWSER -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    advance(Step.AH_MANAGE);
                }
            }
            case AH_MANAGE -> {
                // Click "Manage Auctions" (golden horse armor).
                if (attemptsThisStep == 0 || DelayManager.getInstance().isReady(DELAY_TAG)) {
                    GUIState s = GUIParser.getInstance().read();
                    int slot = finder.findFirst(GUIItemMatcher.nameContains("Manage Auctions"), s);
                    if (slot >= 0) {
                        h.note("AH Manage Auctions found at slot " + slot + "; clicking to open.");
                        clicker.leftClick(slot);
                        DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                        advance(Step.AH_WAIT_MANAGE);
                        return;
                    }
                    if (++attemptsThisStep > 6) {
                        h.note("AH Manage Auctions not found; skipping.");
                        advance(Step.AH_CLOSE_MANAGE);
                        return;
                    }
                    DelayManager.getInstance().reset(DELAY_TAG, 500);
                }
            }
            case AH_WAIT_MANAGE -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    advance(Step.AH_CLOSE_MANAGE);
                }
            }
            case AH_CLOSE_MANAGE -> {
                closeAnyOpenGui();
                DelayManager.getInstance().reset(DELAY_TAG, MENU_BACK_WAIT_MS);
                advance(Step.AH_CLOSE_MANAGE_WAIT);
            }
            case AH_CLOSE_MANAGE_WAIT -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) advance(Step.AH_SEARCH);
            }
            case AH_SEARCH -> {
                // Re-open AH browser then click the search icon to capture the sign prompt.
                String title = h.currentScreenTitle();
                if (!title.contains("Auction")) {
                    if (attemptsThisStep == 0) {
                        CommandSender.send("/ah");
                        DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                        attemptsThisStep++;
                    } else if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                        // Waited for the open; re-enter this step.
                        attemptsThisStep = 0;
                    }
                    return;
                }
                GUIState s = GUIParser.getInstance().read();
                // The search item is typically a sign/oak sign head named "Search" at the browser top bar.
                int slot = finder.findFirst(GUIItemMatcher.nameContains("Search"), s);
                if (slot < 0) slot = finder.findFirst(GUIItemMatcher.nameContains("Auctions Browser"), s);
                if (slot >= 0) {
                    h.note("AH Search found at slot " + slot + "; clicking for sign prompt.");
                    clicker.leftClick(slot);
                    DelayManager.getInstance().reset(DELAY_TAG, 1_500L);
                    advance(Step.AH_WAIT_SEARCH);
                } else if (++attemptsThisStep > 6) {
                    h.note("AH Search item not found; skipping sign capture.");
                    advance(Step.AH_CLOSE_SEARCH);
                } else {
                    DelayManager.getInstance().reset(DELAY_TAG, 500);
                }
            }
            case AH_WAIT_SEARCH -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    // SignEditScreen will have been captured by DevDataHarvester's onTick hook.
                    // Close the sign (ESC) so we don't accidentally search anything.
                    closeAnyOpenGui();
                    DelayManager.getInstance().reset(DELAY_TAG, MENU_BACK_WAIT_MS);
                    advance(Step.AH_CLOSE_SEARCH_WAIT);
                }
            }
            case AH_CLOSE_SEARCH_WAIT -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) advance(Step.AH_CLOSE_SEARCH);
            }
            case AH_CLOSE_SEARCH -> {
                closeAnyOpenGui();
                DelayManager.getInstance().reset(DELAY_TAG, MENU_BACK_WAIT_MS);
                advance(Step.OPEN_BZ);
            }

            // --- Bazaar: catalog → first category → first product → close --
            case OPEN_BZ -> {
                CommandSender.send("/bz");
                DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                h.note("→ /bz (Bazaar catalog snapshot)");
                advance(Step.BZ_WAIT_CATALOG);
            }
            case BZ_WAIT_CATALOG -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    advance(Step.BZ_OPEN_CATEGORY);
                }
            }
            case BZ_OPEN_CATEGORY -> {
                GUIState s = GUIParser.getInstance().read();
                if (s.title == null || !s.title.startsWith("Bazaar")) {
                    // BZ didn't open; retry a couple of times.
                    if (attemptsThisStep == 0 || DelayManager.getInstance().isReady(DELAY_TAG)) {
                        if (++attemptsThisStep > 3) { advance(Step.BZ_CLOSE); return; }
                        CommandSender.send("/bz");
                        DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                    }
                    return;
                }
                attemptsThisStep = 0;
                // Find first non-button category item after the top-bar icons (which occupy
                // the first few slots). Scan from slot 9 onwards to skip top-bar controls.
                int startFrom = Math.max(9, s.playerInventoryStart > 0 ? Math.min(9, s.playerInventoryStart) : 9);
                for (int i = startFrom; i < s.stacks.size(); i++) {
                    var st = s.stacks.get(i);
                    if (st == null) continue;
                    if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break; // stop at inv
                    if (st.displayName() == null || st.displayName().isBlank()) continue;
                    if (isBazaarTopBarItem(st.displayName())) continue;
                    firstCategoryName = st.displayName();
                    firstCategorySlot = i;
                    break;
                }
                if (firstCategorySlot < 0) {
                    h.note("BZ: no category item found; skipping.");
                    advance(Step.BZ_CLOSE);
                    return;
                }
                h.note("BZ category: clicking '" + firstCategoryName + "' at slot " + firstCategorySlot);
                clicker.leftClick(firstCategorySlot);
                DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                advance(Step.BZ_WAIT_CATEGORY);
            }
            case BZ_WAIT_CATEGORY -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    advance(Step.BZ_OPEN_PRODUCT);
                }
            }
            case BZ_OPEN_PRODUCT -> {
                GUIState s = GUIParser.getInstance().read();
                // On a Bazaar CATEGORY page products appear as item stacks below the top bar.
                int startFrom = Math.max(10, s.playerInventoryStart > 0 ? Math.min(10, s.playerInventoryStart) : 10);
                firstProductSlot = -1;
                for (int i = startFrom; i < s.stacks.size(); i++) {
                    var st = s.stacks.get(i);
                    if (st == null) continue;
                    if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break;
                    if (st.displayName() == null || st.displayName().isBlank()) continue;
                    if (isBazaarTopBarItem(st.displayName())) continue;
                    // Category pages can have sub-category headers; skip items whose lore indicates
                    // they are themselves a category (we pick the first one that looks like a product).
                    // Products have "Buy Instantly" / "Sell Instantly" lore.
                    boolean looksLikeProduct = false;
                    if (st.lore() != null) {
                        for (String line : st.lore()) {
                            if (line.contains("Buy") && line.contains("instantly")) { looksLikeProduct = true; break; }
                            if (line.contains("Sell") && line.contains("instantly")) { looksLikeProduct = true; break; }
                        }
                    }
                    if (!looksLikeProduct) continue;
                    firstProductName = st.displayName();
                    firstProductSlot = i;
                    break;
                }
                if (firstProductSlot < 0) {
                    // No product-looking entry found; click first non-top item anyway (may be nested category).
                    for (int i = startFrom; i < s.stacks.size(); i++) {
                        var st = s.stacks.get(i);
                        if (st == null) continue;
                        if (i >= s.playerInventoryStart && s.playerInventoryStart > 0) break;
                        if (st.displayName() == null || st.displayName().isBlank()) continue;
                        if (isBazaarTopBarItem(st.displayName())) continue;
                        firstProductName = st.displayName();
                        firstProductSlot = i;
                        break;
                    }
                }
                if (firstProductSlot < 0) {
                    h.note("BZ: no product item found in category '" + firstCategoryName + "'; closing.");
                    advance(Step.BZ_CLOSE);
                    return;
                }
                h.note("BZ product: clicking '" + firstProductName + "' at slot " + firstProductSlot);
                clicker.leftClick(firstProductSlot);
                DelayManager.getInstance().reset(DELAY_TAG, MENU_OPEN_WAIT_MS);
                advance(Step.BZ_WAIT_PRODUCT);
            }
            case BZ_WAIT_PRODUCT -> {
                if (DelayManager.getInstance().isReady(DELAY_TAG)) {
                    h.snapshotCurrentGui();
                    advance(Step.BZ_CLOSE);
                }
            }
            case BZ_CLOSE -> {
                closeAnyOpenGui();
                DelayManager.getInstance().reset(DELAY_TAG, MENU_BACK_WAIT_MS);
                h.flush();
                advance(Step.DONE);
            }

            case DONE -> {
                h.note("=== Dev Data Tour completed ===");
                h.flush();
                ZenithChat.getInstance().success("Dev data tour completed. Run `.z devdata flush` to sync file, then copy findings into docs/RESEARCH.md.");
                stop("tour complete");
            }
        }
    }

    @Override
    protected void onStop() {
        closeAnyOpenGui();
        step = Step.START;
        pendingAfterWait = null;
        attemptsThisStep = 0;
    }

    // --- Internal helpers ---

    private Step pendingAfterWait;

    private void advance(Step next) {
        step = next;
        stepStartedAt = System.currentTimeMillis();
        attemptsThisStep = 0;
        ZenithClient.LOGGER.debug("[DevDataTour] → {}", next);
    }

    private void advanceWithState(Step next, Step afterWait) {
        step = next;
        stepStartedAt = System.currentTimeMillis();
        attemptsThisStep = 0;
        pendingAfterWait = afterWait;
    }

    private static String warpNameFor(Step s) {
        return switch (s) {
            case WARP_BARN    -> "barn";
            case WARP_PARK    -> "park";
            case WARP_DEEP    -> "deep";
            case WARP_GOLD    -> "gold";
            case WARP_SPIDER  -> "spider";
            case WARP_END     -> "end";
            case WARP_MINES   -> "mines";
            case WARP_DA      -> "da";
            case WARP_MUSEUM  -> "museum";
            case WARP_WIZARD  -> "wizard";
            case WARP_CRYPT   -> "crypt";
            default -> "hub";
        };
    }

    private static Step nextWarp(Step s) {
        return switch (s) {
            case WARP_BARN   -> Step.WARP_PARK;
            case WARP_PARK   -> Step.WARP_DEEP;
            case WARP_DEEP   -> Step.WARP_GOLD;
            case WARP_GOLD   -> Step.WARP_SPIDER;
            case WARP_SPIDER -> Step.WARP_END;
            case WARP_END    -> Step.WARP_MINES;
            case WARP_MINES  -> Step.WARP_DA;
            case WARP_DA     -> Step.WARP_MUSEUM;
            case WARP_MUSEUM -> Step.WARP_WIZARD;
            case WARP_WIZARD -> Step.WARP_CRYPT;
            case WARP_CRYPT  -> Step.OPEN_AH;
            default         -> Step.OPEN_AH;
        };
    }

    private static boolean isBazaarTopBarItem(String name) {
        if (name == null) return false;
        String n = name.trim();
        // These are the known Bazaar top-bar control labels (confirmed for R042).
        return n.equalsIgnoreCase("Go Back")
                || n.equalsIgnoreCase("Manage Orders")
                || n.equalsIgnoreCase("Sell Inventory Now")
                || n.startsWith("Buy") && n.contains("Order")
                || n.startsWith("Sell") && n.contains("Order")
                || n.contains("➜");
    }

    private static void closeAnyOpenGui() {
        DevDataHarvester.getInstance().closeScreen();
    }

    private static double pX() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getX(); }
    private static double pY() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getY(); }
    private static double pZ() { var p = Minecraft.getInstance().player; return p == null ? 0 : p.getZ(); }
}
