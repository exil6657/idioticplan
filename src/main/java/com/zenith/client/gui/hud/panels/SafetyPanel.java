package com.zenith.client.gui.hud.panels;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.FailsafeType;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * Safety status panel — small 4×4 dot + status label that lives in the corner
 * of the HUD. Green = clear, amber = reaction running (wiggle/combat/obstruction/
 * respawn/repath), orange = paused or warping to island, red = actively
 * disconnecting. Clicking the panel toggles the brain view.
 */
public class SafetyPanel extends HudPanel {

    {
        panelId = "SafetyPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 150; }
    @Override public float getDefaultHeight() { return 12; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var mgr = FailsafeManager.getInstance();
        var dbg = mgr.debugData();

        Color4f dotColour = switch (dbg.currentSeverity) {
            case NONE                                            -> new Color4f(0.20f, 0.83f, 0.60f, 1.0f); // green
            case NOTIFY, WIGGLE_REACT, COMBAT, REMOVE_OBSTRUCTION,
                 INSTANT_RESPAWN, REPATH                         -> new Color4f(0.98f, 0.75f, 0.14f, 1.0f); // amber
            case PAUSE, WARP_ISLAND, WARP_HUB                    -> new Color4f(0.98f, 0.45f, 0.09f, 1.0f); // orange
            case DISCONNECT                                      -> new Color4f(0.94f, 0.27f, 0.27f, 1.0f); // red
        };

        float dotR = 3f;
        float cx = x + 6, cy = y + height/2f;
        ctx.fillRect(cx - dotR, cy - dotR, dotR*2, dotR*2, dotColour);

        String label = buildLabel(dbg);
        Color4f labelColour = dbg.active ? dotColour : ThemeManager.getInstance().current().textPrimary;
        ctx.drawString(label, cx + dotR + 4, cy - 4, labelColour, true);
    }

    private static String buildLabel(com.zenith.client.failsafe.FailsafeDebugData d) {
        if (!d.active) return "Safe";
        String type = d.activeType != null ? d.activeType.displayName() : "?";
        if (d.currentSeverity.ordinal() >= FailsafeStrictness.WARP_ISLAND.ordinal()) {
            return type + " → /is";
        }
        if (d.reactionState == null || d.reactionState.isEmpty()) return type;
        return type + " · " + d.reactionState;
    }
}
