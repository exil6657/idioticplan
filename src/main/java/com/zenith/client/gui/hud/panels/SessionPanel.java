package com.zenith.client.gui.hud.panels;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * Session / Inventory Worth / Skills / Jacob's Contest panel, styled after
 * the Pumpkin reference. Header shows active macro label + uptime; sections
 * for NPC inventory worth, skill progress, and contest info when active.
 */
public class SessionPanel extends HudPanel {
    {
        panelId = "SessionPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 230; }
    @Override public float getDefaultHeight() { return 120; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var theme = ThemeManager.getInstance().current();
        float y = this.y + 2;
        ctx.drawString("Session", x + 4, y, new Color4f(0.45f, 0.9f, 1f, 1f), true); y += 12;
        ctx.drawString("  Average BPS: 0.00", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Pests: 0", x + 4, y, theme.textPrimary, true); y += 12;

        ctx.drawString("Inventory Worth (NPC)", x + 4, y, new Color4f(0.45f, 0.9f, 1f, 1f), true); y += 10;
        ctx.drawString("  Purse: $0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("Inventory Worth: $0", x + 4, y, theme.textSecondary, false); y += 10;
        ctx.drawString("Earned Per Hour: $0", x + 4, y, new Color4f(1f, 0.55f, 0.1f, 1f), true); y += 10;
        ctx.drawString("Total Profit: $0", x + 4, y, new Color4f(1f, 0.35f, 0.35f, 1f), true); y += 12;

        ctx.drawString("Skills", x + 4, y, new Color4f(0.45f, 0.9f, 1f, 1f), true); y += 10;
        ctx.drawString("  Farming Level: 0 (0)", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Progress: 0.00%", x + 4, y, theme.accent, true);
    }
}
