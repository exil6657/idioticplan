package com.zenith.client.gui.dashboard;

import com.zenith.client.ZenithClientInfo;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.engine.render.Color4f;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.FlipEngine;
import com.zenith.client.gui.ZenithScreen;
import com.zenith.client.gui.ZenithScreenWrapper;
import com.zenith.client.gui.component.*;
import com.zenith.client.gui.theme.Theme;
import com.zenith.client.gui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary Zenith control surface. Tabs: Home, Flipper, Failsafe, Macros, Settings.
 *
 * <p>This is the primary interface for controlling the client per the user's
 * direction — commands stay as a secondary/dev interface. Buttons toggle state
 * and display live data; no commands are required to start/stop subsystems.</p>
 */
public class DashboardScreen extends ZenithScreen {

    private final ZenithTabBar tabs;
    private String current = "home";
    private final List<Component> homePanel = new ArrayList<>();
    private final List<Component> flipPanel = new ArrayList<>();
    private final List<Component> failPanel = new ArrayList<>();
    private final List<Component> macroPanel = new ArrayList<>();
    private final List<Component> settingsPanel = new ArrayList<>();

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
        buildPanels();
    }

    private void buildPanels() {
        // --- HOME panel ---
        homePanel.add(new Label("Zenith Client v" + ZenithClientInfo.VERSION, accent()));
        homePanel.add(new Label("Phase 9 of 20", secondary()));

        Button btnFailsafeResume = new Button("Resume Failsafe", b -> {
            FailsafeManager.getInstance().resumeFromUser();
            ZenithChat.getInstance().success("Failsafe resumed from dashboard.");
        });
        Button btnPanic = new Button("EMERGENCY STOP", b -> {
            FailsafeManager.getInstance().panicButton().onPress();
        });
        Button btnClose = new Button("Close", b -> ZenithScreenWrapper.close());
        homePanel.add(btnFailsafeResume);
        homePanel.add(btnPanic);
        homePanel.add(btnClose);

        // --- FLIPPER panel ---
        Button flipStart = new Button("Start Flipper", b -> { FlipEngine.getInstance().init(); FlipEngine.getInstance().start(); });
        Button flipStop = new Button("Stop Flipper", b -> FlipEngine.getInstance().stop());
        flipPanel.add(new Label("Auction / Bazaar Flipper", accent()));
        flipPanel.add(flipStart);
        flipPanel.add(flipStop);
        ZenithToggle ahToggle = new ZenithToggle("AH BIN Flips", true);
        ZenithToggle bazaarToggle = new ZenithToggle("Bazaar spreads", true);
        ZenithToggle craftToggle = new ZenithToggle("Craft flips", false);
        ZenithToggle npcToggle = new ZenithToggle("NPC flips", false);
        ZenithToggle breaksToggle = new ZenithToggle("Scheduled breaks", true);
        flipPanel.add(ahToggle); flipPanel.add(bazaarToggle); flipPanel.add(craftToggle);
        flipPanel.add(npcToggle); flipPanel.add(breaksToggle);

        // --- FAILSAFE panel ---
        failPanel.add(new Label("Failsafe status", accent()));
        Button failResume = new Button("Clear & Resume", b -> FailsafeManager.getInstance().resumeFromUser());
        Button failDisconnect = new Button("Disconnect", b -> FailsafeManager.getInstance().banActions()
                .disconnect("manual dashboard disconnect"));
        failPanel.add(failResume);
        failPanel.add(failDisconnect);
        failPanel.add(new ZenithToggle("Sound alert", FailsafeManager.getInstance().config().soundAlert)
                .onToggle(v -> FailsafeManager.getInstance().config().soundAlert = v));
        failPanel.add(new ZenithToggle("Toast alert", FailsafeManager.getInstance().config().toastAlert)
                .onToggle(v -> FailsafeManager.getInstance().config().toastAlert = v));

        // --- MACROS panel (placeholders until farming/combat/mining phases). ---
        macroPanel.add(new Label("Macros — coming in later phases", secondary()));
        macroPanel.add(new Label("(farming / mining / combat / fishing / foraging)", secondary()));

        // --- SETTINGS panel ---
        settingsPanel.add(new Label("Settings", accent()));
        settingsPanel.add(new ZenithToggle("HUD visible", true));
        settingsPanel.add(new ZenithToggle("Brain View (debug)", false));
        settingsPanel.add(new ZenithToggle("Chat prefix", true));
        settingsPanel.add(new Button("Open HUD Editor", b -> {
            com.zenith.client.gui.hud.editor.HudEditor.getInstance().toggle();
        }));
        settingsPanel.add(new Button("Reset HUD Layout", b -> {
            com.zenith.client.gui.hud.HudManager.getInstance().applyLayout();
        }));
    }

    @Override
    public void init(int w, int h) {
        tabs.x = 20; tabs.y = 30; tabs.width = w - 40; tabs.height = 22;
        layoutPanel(homePanel, 20, 64, w-40);
        layoutPanel(flipPanel, 20, 64, w-40);
        layoutPanel(failPanel, 20, 64, w-40);
        layoutPanel(macroPanel, 20, 64, w-40);
        layoutPanel(settingsPanel, 20, 64, w-40);
    }

    private void layoutPanel(List<Component> panel, float ox, float oy, float maxW) {
        float y = oy;
        for (Component c : panel) {
            if (c instanceof Label) { c.width = maxW; c.height = 11; }
            else if (c instanceof Button) { c.width = Math.min(maxW - 20, 180); c.height = 18; }
            else if (c instanceof ZenithToggle) { c.width = maxW - 20; c.height = 16; }
            c.x = ox + 10; c.y = y;
            y += c.height + 4;
        }
    }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        Theme t = ThemeManager.getInstance().current();
        int w = ctx.screenWidth, h = ctx.screenHeight;
        // Background dimming + panel.
        ctx.fillRect(0, 0, w, h, new Color4f(0,0,0,0.55f));
        float pw = Math.min(460, w - 40);
        float ph = Math.min(360, h - 40);
        float px = w/2f - pw/2f;
        float py = h/2f - ph/2f;
        ctx.fillRect(px, py, pw, ph, t.background);
        ctx.drawOutline(px, py, pw, ph, 1.5f, t.border);
        ctx.drawString(title, px + 12, py + 10, t.accent, true);
        ctx.drawString("Press ESC to close", px + pw - 100, py + ph - 16, t.textSecondary, false);

        // Translate for children by adjusting positions temporarily.
        tabs.x = px + 10; tabs.y = py + 30; tabs.width = pw - 20;

        // Render tabs at their absolute position.
        tabs.hovered = tabs.isMouseOver(mx, my);
        tabs.render(ctx, mx, my, td);

        // Render active panel with an offset clip.
        List<Component> active = activePanel();
        float startX = px + 10;
        float startY = py + 60;
        for (Component c : active) {
            float ox = c.x, oy = c.y;
            c.x = startX + (c.x - 20); // panel origin was at 20 during layout
            c.y = startY + (c.y - 64);
            if (c.y + c.height > py + ph - 24) break;
            c.hovered = c.isMouseOver(mx, my);
            c.render(ctx, mx, my, td);
            c.x = ox; c.y = oy;
        }

        // Status line at bottom
        var fd = FailsafeManager.getInstance().debugData();
        var fl = FlipEngine.getInstance().debugData();
        String status = String.format("Failsafe: %s (%s)   Flipper: %s profit=%d",
                fd.currentSeverity, fd.active ? fd.activeType.displayName() : "clear",
                fl.running ? "ON" : "off", fl.sessionProfit);
        ctx.drawString(status, px + 12, py + ph - 24, t.textSecondary, false);
    }

    private List<Component> activePanel() {
        return switch (current) {
            case "flipper"  -> flipPanel;
            case "failsafe" -> failPanel;
            case "macros"   -> macroPanel;
            case "settings" -> settingsPanel;
            default         -> homePanel;
        };
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        tabs.mouseClicked(mx, my, button);
        for (Component c : activePanel()) c.mouseClicked(mx, my, button);
        super.mouseClicked(mx, my, button);
    }

    private static Color4f accent()   { return ThemeManager.getInstance().current().accent; }
    private static Color4f secondary(){ return ThemeManager.getInstance().current().textSecondary; }
}
