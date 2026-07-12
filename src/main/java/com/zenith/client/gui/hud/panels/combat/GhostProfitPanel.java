package com.zenith.client.gui.hud.panels.combat;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * "Ghost Profit Tracker"-style panel: itemised drop list with counts × coin
 * value, kills/combo/XP/MF footer, session profit, P/H, uptime.
 *
 * <p>Populated in Phase 15 (combat macros).</p>
 */
public class GhostProfitPanel extends HudPanel {
    {
        panelId = "GhostProfitPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 240; }
    @Override public float getDefaultHeight() { return 120; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var theme = ThemeManager.getInstance().current();
        float y = this.y + 2;
        ctx.drawString("Ghost Profit Tracker", x + 4, y, new Color4f(1f, 0.85f, 0.2f, 1f), true); y += 12;
        ctx.drawString("  (drop list populated in combat phase)", x + 4, y, theme.textSecondary, false); y += 10;
        ctx.drawString("Kills: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Max Kill Combo: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Combat XP Gained: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Average Magic Find: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Session Profit: 0 coins", x + 4, y, new Color4f(1f, 0.55f, 0.1f, 1f), true); y += 10;
        ctx.drawString("Profit Per Hour: 0 coins", x + 4, y, new Color4f(1f, 0.55f, 0.1f, 1f), true); y += 10;
        ctx.drawString("Total Uptime: 0s", x + 4, y, theme.textSecondary, false);
    }
}
