package com.zenith.client.gui.hud.panels;

import com.zenith.client.ZenithClientInfo;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;

public class WatermarkPanel extends HudPanel {
    { panelId = "watermark"; width=getDefaultWidth(); height=getDefaultHeight(); }
    @Override public float getDefaultWidth() { return 120; }
    @Override public float getDefaultHeight() { return 10; }
    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var t = ThemeManager.getInstance().current();
        ctx.drawString("Zenith v"+ ZenithClientInfo.VERSION, x, y, t.accent, true);
    }
}
