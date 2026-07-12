package com.zenith.client.gui;

import com.zenith.client.ZenithClient;
import com.zenith.client.ZenithClientInfo;
import com.zenith.client.config.ConfigManager;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.RenderHudEvent;
import com.zenith.client.engine.eyes.RotationDebugData;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.path.PathDebugData;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.animation.AnimationEngine;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudManager;
import com.zenith.client.gui.hud.panels.ModuleListPanel;
import com.zenith.client.gui.hud.panels.WatermarkPanel;
import com.zenith.client.gui.lang.LanguageManager;
import com.zenith.client.gui.theme.ThemeManager;
import com.zenith.client.gui.toast.ToastManager;
import net.minecraft.client.Minecraft;

/**
 * Central GUI engine. Subscribes to RenderHudEvent, initialises theme + animation
 * engine + dashboard screen + HUD panels, and draws HUD overlays each frame.
 */
public final class GuiEngine {

    private static GuiEngine instance;

    private final WatermarkPanel watermark = new WatermarkPanel();
    private final ModuleListPanel moduleList = new ModuleListPanel();
    private boolean showBrainView;
    private boolean registered;

    private ZenithScreen currentScreen;
    private boolean initialized;

    private GuiEngine() {}

    public static GuiEngine getInstance() {
        if (instance == null) instance = new GuiEngine();
        return instance;
    }

    public void init() {
        ThemeManager.getInstance().init();
        LanguageManager.getInstance().init();
        watermark.at(4, 4).size(120, 10);
        moduleList.at(4, 16).size(160, 200);
        HudManager.getInstance().init();
        if (!registered) {
            com.zenith.client.core.event.ZenithEventBus.getInstance().register(this);
            registered = true;
        }
        initialized = true;
        ZenithClient.LOGGER.info("[GuiEngine] Initialized (HUD panels, toasts, brain view, HUD editor).");
    }

    public boolean isInitialized() { return initialized; }

    public void openScreen(ZenithScreen screen) {
        if (currentScreen != null) currentScreen.onClose();
        currentScreen = screen;
    }

    public void closeScreen() {
        if (currentScreen != null) currentScreen.onClose();
        currentScreen = null;
    }

    public ZenithScreen getCurrentScreen() { return currentScreen; }

    public void toggleBrainView() { showBrainView = !showBrainView; }
    public boolean isBrainViewShown() { return showBrainView; }

    @SubscribeEvent
    public void onRenderHud(RenderHudEvent event) {
        AnimationEngine.getInstance().frame();
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        int w = event.screenWidth;
        int h = event.screenHeight;
        GuiDrawContext ctx = new GuiDrawContext(event.graphics, w, h, event.tickDelta);

        // Position right-anchored panels each frame so they follow window resize.
        moduleList.x = w - 124;
        // Watermark/module-list rendering is dispatched by HudManager via its own subscriber.

        // Brain view debug panel (toggle via .z debug brain)
        if (showBrainView) renderBrainView(ctx, w, h);

        // Toasts
        int yt = 40;
        for (var t : ToastManager.getInstance().active()) {
            int tw = (int) ctx.stringWidth(t.message());
            int color = switch ((t.color() >> 24) & 0xFF) { default -> 0xFFFFFFFF; };
            // color byte is actually unused in ToastManager right now; draw white.
            ctx.drawString(t.message(), w/2f - tw/2f, yt, new Color4f(1,1,1,1), true);
            yt += 12;
        }
    }

    private void renderBrainView(GuiDrawContext ctx, int w, int h) {
        int panelW = 400;
        int panelH = 290;
        float x = w/2f - panelW/2f;
        float y = h/2f - panelH/2f;
        var t = ThemeManager.getInstance().current();
        ctx.fillRect(x, y, panelW, panelH, t.background);
        ctx.drawOutline(x, y, panelW, panelH, 1f, t.border);
        ctx.drawString("Zenith Brain View", x+8, y+6, t.accent, true);

        RotationDebugData eyes = ZenithEyes.getInstance().debugData();
        PathDebugData path = ZenithPath.getInstance().debugData();
        var fail = com.zenith.client.failsafe.FailsafeManager.getInstance().debugData();
        float ty = y + 22;
        ctx.drawString(String.format("Eyes: state=%s tag=%s profile=%s", eyes.state(), eyes.activeTag(), eyes.profileId()),
                x+8, ty, t.textPrimary, false); ty += 11;
        ctx.drawString(String.format("  yaw/pitch: %.2f / %.2f → %.2f / %.2f (%.1f°/s)",
                eyes.currentYaw(), eyes.currentPitch(), eyes.targetYaw(), eyes.targetPitch(), eyes.angularVelocityDegPerSec()),
                x+8, ty, t.textSecondary, false); ty += 11;
        ctx.drawString(String.format("  flags: overshoot=%b correct=%b hesitate=%b fatigue=%.2f queued=%d pri=%s",
                eyes.overshooting(), eyes.correcting(), eyes.hesitating(), eyes.fatigue(), eyes.queued(), eyes.priority()),
                x+8, ty, t.textSecondary, false); ty += 14;

        ctx.drawString(String.format("Path: state=%s mode=%s speed=%.2fbps stuck=%b",
                path.state(), path.mode(), path.currentSpeedBps(), path.stuck()),
                x+8, ty, t.textPrimary, false); ty += 11;
        ctx.drawString(String.format("  nodes planned=%d executed=%d cost=%.1f dist=%.2f",
                path.plannedNodes(), path.executedNodes(), path.totalCost(), path.distanceToTarget()),
                x+8, ty, t.textSecondary, false); ty += 11;
        ctx.drawString(String.format("  reaction=%s compute=%dms", path.reaction(), path.computeMs()),
                x+8, ty, t.textSecondary, false); ty += 14;

        String failType = fail.activeType != null ? fail.activeType.displayName() : "—";
        ctx.drawString(String.format("Failsafe: active=%b sev=%s type=%s frozen=%b paused=%b",
                fail.active, fail.currentSeverity, failType, fail.inputFrozen, fail.macrosPaused),
                x+8, ty, fail.active ? new com.zenith.client.engine.render.Color4f(1f,0.4f,0.4f,1f) : t.textPrimary, false); ty += 11;
        ctx.drawString(String.format("  reason=%s", fail.activeReason.isEmpty() ? "—" : fail.activeReason),
                x+8, ty, t.textSecondary, false); ty += 11;
        ctx.drawString(String.format("  for=%dms reaction=%s totalTriggers=%d",
                fail.activeForMs, fail.reactionState, fail.triggersSinceStartup),
                x+8, ty, t.textSecondary, false); ty += 14;

        var flips = com.zenith.client.flipping.FlipEngine.getInstance().debugData();
        Color4f flipColor = flips.running ? t.textPrimary : new Color4f(0.55f,0.55f,0.55f,1f);
        ctx.drawString(String.format("Flipper: running=%b orders=%d listed=%d profit=%d",
                flips.running, flips.activeOrders, flips.listedOrders, flips.sessionProfit),
                x+8, ty, flipColor, false); ty += 11;
        ctx.drawString(String.format("  scans=%d queued=%d BIN=%d bazaar=%d break=%b",
                flips.scans, flips.candidateQueueDepth, flips.binItems, flips.bazaarItems, flips.onBreak),
                x+8, ty, t.textSecondary, false); ty += 14;

        ctx.drawString("Press .z debug brain to toggle", x+8, y+panelH-12, t.textSecondary, false);
    }
}
