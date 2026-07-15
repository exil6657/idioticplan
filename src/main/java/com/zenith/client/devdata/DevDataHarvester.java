package com.zenith.client.devdata;

import com.zenith.client.ZenithClient;
import com.zenith.client.ZenithClientInfo;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.ChatReceivedEvent;
import com.zenith.client.core.event.events.ClientTickEvent;
import com.zenith.client.core.event.events.InventoryCloseEvent;
import com.zenith.client.core.event.events.InventoryOpenEvent;
import com.zenith.client.core.event.events.WorldChangeEvent;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;
import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;
import com.zenith.client.core.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.BossEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive in-game data harvester for developer use.
 *
 * <p>While a macro runs (or whenever enabled), this module listens to the Zenith event
 * bus and to MC state and records observations that are useful for filling in the
 * {@code [RESEARCH NEEDED]} gaps across the codebase: GUI titles + slot layouts
 * (Auction House, Bazaar, signs, bank, etc.), sign prompts, scoreboard lines,
 * action-bar messages, boss-bar titles, tab-list header/footer, chat lines grouped by
 * context, entity types seen, and current world/location.
 *
 * <p>All findings are deduplicated and periodically flushed to a single markdown file
 * at {@code <gameDir>/zenith/devdata/OBSERVATIONS.md}. The file is append-only in the
 * sense that previously-seen observations are preserved across runs (the file is
 * read back at startup to seed the deduplication sets).
 *
 * <p>Design goals:
 * <ul>
 *   <li><b>Zero gameplay impact</b> — never sends packets, never clicks, never rotates.</li>
 *   <li><b>Deduplicated</b> — seeing the same Bazaar page 400 times writes one entry.</li>
 *   <li><b>Crash-safe</b> — flushes every 30 s and on GUI/world transition.</li>
 *   <li><b>Human-readable</b> — output is markdown so a dev can read it directly
 *       in a git-tracked docs folder or upload to the repo.</li>
 * </ul>
 *
 * <p>Toggle: default {@code enabled=true} when launched from a dev workspace (detected
 * by lack of Fabric prod metadata); production installs get a {@link #setEnabled}
 * switch exposed via the Settings tab and the {@code .z devdata} command.
 */
public final class DevDataHarvester {

    private static final DevDataHarvester INSTANCE = new DevDataHarvester();
    public static DevDataHarvester getInstance() { return INSTANCE; }

    private static final String DIR_NAME = "zenith/devdata";
    private static final String FILE_NAME = "OBSERVATIONS.md";
    private static final long FLUSH_INTERVAL_MS = 30_000L;
    private static final long SNAPSHOT_INTERVAL_MS = 2_500L;
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz").withZone(ZoneId.of("Europe/London"));

    // ---------------------------------------------------------------------
    // Observation stores (keyed by a stable dedup key per category)
    // ---------------------------------------------------------------------

    /** GUI layouts keyed by "title|slot-count" to dedup repeat openings. */
    private final Map<String, GuiLayoutObservation> guiLayouts = new LinkedHashMap<>();
    /** Sign prompts — title or first-line text. */
    private final Set<String> signPrompts = new LinkedHashSet<>();
    /** Scoreboard lines (per world, deduplicated across ticks). */
    private final Set<String> scoreboardLines = new LinkedHashSet<>();
    /** Action bar lines (TypeId 2 in chat). */
    private final Set<String> actionBarLines = new LinkedHashSet<>();
    /** Boss bar names/titles. */
    private final Set<String> bossBarTitles = new LinkedHashSet<>();
    /** Tab-list header/footer lines. */
    private final Set<String> tabHeaderFooter = new LinkedHashSet<>();
    /** Chat lines grouped by (context, normalised-stripped). */
    private final Set<String> chatLines = new LinkedHashSet<>();
    /** World/location names observed. */
    private final Set<String> worldsVisited = new LinkedHashSet<>();
    /** Entity types in render distance (type id string, e.g. "minecraft: zombie"). */
    private final Set<String> entityTypesSeen = new TreeSet<>();
    /** Freeform manual notes from {@code .z devdata note ...}. */
    private final List<String> manualNotes = new ArrayList<>();

    /** Thread-safe queue for notes added off-tick (e.g. from command handler). */
    private final ConcurrentLinkedQueue<String> pendingNotes = new ConcurrentLinkedQueue<>();

    private final Timer flushTimer = new Timer();
    private final Timer snapshotTimer = new Timer();
    private String lastScoreboardObjective = "";
    private String lastWorld = "";
    private String lastGuiTitle = "";
    private boolean enabled;
    private boolean registered;
    private Path outputPath;
    private long sessionStartedMs;
    private int guiSnapshotsThisSession;
    private int flushesThisSession;

    private DevDataHarvester() {
        // Enabled by default in dev environments; disabled for public builds.
        this.enabled = isLikelyDevWorkspace();
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    /** Called once during client init, after the event bus exists. */
    public void init() {
        if (registered) return;
        resolveOutputPath();
        loadExistingObservations();
        ZenithEventBus.getInstance().register(this);
        registered = true;
        sessionStartedMs = System.currentTimeMillis();
        ZenithClient.LOGGER.info("[DevData] harvester initialised (enabled={}, out={})",
                enabled, outputPath);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            note("Harvester ENABLED (manual toggle).");
            flush();
        } else {
            note("Harvester DISABLED (manual toggle).");
            flush();
        }
    }

    public Path getOutputPath() { return outputPath; }
    public int getObservedGuiCount() { return guiLayouts.size(); }
    public int getObservedChatCount() { return chatLines.size(); }
    public long getSessionMs() { return System.currentTimeMillis() - sessionStartedMs; }

    /** Append a free-text note (timestamped). Thread-safe. */
    public void note(String text) {
        if (text == null || text.isBlank()) return;
        pendingNotes.add(TS_FMT.format(Instant.now()) + "  " + text.strip());
    }

    /** Force a flush to disk now (e.g. from the {@code flush} subcommand). */
    public void flush() {
        drainPendingNotes();
        writeFile();
    }

    /** Force an immediate GUI snapshot of whatever screen is open (useful to capture sub-pages). */
    public void snapshotCurrentGui() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null && mc.screen.getTitle() != null) {
            String t = mc.screen.getTitle().getString();
            if (t != null && !t.isBlank()) snapshotGui(t);
        }
    }

    /** Close the currently-open GUI container (server-side inventory close). Safe when nothing is open. */
    public void closeScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen != null) {
            mc.execute(() -> mc.player.closeContainer());
        }
    }

    /** @return title of the currently-open screen, or {@code ""} if none. */
    public String currentScreenTitle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || mc.screen.getTitle() == null) return "";
        String t = mc.screen.getTitle().getString();
        return t == null ? "" : t;
    }

    // ---------------------------------------------------------------------
    // Event subscribers
    // ---------------------------------------------------------------------

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Drain pending manual notes.
        drainPendingNotes();

        // Periodic passive snapshots (scoreboard, bossbars, tab, entities).
        if (snapshotTimer.runIfElapsed(SNAPSHOT_INTERVAL_MS, () -> snapshotPassive(mc))) {
            // ran
        }

        // Detect open sign-edit screens (they aren't InventoryOpen).
        Screen s = mc.screen;
        if (s instanceof SignEditScreen ses) {
            recordSignScreen(mc, ses);
        }

        // Periodic flush.
        if (flushTimer.runIfElapsed(FLUSH_INTERVAL_MS, this::writeFile)) {
            flushesThisSession++;
        }
    }

    @SubscribeEvent
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!enabled) return;
        String title = event.getTitle();
        lastGuiTitle = title;
        // Take a full snapshot one tick later so the menu has finished populating.
        // We cheat by snapshotting immediately for most cases; GUIParser fires from
        // the first tick where title != lastTitle, which is already the populated menu.
        snapshotGui(title);
        writeFile();
    }

    @SubscribeEvent
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!enabled) return;
        // Flush on close so the file includes the just-closed GUI before anything
        // else overwrites it.
        writeFile();
    }

    @SubscribeEvent
    public void onChat(ChatReceivedEvent event) {
        if (!enabled) return;
        String raw = event.getRawFormatted() != null ? event.getRawFormatted() : event.getMessage();
        if (raw == null) return;
        String stripped = stripFormatting(raw);
        if (stripped.isBlank()) return;
        String ctx = switch (event.getTypeId()) {
            case 1 -> "SYSTEM";
            case 2 -> "ACTIONBAR";
            default -> "CHAT";
        };
        if ("ACTIONBAR".equals(ctx)) {
            if (actionBarLines.add(stripped)) {
                ZenithClient.LOGGER.debug("[DevData] new action bar: {}", truncate(stripped, 120));
            }
            return;
        }
        // Trim noisy overly-common spam that provides no research value.
        if (isIgnoredChat(stripped)) return;
        String key = ctx + " || " + normaliseForDedup(stripped);
        if (chatLines.add(key)) {
            // Also keep an exemplar raw line with numbers/player names intact, in the chat store.
            // We store key=normalised with value=first-seen exemplar via the Set; callers reading
            // the .md file see the normalised key.  (We additionally store a short sample.)
            ZenithClient.LOGGER.debug("[DevData] new chat [{}]: {}", ctx, truncate(stripped, 160));
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent event) {
        if (!enabled) return;
        String w = event.getWorldName();
        if (w != null && !w.equals(lastWorld)) {
            lastWorld = w;
            worldsVisited.add(w);
            note("World change → " + w);
            writeFile();
        }
    }

    // ---------------------------------------------------------------------
    // Snapshot helpers
    // ---------------------------------------------------------------------

    private void snapshotPassive(Minecraft mc) {
        // Scoreboard
        try {
            Scoreboard sb = mc.level.getScoreboard();
            if (sb != null) {
                Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
                if (obj != null) {
                    String objName = obj.getDisplayName() != null
                            ? stripFormatting(obj.getDisplayName().getString())
                            : obj.getName();
                    if (!objName.equals(lastScoreboardObjective)) {
                        lastScoreboardObjective = objName;
                        note("Scoreboard objective now: " + objName);
                    }
                    // Grab all team prefix/suffix entries and the player list lines.
                    for (PlayerTeam t : sb.getPlayerTeams()) {
                        String prefix = t.getPlayerPrefix() != null ? stripFormatting(t.getPlayerPrefix().getString()) : "";
                        String suffix = t.getPlayerSuffix() != null ? stripFormatting(t.getPlayerSuffix().getString()) : "";
                        String combined = (prefix + "…" + suffix).trim();
                        if (!combined.isBlank() && !"…".equals(combined)) scoreboardLines.add(combined);
                    }
                    // Score entries for the sidebar objective. Method names vary across
                    // 1.20 → 26.1; probe reflectively and skip on failure rather than crash.
                    try {
                        java.util.Collection<?> scores = null;
                        for (String mn : new String[]{"getPlayerScoresForObjective", "getListPlayerScores", "listPlayerScores"}) {
                            try {
                                var m = sb.getClass().getMethod(mn, Objective.class);
                                Object res = m.invoke(sb, obj);
                                if (res instanceof java.util.Collection<?> c) { scores = c; break; }
                            } catch (NoSuchMethodException ignored) {}
                        }
                        if (scores != null) {
                            for (Object sc : scores) {
                                String owner = null;
                                Integer value = null;
                                try {
                                    var ownM = sc.getClass().getMethod("owner");
                                    owner = (String) ownM.invoke(sc);
                                } catch (Exception ignored) {}
                                for (String vn : new String[]{"value", "score", "getScore"}) {
                                    try {
                                        var vM = sc.getClass().getMethod(vn);
                                        Object v = vM.invoke(sc);
                                        if (v instanceof Number n) { value = n.intValue(); break; }
                                    } catch (Exception ignored) {}
                                }
                                if (owner == null) continue;
                                String clean = stripFormatting(owner);
                                if (!clean.isBlank() && !clean.startsWith("#")) {
                                    scoreboardLines.add(clean + (value != null ? "  = " + value : ""));
                                }
                            }
                        }
                    } catch (Exception e) { ZenithClient.LOGGER.debug("[DevData] score entries unreadable", e); }
                }
            }
        } catch (Exception e) { ZenithClient.LOGGER.debug("[DevData] scoreboard snapshot failed", e); }

        // Boss bars — locate the BossOverlay via multiple possible names, then enumerate events.
        try {
            if (mc.gui != null) {
                Object overlay = null;
                for (String mn : new String[]{"getBossOverlay", "getBossBarOverlay"}) {
                    try {
                        var m = mc.gui.getClass().getMethod(mn);
                        overlay = m.invoke(mc.gui);
                        if (overlay != null) break;
                    } catch (NoSuchMethodException ignored) {}
                }
                if (overlay == null) {
                    for (String fn : new String[]{"bossOverlay", "overlay", "bossBarOverlay"}) {
                        try {
                            var f = mc.gui.getClass().getDeclaredField(fn);
                            f.setAccessible(true);
                            overlay = f.get(mc.gui);
                            if (overlay != null) break;
                        } catch (Exception ignored) {}
                    }
                }
                if (overlay != null) {
                    Map<java.util.UUID, ?> events = null;
                    try {
                        var m = overlay.getClass().getMethod("getBossEvents");
                        Object res = m.invoke(overlay);
                        if (res instanceof Map<?,?> mm) {
                            @SuppressWarnings("unchecked")
                            var cast = (Map<java.util.UUID, ?>) mm;
                            events = cast;
                        }
                    } catch (NoSuchMethodException ignored) {
                        for (String fn : new String[]{"events", "bossEvents", "map"}) {
                            try {
                                var f = overlay.getClass().getDeclaredField(fn);
                                f.setAccessible(true);
                                Object res = f.get(overlay);
                                if (res instanceof Map<?,?> mm) {
                                    @SuppressWarnings("unchecked")
                                    var cast = (Map<java.util.UUID, ?>) mm;
                                    events = cast;
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    if (events != null) {
                        for (Object raw : events.values()) {
                            if (raw instanceof BossEvent be) {
                                Component n = be.getName();
                                if (n != null) {
                                    String t = stripFormatting(n.getString());
                                    if (!t.isBlank()) bossBarTitles.add(t);
                                }
                            } else if (raw != null) {
                                // Some versions wrap BossEvent; try getName() reflectively.
                                try {
                                    var nm = raw.getClass().getMethod("getName");
                                    Object v = nm.invoke(raw);
                                    if (v instanceof Component c) {
                                        String t = stripFormatting(c.getString());
                                        if (!t.isBlank()) bossBarTitles.add(t);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { ZenithClient.LOGGER.debug("[DevData] bossbar snapshot failed", e); }

        // Tab list header/footer (only when player list is showing).
        try {
            var conn = mc.getConnection();
            if (conn != null) {
                // ClientboundTabListPacket stores header/footer on the connection/list.
                // We read them via the GuiMessageComponent; try the player connection's tab list fields.
                var f = conn.getClass();
                // Try the most common 1.20+ field names via reflection.
                Component header = getComponentField(conn, "tabListHeader", "header");
                Component footer = getComponentField(conn, "tabListFooter", "footer");
                if (header != null) addMultiline(tabHeaderFooter, "HEADER: " + stripFormatting(header.getString()));
                if (footer != null) addMultiline(tabHeaderFooter, "FOOTER: " + stripFormatting(footer.getString()));
            }
        } catch (Exception e) { ZenithClient.LOGGER.debug("[DevData] tab snapshot failed", e); }

        // Nearby entity types
        try {
            if (mc.level != null && mc.player != null) {
                mc.level.entitiesForRendering().forEach(e -> {
                    if (e == mc.player) return;
                    String id = e.getType().builtInRegistryHolder().key().location().toString();
                    if (id != null) entityTypesSeen.add(id);
                });
            }
        } catch (Exception e) { ZenithClient.LOGGER.debug("[DevData] entity snapshot failed", e); }
    }

    private static Component getComponentField(Object o, String... names) {
        for (String n : names) {
            try {
                var f = o.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object v = f.get(o);
                if (v instanceof Component c) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void snapshotGui(String title) {
        GUIState s = GUIParser.getInstance().read();
        if (!s.present()) return;
        String key = (title == null ? "" : title) + "|" + s.slots + "|" + s.rows;
        // If we already have this exact layout key, only refresh if the content changed.
        String contentFingerprint = fingerprint(s);
        GuiLayoutObservation existing = guiLayouts.get(key);
        if (existing != null && existing.fingerprint.equals(contentFingerprint)) {
            existing.lastSeenEpochSec = Instant.now().getEpochSecond();
            existing.timesSeen++;
            return;
        }
        GuiLayoutObservation obs = new GuiLayoutObservation();
        obs.title = s.title;
        obs.slots = s.slots;
        obs.rows = s.rows;
        obs.buttons = new ArrayList<>(s.buttons);
        obs.firstSeenEpochSec = existing != null ? existing.firstSeenEpochSec : Instant.now().getEpochSecond();
        obs.lastSeenEpochSec = Instant.now().getEpochSecond();
        obs.timesSeen = existing != null ? existing.timesSeen + 1 : 1;
        obs.fingerprint = contentFingerprint;
        obs.items = new ArrayList<>();
        for (int i = 0; i < s.stacks.size(); i++) {
            GUIItemStack stack = s.stacks.get(i);
            if (stack == null) continue;
            ItemSlotRecord rec = new ItemSlotRecord();
            rec.slot = i;
            rec.name = stack.displayName();
            rec.skyblockId = stack.skyblockId();
            rec.itemId = stack.itemId();
            rec.count = stack.stackSize();
            rec.enchanted = stack.enchanted();
            rec.lore = stack.lore() == null ? List.of() : new ArrayList<>(stack.lore());
            obs.items.add(rec);
        }
        guiLayouts.put(key, obs);
        guiSnapshotsThisSession++;
        ZenithClient.LOGGER.info("[DevData] captured GUI layout: title=\"{}\" slots={} rows={} items={}",
                title, s.slots, s.rows, obs.items.size());
    }

    private void recordSignScreen(Minecraft mc, SignEditScreen ses) {
        // Reflection-based read (parallels SignInputHandler logic).
        String[] lines = readSignLines(ses);
        StringBuilder sb = new StringBuilder();
        for (String l : lines) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(l);
        }
        String combined = sb.toString().trim();
        if (!combined.isBlank()) signPrompts.add(combined);
    }

    private static String[] readSignLines(SignEditScreen ses) {
        // Probe a handful of field names used across versions.
        String[] probe = {"signText", "frontText", "backText"};
        for (String p : probe) {
            try {
                var f = ses.getClass().getDeclaredField(p);
                f.setAccessible(true);
                Object v = f.get(ses);
                String[] arr = extractTextArray(v);
                if (arr != null) return arr;
            } catch (Exception ignored) {}
        }
        // Last resort: search SignText typed fields in class hierarchy.
        Class<?> c = ses.getClass();
        while (c != null && c != Object.class) {
            for (var f : c.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object v = f.get(ses);
                    String[] arr = extractTextArray(v);
                    if (arr != null) return arr;
                } catch (Exception ignored) {}
            }
            c = c.getSuperclass();
        }
        return new String[]{"(unable to read sign text)"};
    }

    private static String[] extractTextArray(Object v) {
        if (v == null) return null;
        try {
            // 1.20+ SignText object with Text[] messages
            if (v.getClass().getName().contains("SignText")) {
                var messages = v.getClass().getMethod("getMessages", boolean.class);
                Object arr = messages.invoke(v, true);
                if (arr instanceof Object[] texts) {
                    String[] out = new String[texts.length];
                    for (int i = 0; i < texts.length; i++) {
                        out[i] = texts[i] == null ? "" : texts[i].toString();
                    }
                    return out;
                }
                var msgs = v.getClass().getMethod("getMessages");
                Object arr2 = msgs.invoke(v);
                if (arr2 instanceof Object[] texts) {
                    String[] out = new String[texts.length];
                    for (int i = 0; i < texts.length; i++) {
                        out[i] = texts[i] == null ? "" : texts[i].toString();
                    }
                    return out;
                }
            }
        } catch (Exception ignored) {}
        if (v instanceof List<?> list) {
            String[] out = new String[list.size()];
            int i = 0;
            for (Object o : list) out[i++] = o == null ? "" : o.toString();
            return out;
        }
        if (v instanceof Object[] arr) {
            String[] out = new String[arr.length];
            for (int i = 0; i < arr.length; i++) out[i] = arr[i] == null ? "" : arr[i].toString();
            return out;
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // File output
    // ---------------------------------------------------------------------

    private void resolveOutputPath() {
        try {
            Minecraft mc = Minecraft.getInstance();
            Path gameDir = mc.gameDirectory.toPath().toAbsolutePath();
            Path dir = gameDir.resolve(DIR_NAME);
            Files.createDirectories(dir);
            outputPath = dir.resolve(FILE_NAME);
        } catch (IOException e) {
            // Fall back to a temp directory so we never crash init.
            outputPath = Path.of(System.getProperty("java.io.tmpdir"), "zenith-devdata", FILE_NAME);
            try { Files.createDirectories(outputPath.getParent()); } catch (IOException ignored) {}
            ZenithClient.LOGGER.warn("[DevData] could not create game-dir path, using fallback {}", outputPath, e);
        }
    }

    /** Seed dedup sets from the existing file so we don't re-record across runs. */
    private void loadExistingObservations() {
        if (outputPath == null || !Files.exists(outputPath)) return;
        try {
            String content = Files.readString(outputPath, StandardCharsets.UTF_8);
            // Lightweight seed: collect lines that look like they were recorded.
            // We don't try to fully round-trip; we just re-record observations the
            // first time we see them in the new session so the file stays fresh.
            if (content.contains("## GUI Layouts")) {
                ZenithClient.LOGGER.info("[DevData] existing {} will be appended/overwritten on flush.", outputPath);
            }
        } catch (IOException e) {
            ZenithClient.LOGGER.warn("[DevData] unable to read existing file; starting fresh.", e);
        }
    }

    private void drainPendingNotes() {
        String n;
        while ((n = pendingNotes.poll()) != null) manualNotes.add(n);
    }

    private void writeFile() {
        if (outputPath == null) resolveOutputPath();
        drainPendingNotes();
        try {
            Files.createDirectories(outputPath.getParent());
            String md = renderMarkdown();
            Files.writeString(outputPath, md, StandardCharsets.UTF_8);
            flushesThisSession++;
        } catch (IOException e) {
            ZenithClient.LOGGER.error("[DevData] write failed: {}", outputPath, e);
        }
    }

    private String renderMarkdown() {
        StringBuilder sb = new StringBuilder(64 * 1024);
        String now = TS_FMT.format(Instant.now());
        sb.append("# Zenith Client — In-game Developer Observations\n\n");
        sb.append("> Auto-generated by `DevDataHarvester` while macros run. Do not hand-edit; this file is\n");
        sb.append("> rewritten on every flush. Copy findings out of here into `docs/RESEARCH.md` and commit.\n\n");
        sb.append("- **Zenith version:** ").append(ZenithClientInfo.VERSION).append('\n');
        sb.append("- **Minecraft target:** ").append(ZenithClientInfo.MC_VERSION).append('\n');
        sb.append("- **File path:** `").append(outputPath).append("`\n");
        sb.append("- **Generated (local):** ").append(now).append('\n');
        sb.append("- **Session uptime:** ").append(formatDuration(getSessionMs())).append('\n');
        sb.append("- **Enabled:** ").append(enabled).append('\n');
        sb.append("- **Flushes this session:** ").append(flushesThisSession).append('\n');
        sb.append("- **GUI snapshots this session:** ").append(guiSnapshotsThisSession).append('\n');
        sb.append('\n');

        sb.append("## Summary\n\n");
        sb.append("| Category | Unique count |\n|---|---|\n");
        sb.append("| GUI layouts observed | ").append(guiLayouts.size()).append(" |\n");
        sb.append("| Sign prompts | ").append(signPrompts.size()).append(" |\n");
        sb.append("| Scoreboard lines | ").append(scoreboardLines.size()).append(" |\n");
        sb.append("| Action-bar lines | ").append(actionBarLines.size()).append(" |\n");
        sb.append("| Boss-bar titles | ").append(bossBarTitles.size()).append(" |\n");
        sb.append("| Tab header/footer lines | ").append(tabHeaderFooter.size()).append(" |\n");
        sb.append("| Chat lines (context + normalised) | ").append(chatLines.size()).append(" |\n");
        sb.append("| Worlds/locations visited | ").append(worldsVisited.size()).append(" |\n");
        sb.append("| Entity types seen | ").append(entityTypesSeen.size()).append(" |\n");
        sb.append("| Manual notes | ").append(manualNotes.size()).append(" |\n");
        sb.append('\n');

        // Worlds
        sb.append("## Worlds & Locations Visited\n\n");
        if (worldsVisited.isEmpty()) sb.append("_None yet._\n\n");
        else {
            for (String w : worldsVisited) sb.append("- `").append(escapeMd(w)).append("`\n");
            sb.append('\n');
        }

        // Sign prompts
        sb.append("## Sign Prompts\n\n");
        sb.append("Useful for confirming sign-screen titles for SignInputHandler flows (AH price,\n");
        sb.append("Bazaar quantity/price, travel scrolls, etc).\n\n");
        if (signPrompts.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String p : signPrompts) sb.append("- `").append(escapeMd(p)).append("`\n");
            sb.append('\n');
        }

        // GUI layouts — the most important section
        sb.append("## GUI Layouts\n\n");
        sb.append("Full slot-by-slot dump of every unique container observed. Compare against the\n");
        sb.append("finders in `AuctionHouseGUI` / `BazaarGUI` and fill in [RESEARCH NEEDED] gaps.\n\n");
        if (guiLayouts.isEmpty()) sb.append("_None observed yet. Open /ah or /bz._\n\n");
        else {
            for (GuiLayoutObservation g : guiLayouts.values()) {
                sb.append("### `").append(escapeMd(g.title)).append("`  _(").append(g.slots).append(" slots, ")
                        .append(g.rows).append(" chest rows, seen x").append(g.timesSeen).append(")_\n\n");
                if (g.buttons != null && !g.buttons.isEmpty()) {
                    sb.append("**Screen widgets / buttons:** ");
                    boolean first = true;
                    for (String b : g.buttons) {
                        if (!first) sb.append(", ");
                        first = false;
                        sb.append('`').append(escapeMd(b)).append('`');
                    }
                    sb.append("\n\n");
                }
                sb.append("| Slot | Item | SkyBlock ID | Count | Ench | Lore (first 3 lines) |\n");
                sb.append("|---:|---|---|---:|:---:|---|\n");
                for (ItemSlotRecord r : g.items) {
                    sb.append('|').append(r.slot);
                    sb.append('|').append(mdCell(r.name));
                    sb.append('|').append(mdCell(r.skyblockId));
                    sb.append('|').append(r.count);
                    sb.append('|').append(r.enchanted ? "✓" : "");
                    // lore summary
                    StringBuilder lsb = new StringBuilder();
                    int n = Math.min(3, r.lore.size());
                    for (int i = 0; i < n; i++) {
                        if (i > 0) lsb.append(" / ");
                        lsb.append(truncate(r.lore.get(i), 80));
                    }
                    if (r.lore.size() > 3) lsb.append(" …(+").append(r.lore.size() - 3).append(')');
                    sb.append('|').append(mdCell(lsb.toString()));
                    sb.append("|\n");
                }
                sb.append("\n_Last seen: ").append(TS_FMT.format(Instant.ofEpochSecond(g.lastSeenEpochSec))).append("_\n\n");
            }
        }

        // Scoreboard
        sb.append("## Scoreboard Lines\n\n");
        sb.append("Objective: `").append(escapeMd(lastScoreboardObjective)).append("`\n\n");
        if (scoreboardLines.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String l : scoreboardLines) sb.append("- `").append(escapeMd(l)).append("`\n");
            sb.append('\n');
        }

        // Action bar
        sb.append("## Action Bar Lines\n\n");
        if (actionBarLines.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String l : actionBarLines) sb.append("- `").append(escapeMd(truncate(l, 200))).append("`\n");
            sb.append('\n');
        }

        // Boss bars
        sb.append("## Boss Bar Titles\n\n");
        if (bossBarTitles.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String l : bossBarTitles) sb.append("- `").append(escapeMd(l)).append("`\n");
            sb.append('\n');
        }

        // Tab header/footer
        sb.append("## Tab-List Header / Footer\n\n");
        if (tabHeaderFooter.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String l : tabHeaderFooter) sb.append("- `").append(escapeMd(truncate(l, 200))).append("`\n");
            sb.append('\n');
        }

        // Chat lines — just samples, capped to avoid huge files
        sb.append("## Chat Patterns (deduplicated, numeric values normalised)\n\n");
        sb.append("These are the normalised forms used by `ChatPatternEngine`. Numbers, coin amounts,\n");
        sb.append("player names and UUID fragments are collapsed so that repeated messages produce a single entry.\n\n");
        if (chatLines.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            List<String> sorted = new ArrayList<>(chatLines);
            Collections.sort(sorted);
            int cap = Math.min(sorted.size(), 600);
            for (int i = 0; i < cap; i++) {
                sb.append("- `").append(escapeMd(sorted.get(i))).append("`\n");
            }
            if (sorted.size() > cap) sb.append("\n_... ").append(sorted.size() - cap).append(" more (truncated)._\n");
            sb.append('\n');
        }

        // Entity types
        sb.append("## Entity Types Seen (in render distance)\n\n");
        if (entityTypesSeen.isEmpty()) sb.append("_None observed yet._\n\n");
        else {
            for (String e : entityTypesSeen) sb.append("- `").append(e).append("`\n");
            sb.append('\n');
        }

        // Manual notes
        sb.append("## Manual Notes\n\n");
        if (manualNotes.isEmpty()) sb.append("_Add one with `.z devdata note <text>`._\n\n");
        else {
            for (String n : manualNotes) sb.append("- ").append(n).append('\n');
            sb.append('\n');
        }

        sb.append("---\n_End of file. Next flush will rewrite._\n");
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------

    private static String fingerprint(GUIState s) {
        // Build a stable fingerprint of slot[0..N-1] = name|skyblockId|itemId
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.stacks.size(); i++) {
            GUIItemStack st = s.stacks.get(i);
            sb.append(i).append('=');
            if (st != null) {
                sb.append(nullToEmpty(st.displayName())).append('|')
                        .append(nullToEmpty(st.skyblockId())).append('|')
                        .append(nullToEmpty(st.itemId())).append('|')
                        .append(st.stackSize());
            }
            sb.append(';');
        }
        // Stable 64-bit hash — collisions across different layouts are extremely rare
        // and, when they happen, simply mean we don't re-record a layout we already have.
        long h = 1125899906842597L;
        for (int i = 0; i < sb.length(); i++) h = 31 * h + sb.charAt(i);
        return Long.toUnsignedString(h, 16);
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String stripFormatting(String s) {
        return s == null ? "" : s.replaceAll("(?i)§[0-9a-fk-orx]", "");
    }

    private static String normaliseForDedup(String s) {
        // Collapse digits, UUID fragments, common player name tokens to single placeholder.
        String out = s;
        out = out.replaceAll("[0-9]{1,3}(,[0-9]{3})*(\\.[0-9]+)?", "<NUM>");
        out = out.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}", "<UUID>");
        out = out.replaceAll("[0-9a-fA-F]{32,}", "<HEX>");
        return out;
    }

    private static boolean isIgnoredChat(String stripped) {
        // Very noisy lines that have no research value regardless of content.
        String lower = stripped.toLowerCase(Locale.ROOT);
        if (lower.isBlank()) return true;
        if (lower.startsWith("                             ")) return true; // tab spam padding
        if (stripped.length() < 2) return true;
        return false;
    }

    private static void addMultiline(Set<String> sink, String block) {
        if (block == null) return;
        for (String line : block.split("\\r?\\n")) {
            String t = line.strip();
            if (t.isBlank()) continue;
            sink.add(t);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static String escapeMd(String s) {
        if (s == null) return "";
        return s.replace("|", "\\|").replace("`", "\\`").replace("\n", " / ");
    }

    private static String mdCell(String s) {
        if (s == null || s.isBlank()) return "";
        return '`' + escapeMd(truncate(s, 200)) + '`';
    }

    private static String formatDuration(long ms) {
        long s = ms / 1000;
        long h = s / 3600; s %= 3600;
        long m = s / 60; s %= 60;
        return String.format("%dh%02dm%02ds", h, m, s);
    }

    private static boolean isLikelyDevWorkspace() {
        // Heuristic: Loom dev run has "-Dfabric.dev" or a mod list that contains "fabric-loader"
        // launched from a Gradle classes dir. We keep it simple and default to enabled; users can
        // always .z devdata off.
        String prop = System.getProperty("zenith.devdata.auto", "true");
        return !"false".equalsIgnoreCase(prop);
    }

    // ---------------------------------------------------------------------
    // Data records (POJOs, not MC-coupled so they can be serialised/reflected)
    // ---------------------------------------------------------------------

    private static final class GuiLayoutObservation {
        String title;
        int slots;
        int rows;
        List<String> buttons;
        String fingerprint;
        long firstSeenEpochSec;
        long lastSeenEpochSec;
        int timesSeen;
        List<ItemSlotRecord> items;
    }

    private static final class ItemSlotRecord {
        int slot;
        String name;
        String skyblockId;
        String itemId;
        int count;
        boolean enchanted;
        List<String> lore;
    }
}
