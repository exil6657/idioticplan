package com.zenith.client.gui.hud.panels;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * Farming skill-stats HUD panel. Phase 13 (farming macros) populates the data;
 * skeleton laid out here so the panel is already registered/draggable.
 *
 * <p>Layout mirrors the reference screenshot: counter, crops/min, coins/h,
 * blocks/s, skill level bar, XP/h, Yaw/Pitch debug.</p>
 */
public class FarmingStatsPanel extends HudPanel {
    {
        panelId = "FarmingStatsPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 220; }
    @Override public float getDefaultHeight() { return 110; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var theme = ThemeManager.getInstance().current();
        float y = this.y + 2;
        String header = "-Farming ";
        ctx.drawString(header, x + 4, y, new Color4f(1f, 0.85f, 0.2f, 1f), true);
        y += 12;
        ctx.drawString("  Cultivating: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Crops/min: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Coins/h: 0 (Bazaar)", x + 4, y, theme.accent, true); y += 10;
        ctx.drawString("  Blocks/s: 0.0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Farming Level:", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Farming XP/h: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString(String.format("Yaw: %.2f  Pitch: %.2f", 0f, 0f),
                x + 4, y, new Color4f(1f, 0.65f, 0.2f, 1f), true);
    }
}
