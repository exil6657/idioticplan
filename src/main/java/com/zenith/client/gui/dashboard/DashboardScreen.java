package com.zenith.client.gui.dashboard;

import com.zenith.client.ZenithClientInfo;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.engine.render.Color4f;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeType;
import com.zenith.client.flipping.FlipEngine;
import com.zenith.client.flipping.order.OrderManager;
import com.zenith.client.flipping.profit.ProfitTracker;
import com.zenith.client.gui.ZenithScreen;
import com.zenith.client.gui.ZenithScreenWrapper;
import com.zenith.client.gui.component.*;
import com.zenith.client.gui.theme.Theme;
import com.zenith.client.gui.theme.ThemeManager;
import com.zenith.client.devdata.DevDataHarvester;
import com.zenith.client.macro.MacroManager;
import com.zenith.client.macro.MacroModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Primary Zenith control surface — dashboard is PRIMARY, commands secondary (user spec).
 *
 * <p>v2 rewrite: now shows live global controls always visible, per-tab live data, active orders table,
 * failsafe detector list, macro grouping, and full settings with devdata quick/full tours.</p>
 *
 * <p>Tabs: Home (session overview), Flipper (live orders + scanner), Failsafe (detectors + safety dot + tests),
 * Macros (family-grouped start/stop + config), Settings (HUD/theme/keybinds/devdata/breaks/bits).</p>
 */
public class DashboardScreen extends ZenithScreen {

    private final ZenithTabBar tabs;
    private String current = "home";
    private final List<Component> homePanel = new ArrayList<>();
    private final List<Component> flipPanel = new ArrayList<>();
    private final List<Component> failPanel = new ArrayList<>();
    private final List<Component> macroPanel = new ArrayList<>();
    private final List<Component> settingsPanel = new ArrayList<>();

    // Global controls (always visible at top)
    private final List<Component> globalBar = new ArrayList<>();

    public DashboardScreen() {
        title = "Zenith Dashboard";
        closeOnEsc = true;
        tabs = new ZenithTabBar();
        tabs.addTab("home", "Home")
            .addTab("flipper", "Flipper")
            .addTab("failsafe", "Failsafe")
            .addTab("macros", "Macros")
            .addTab("settings", "Settings")
            .onSwitch(id -> current = id);
        add(tabs);
        buildGlobalBar();
        buildPanels();
    }

    private void buildGlobalBar() {
        Button resume = new Button("Resume", b -> {
            FailsafeManager.getInstance().resumeFromUser();
            ZenithChat.getInstance().success("Failsafe resumed.");
        });
        Button panic = new Button("PANIC", b -> {
            FailsafeManager.getInstance().panicButton().onPress();
        });
        Button disconnect = new Button("Disconnect", b -> FailsafeManager.getInstance().banActions().disconnect("dashboard manual"));
        Button close = new Button("Close", b -> ZenithScreenWrapper.close());
        globalBar.add(resume); globalBar.add(panic); globalBar.add(disconnect); globalBar.add(close);
    }

    private void buildPanels() {
        // ---- HOME ----
        homePanel.clear();
        homePanel.add(new Label("Zenith Client v" + ZenithClientInfo.VERSION + " — MC " + ZenithClientInfo.MC_VERSION, accent()));
        homePanel.add(new Label("Dashboard is PRIMARY control — commands are secondary", secondary()));
        homePanel.add(new Label(" ", secondary()));
        homePanel.add(new Label("Global Controls (always visible above tabs also)", secondary()));
        // Status will be live-rendered in render()

        // ---- FLIPPER ----
        flipPanel.clear();
        flipPanel.add(new Label("Flipper — Bazaar / AH / NPC / Craft", accent()));
        flipPanel.add(new Button("Start Flipper", b -> { FlipEngine.getInstance().start(); ZenithChat.getInstance().info("Flipper started"); }));
        flipPanel.add(new Button("Stop Flipper", b -> FlipEngine.getInstance().stop()));
        flipPanel.add(new Label(" ", secondary()));
        flipPanel.add(new ZenithToggle("AH BIN Flips", true).onToggle(v -> { /* wired to FlipperConfig later */ }));
        flipPanel.add(new ZenithToggle("Bazaar Spreads", true));
        flipPanel.add(new ZenithToggle("Craft Flips", false));
        flipPanel.add(new ZenithToggle("NPC Flips", false));
        flipPanel.add(new ZenithToggle("Scheduled Breaks (~45/5 min)", true));
        flipPanel.add(new Label(" ", secondary()));
        flipPanel.add(new Label("Active Orders — live (rebuilds each frame)", secondary()));
        // active orders table built dynamically in rebuildFlipperPanel()

        // ---- FAILSAFE ----
        failPanel.clear();
        failPanel.add(new Label("Failsafe — Safety dot + detectors", accent()));
        failPanel.add(new Button("Clear & Resume (→ /is cleared)", b -> FailsafeManager.getInstance().resumeFromUser()));
        failPanel.add(new Button("Force /is (private island)", b -> FailsafeManager.getInstance().banActions().sendIsland()));
        failPanel.add(new Button("Force /hub fallback", b -> FailsafeManager.getInstance().banActions().sendHub()));
        failPanel.add(new Button("Disconnect", b -> FailsafeManager.getInstance().banActions().disconnect("dashboard failsafe")));
        failPanel.add(new Label(" ", secondary()));
        failPanel.add(new ZenithToggle("Sound alert", FailsafeManager.getInstance().config().soundAlert).onToggle(v -> FailsafeManager.getInstance().config().soundAlert = v));
        failPanel.add(new ZenithToggle("Toast alert", FailsafeManager.getInstance().config().toastAlert).onToggle(v -> FailsafeManager.getInstance().config().toastAlert = v));
        failPanel.add(new ZenithToggle("Global enabled", FailsafeManager.getInstance().config().globalEnabled).onToggle(v -> FailsafeManager.getInstance().config().globalEnabled = v));
        failPanel.add(new Label(" ", secondary()));
        failPanel.add(new Label("Detectors — toggle per type", secondary()));
        // detector toggles built dynamically in rebuildFailsafePanel()

        // ---- MACROS ----
        macroPanel.clear();
        macroPanel.add(new Label("Macros — Farming / Mining / Combat / etc (one at a time)", accent()));
        macroPanel.add(new Button("Stop All Macros", b -> MacroManager.getInstance().stopAll("dashboard")));
        macroPanel.add(new Label(" ", secondary()));
        // rows rebuilt dynamically

        // ---- SETTINGS ----
        settingsPanel.clear();
        settingsPanel.add(new Label("Settings — HUD / Theme / DevData / Keybinds / Bits", accent()));
        settingsPanel.add(new ZenithToggle("HUD visible", true).onToggle(v -> { /* HudManager visible */ }));
        settingsPanel.add(new ZenithToggle("Brain View (debug overlay)", false).onToggle(v -> {
            // Toggle brain view via GuiEngine debug flag if exists
            try { com.zenith.client.gui.GuiEngine.getInstance().setBrainView(v); } catch (Throwable ignored) {}
        }));
        settingsPanel.add(new Label(" ", secondary()));
        settingsPanel.add(new Label("DevData Harvester (exhaustive)", secondary()));
        settingsPanel.add(new ZenithToggle("Record dev data → OBSERVATIONS.md", DevDataHarvester.getInstance().isEnabled()).onToggle(v -> DevDataHarvester.getInstance().setEnabled(v)));
        settingsPanel.add(new Button("Flush .md now", b -> { DevDataHarvester.getInstance().flush(); ZenithChat.getInstance().info("Flushed to {}", DevDataHarvester.getInstance().getOutputPath()); }));
        settingsPanel.add(new Button("Run QUICK tour (3 min)", b -> {
            MacroManager.getInstance().stopAll("devdata dashboard quick");
            MacroManager.getInstance().start("dev:devdata");
            ZenithChat.getInstance().info("Quick tour started — first cat/prod only");
        }));
        settingsPanel.add(new Button("Run FULL tour (25-40 min, ALL products)", b -> {
            MacroManager.getInstance().stopAll("devdata dashboard full");
            MacroManager.getInstance().start("dev:devdata-full");
            ZenithChat.getInstance().info("FULL tour started — ALL Bazaar products, AH sort cycle, 80+ GUIs — zero spend");
            ZenithChat.getInstance().info("File will be at {}", DevDataHarvester.getInstance().getOutputPath());
        }));
        settingsPanel.add(new Button("Show .md path", b -> ZenithChat.getInstance().info("DevData file: {}", DevDataHarvester.getInstance().getOutputPath())));
        settingsPanel.add(new Button("Add note: .z devdata note <text>", b -> ZenithChat.getInstance().info("Use command: .z devdata note my note")));
        settingsPanel.add(new Label(" ", secondary()));
        settingsPanel.add(new Button("Open HUD Editor", b -> com.zenith.client.gui.hud.editor.HudEditor.getInstance().toggle()));
        settingsPanel.add(new Button("Reset HUD Layout", b -> com.zenith.client.gui.hud.HudManager.getInstance().applyLayout()));
        settingsPanel.add(new Label(" ", secondary()));
        settingsPanel.add(new Label("Theme / Language (stub — Phase 18+)", secondary()));
        settingsPanel.add(new ZenithToggle("Amethyst theme", false).onToggle(v -> {
            try { ThemeManager.getInstance().setTheme(v ? "amethyst" : "dark"); } catch (Throwable ignored) {}
        }));
        settingsPanel.add(new Label("Bits protection — blocks bits spending when enabled (default ON)", secondary()));
        settingsPanel.add(new ZenithToggle("Block bits spend", true).onToggle(v -> com.zenith.client.core.protection.BitsSpendBlocker.setBlocked(v)));
    }

    @Override
    public void init(int w, int h) {
        tabs.x = 20; tabs.y = 38; tabs.width = w - 40; tabs.height = 22;
        layoutPanel(homePanel, 20, 70, w-40);
        layoutPanel(flipPanel, 20, 70, w-40);
        layoutPanel(failPanel, 20, 70, w-40);
        layoutPanel(macroPanel, 20, 70, w-40);
        layoutPanel(settingsPanel, 20, 70, w-40);
        layoutGlobalBar(w, h);
    }

    private void layoutGlobalBar(int w, int h) {
        float y = 10;
        float x = w - 40 - 4*84;
        for (Component c : globalBar) {
            c.width = 80; c.height = 16;
            c.x = x; c.y = y;
            x += 84;
        }
    }

    private void layoutPanel(List<Component> panel, float ox, float oy, float maxW) {
        float y = oy;
        for (Component c : panel) {
            if (c instanceof Label) { c.width = maxW; c.height = 11; }
            else if (c instanceof Button) { c.width = Math.min(maxW - 20, 220); c.height = 18; }
            else if (c instanceof ZenithToggle) { c.width = maxW - 20; c.height = 16; }
            c.x = ox + 10; c.y = y;
            y += c.height + 4;
        }
    }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        Theme t = ThemeManager.getInstance().current();
        int w = ctx.screenWidth, h = ctx.screenHeight;
        ctx.fillRect(0, 0, w, h, new Color4f(0,0,0,0.58f));
        float pw = Math.min(620, w - 24);
        float ph = Math.min(460, h - 20);
        float px = w/2f - pw/2f;
        float py = h/2f - ph/2f;
        ctx.fillRect(px, py, pw, ph, t.background);
        ctx.drawOutline(px, py, pw, ph, 1.5f, t.border);

        // Title + version + close hint
        ctx.drawString(title + " — v" + ZenithClientInfo.VERSION, px + 12, py + 10, t.accent, true);
        ctx.drawString("ESC to close • .z dashboard", px + pw - 180, py + 10, t.textSecondary, false);

        // Global bar
        for (Component c : globalBar) {
            c.hovered = c.isMouseOver(mx, my);
            c.render(ctx, mx, my, td);
        }

        // Tabs
        tabs.x = px + 10; tabs.y = py + 32; tabs.width = pw - 20;
        tabs.hovered = tabs.isMouseOver(mx, my);
        tabs.render(ctx, mx, my, td);

        // Rebuild dynamic panels each frame
        rebuildMacroPanel();
        rebuildFlipperPanel();
        rebuildFailsafePanel();
        rebuildHomePanel();

        List<Component> active = activePanel();
        float startX = px + 10;
        float startY = py + 62;
        float clipBottom = py + ph - 28;
        for (Component c : active) {
            float ox = c.x, oy = c.y;
            c.x = startX + (c.x - 20);
            c.y = startY + (c.y - 70);
            if (c.y + c.height > clipBottom) break;
            if (c.y < startY - 10) { c.x = ox; c.y = oy; continue; }
            c.hovered = c.isMouseOver(mx, my);
            c.render(ctx, mx, my, td);
            c.x = ox; c.y = oy;
        }

        // Bottom status bar: failsafe + flipper + macro + devdata counts
        var fd = FailsafeManager.getInstance().debugData();
        var fl = FlipEngine.getInstance().debugData();
        var macroActive = MacroManager.getInstance().active();
        String macroStr = macroActive == null ? "none" : macroActive.displayName() + " " + macroActive.state();
        String status = String.format("FS:%s %s | Flip:%s %d₵ +%d orders | Macro:%s | DevData:%d guis",
                fd.currentSeverity, fd.active ? fd.activeType.displayName() : "clear",
                fl.running ? "ON" : "off", fl.sessionProfit, OrderManager.getInstance().activeOrders().size(),
                macroStr, DevDataHarvester.getInstance().getObservedGuiCount());
        ctx.drawString(status, px + 12, py + ph - 18, t.textSecondary, false);
    }

    private List<Component> activePanel() {
        return switch (current) {
            case "flipper" -> flipPanel;
            case "failsafe" -> failPanel;
            case "macros" -> macroPanel;
            case "settings" -> settingsPanel;
            default -> homePanel;
        };
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        for (Component c : globalBar) if (c.isMouseOver(mx, my)) c.mouseClicked(mx, my, button);
        tabs.mouseClicked(mx, my, button);
        for (Component c : activePanel()) if (c.isMouseOver(mx, my)) c.mouseClicked(mx, my, button);
        super.mouseClicked(mx, my, button);
    }

    private static Color4f accent() { return ThemeManager.getInstance().current().accent; }
    private static Color4f secondary() { return ThemeManager.getInstance().current().textSecondary; }

    // ---- Dynamic rebuilds ----

    private void rebuildHomePanel() {
        // Keep first 3 labels, rebuild rest
        if (homePanel.size() > 3) homePanel.subList(3, homePanel.size()).clear();
        var fd = FailsafeManager.getInstance().debugData();
        var fl = FlipEngine.getInstance().debugData();
        var om = OrderManager.getInstance();
        var pt = ProfitTracker.getInstance();
        var macros = MacroManager.getInstance();

        String safety = fd.active ? "⚠ " + fd.activeType.displayName() + ": " + fd.activeReason : "✓ Clear — macros running normally";
        homePanel.add(new Label("Safety: " + safety, secondary()));
        homePanel.add(new Label("Failsafe severity: " + fd.currentSeverity + (fd.active ? " (" + fd.reactionState + ")" : ""), secondary()));
        homePanel.add(new Label("Flipper: " + (fl.running ? "ON" : "OFF") + " profit=" + fl.sessionProfit + " orders=" + om.activeOrders().size() + " holding=" + om.holdingCount() + " listed=" + om.listedCount(), secondary()));
        homePanel.add(new Label("Profit tracker: " + pt.sessionProfit() + " coins session, " + pt.sessionFlips() + " flips", secondary()));
        homePanel.add(new Label("Active macro: " + (macros.active() == null ? "none" : macros.active().displayName() + " " + macros.active().state() + " " + macros.active().sessionMs()/1000 + "s"), secondary()));
        homePanel.add(new Label("DevData: " + DevDataHarvester.getInstance().getObservedGuiCount() + " guis, " + DevDataHarvester.getInstance().getObservedChatCount() + " chats", secondary()));
        homePanel.add(new Label(" ", secondary()));
        homePanel.add(new Button("Resume Failsafe", b -> FailsafeManager.getInstance().resumeFromUser()));
        homePanel.add(new Button("Stop All Macros", b -> macros.stopAll("home dashboard")));
        homePanel.add(new Button("Start Flipper", b -> FlipEngine.getInstance().start()));
    }

    private void rebuildFlipperPanel() {
        // Preserve first 12 components (header + controls + toggles), rebuild table after
        List<Component> preserved = new ArrayList<>();
        for (int i = 0; i < Math.min(12, flipPanel.size()); i++) preserved.add(flipPanel.get(i));
        flipPanel.clear();
        flipPanel.addAll(preserved);

        var om = OrderManager.getInstance();
        var active = om.activeOrders();
        if (active.isEmpty()) {
            flipPanel.add(new Label("No active orders — scanner idle", secondary()));
        } else {
            flipPanel.add(new Label("Active orders (" + active.size() + "):", accent()));
            for (var o : active) {
                if (flipPanel.size() > 35) break; // avoid overflow
                String line = String.format("%s %s @%d → %d profit %d [%s] %ds",
                        o.itemId(), o.candidate.type, o.buyPrice, o.listPrice > 0 ? o.listPrice : o.candidate.sellPrice,
                        o.candidate.expectedProfit, o.state, (System.currentTimeMillis() - o.enteredStateAtMs)/1000);
                flipPanel.add(new Label(line, secondary()));
            }
        }
        var fl = FlipEngine.getInstance().debugData();
        flipPanel.add(new Label("Scanner: BIN=" + fl.binItems + " Bazaar=" + fl.bazaarItems + " queue=" + fl.candidateQueueDepth + " scans=" + fl.scans + " break=" + fl.onBreak, secondary()));
        flipPanel.add(new Label("Budget: " + fl.budget + " coins | Success: " + String.format("%.1f%%", fl.successRate*100), secondary()));
    }

    private void rebuildFailsafePanel() {
        // Preserve first 9 components (header + buttons + toggles)
        List<Component> preserved = new ArrayList<>();
        for (int i = 0; i < Math.min(10, failPanel.size()); i++) preserved.add(failPanel.get(i));
        failPanel.clear();
        failPanel.addAll(preserved);

        var fd = FailsafeManager.getInstance().debugData();
        failPanel.add(new Label("Highest: " + fd.currentSeverity + " active=" + fd.active, secondary()));
        if (fd.active) {
            failPanel.add(new Label("Type: " + fd.activeType.displayName() + " reason=" + fd.activeReason, secondary()));
            failPanel.add(new Label("For: " + fd.activeForMs/1000 + "s triggers total=" + fd.triggersSinceStartup, secondary()));
            failPanel.add(new Label("Reaction: " + fd.reactionState + " frozen=" + fd.inputFrozen + " paused=" + fd.macrosPaused, secondary()));
        }
        // List recent triggers
        if (fd.recentTriggers != null && !fd.recentTriggers.isEmpty()) {
            failPanel.add(new Label("Recent:", accent()));
            for (String r : fd.recentTriggers) {
                if (failPanel.size() > 40) break;
                failPanel.add(new Label("- " + r, secondary()));
            }
        }
        // Detector list toggles
        failPanel.add(new Label("Detectors:", accent()));
        for (FailsafeType type : FailsafeType.values()) {
            if (failPanel.size() > 50) break;
            boolean enabled = FailsafeManager.getInstance().config().isDetectorEnabled(type);
            failPanel.add(new ZenithToggle(type.displayName() + " [" + FailsafeManager.getInstance().config().severityFor(type) + "]", enabled)
                    .onToggle(v -> FailsafeManager.getInstance().config().setDetectorEnabled(type, v)));
        }
    }

    private void rebuildMacroPanel() {
        List<Component> preserved = new ArrayList<>();
        for (int i = 0; i < Math.min(3, macroPanel.size()); i++) preserved.add(macroPanel.get(i));
        macroPanel.clear();
        macroPanel.addAll(preserved);

        Map<String, List<MacroModule>> byFamily = MacroManager.getInstance().all().stream()
                .collect(Collectors.groupingBy(MacroModule::skillFamily));

        for (Map.Entry<String, List<MacroModule>> e : byFamily.entrySet()) {
            if (macroPanel.size() > 60) break;
            macroPanel.add(new Label("— " + e.getKey() + " —", accent()));
            for (MacroModule m : e.getValue()) {
                boolean running = m.isRunning();
                Color4f state = running ? new Color4f(0.3f, 0.85f, 0.4f, 1f) : secondary();
                String label = m.icon() + " " + m.displayName() + " " + (running ? "● " + m.state() + " " + m.sessionMs()/1000 + "s" : "○ idle");
                macroPanel.add(new Label(label, state));
                final String id = m.id();
                macroPanel.add(new Button(running ? "Stop" : "Start", b -> {
                    if (MacroManager.getInstance().active() == m && running) MacroManager.getInstance().stopAll("dashboard");
                    else MacroManager.getInstance().start(id);
                }));
                // Quick config hint for farming macros
                if (m.id().startsWith("farming:")) {
                    macroPanel.add(new Label("  row 120 blocks, pitch -35°, yaw from start — configure in .z macro config TODO", secondary()));
                }
            }
        }
    }
}

