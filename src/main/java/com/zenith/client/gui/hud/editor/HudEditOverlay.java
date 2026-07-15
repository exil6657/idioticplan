package com.zenith.client.gui.hud.editor;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

/** Visual overlay drawn around HUD panels in edit mode (dashed border + name). */
public final class HudEditOverlay {
    public void render(GuiDrawContext ctx, Iterable<HudPanel> panels) {
        var t = ThemeManager.getInstance().current();
        for (var p : panels) {
            ctx.drawOutline(p.x, p.y, p.width, p.height, 1f, t.accent);
            ctx.fillRect(p.x, p.y - 9, p.width, 9, t.accent.withAlpha(0.85f));
            ctx.drawString(p.panelId, p.x+2, p.y-8, new Color4f(1,1,1,1), true);
        }
        // Grid hint
        for (int x = 0; x < ctx.screenWidth; x += SnapEngine.GRID*10) {
            ctx.fillRect(x, 0, 0.4f, ctx.screenHeight, new Color4f(1,1,1,0.04f));
        }
        for (int y = 0; y < ctx.screenHeight; y += SnapEngine.GRID*10) {
            ctx.fillRect(0, y, ctx.screenWidth, 0.4f, new Color4f(1,1,1,0.04f));
        }
        ctx.drawString("HUD EDIT MODE — drag panels, ESC to exit, press S to save",
                ctx.screenWidth/2f - 140, ctx.screenHeight - 18, new Color4f(1,1,1,0.8f), true);
    }
}
