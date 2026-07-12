package com.zenith.client.gui.hud.panels;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/**
 * Active-macro skill-stats HUD panel. The icon next to the title is DYNAMIC —
 * it is set by whichever macro module is currently running (🍉 melon, 🎃
 * pumpkin, 🥔 potato, ⛏ mining, 🎣 fishing, 🗡 combat, etc.). The user
 * explicitly said NOT to hardcode a watermelon; the screenshot was a design
 * reference, not a literal target.
 *
 * <p>Layout (mirrors the reference features, not the literal crop): activity
 * counter, per-unit rate (crops/min or blocks/s), coins/h with source label,
 * skill level + 20-char progress bar, skill XP/h, Yaw/Pitch debug in orange.</p>
 *
 * <p>Phase 13+ (farming/mining/combat macros) populates the live data; the
 * skeleton here just shows the header + placeholder lines so the panel is
 * registered/draggable.</p>
 */
public class FarmingStatsPanel extends HudPanel {

    /** Icon that swaps to match the active macro — defaults to "■" until set. */
    private static volatile String currentIcon = "■";
    private static volatile String currentActivity = "Idle";
    private static volatile String currentSkill = "Farming";

    public static void setActiveMacro(String icon, String activity, String skill) {
        currentIcon = icon == null ? "■" : icon;
        currentActivity = activity == null ? "Idle" : activity;
        currentSkill = skill == null ? "Farming" : skill;
    }

    {
        panelId = "FarmingStatsPanel";
        width = getDefaultWidth();
        height = getDefaultHeight();
    }

    @Override public float getDefaultWidth() { return 230; }
    @Override public float getDefaultHeight() { return 110; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var theme = ThemeManager.getInstance().current();
        float y = this.y + 2;

        String header = "-" + currentIcon + " " + currentActivity + " ";
        ctx.drawString(header, x + 4, y, new Color4f(1f, 0.85f, 0.2f, 1f), true); y += 12;
        ctx.drawString("  " + currentIcon + " " + currentSkill + " counter: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  Coins/h: 0 (Bazaar)", x + 4, y, theme.accent, true); y += 10;
        ctx.drawString("  Blocks/s: 0.0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString("  " + currentSkill + " Level:", x + 4, y, theme.textPrimary, true); y += 10;
        // 20-char progress bar placeholder.
        ctx.drawString("  ▮▮▮▮▮▮▮▮▯▯▯▯▯▯▯▯▯▯▯▯ 0.00%", x + 4, y, theme.textPrimary, false); y += 10;
        ctx.drawString("  " + currentSkill + " XP/h: 0", x + 4, y, theme.textPrimary, true); y += 10;
        ctx.drawString(String.format("Yaw: %.2f  Pitch: %.2f", 0f, 0f),
                x + 4, y, new Color4f(1f, 0.65f, 0.2f, 1f), true);
    }
}
