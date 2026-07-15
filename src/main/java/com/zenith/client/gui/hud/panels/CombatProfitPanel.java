package com.zenith.client.gui.hud.panels;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * Generic combat profit-tracker panel. The reference screenshot was "Ghost
 * Profit Tracker"; the same panel is reused for ghosts, endermen, dragons,
 * etc. by changing the title string and registering a drop-list provider.
 *
 * <p>Sections: dynamic title, itemised drop list (count × name × value),
 * Kills / Kills-since-rare / Max Combo / XP Gained / Avg Magic Find /
 * Bestiary, then Session Profit / Profit Per Hour / Total Uptime footer.</p>
 */
public class CombatProfitPanel extends HudPanel {

    private static volatile String title = "Combat Profit";

    public static void setTitle(String t) { title = t == null ? "Combat Profit" : t; }

    {
        panelId = "CombatProfitPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 250; }
    @Override public float getDefaultHeight() { return 130; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var theme = ThemeManager.getInstance().current();
        float y = this.y + 2;
        ctx.drawString(title, x + 4, y, new Color4f(1f, 0.85f, 0.2f, 1f), true); y += 12;
        ctx.drawString("  (drop list populated by active combat macro)", x + 4, y, theme.textSecondary, false); y += 10;
        ctx.drawString("Kills: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Max Kill Combo: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Combat XP Gained: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Average Magic Find: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Session Profit: 0 coins", x + 4, y, new Color4f(1f, 0.55f, 0.1f, 1f), true); y += 10;
        ctx.drawString("Profit Per Hour: 0 coins", x + 4, y, new Color4f(1f, 0.55f, 0.1f, 1f), true); y += 10;
        ctx.drawString("Total Uptime: 0s", x + 4, y, theme.textSecondary, false);
    }
}
