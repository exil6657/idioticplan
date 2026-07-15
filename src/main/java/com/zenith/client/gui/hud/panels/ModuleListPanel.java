package com.zenith.client.gui.hud.panels;

import com.zenith.client.core.module.ModuleManager;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;
import com.zenith.client.gui.theme.ThemeManager;
import java.util.stream.Collectors;

public class ModuleListPanel extends HudPanel {
    { panelId = "module_list"; width=getDefaultWidth(); height=getDefaultHeight(); }
    @Override public float getDefaultWidth() { return 120; }
    @Override public float getDefaultHeight() { return 120; }
    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var t = ThemeManager.getInstance().current();
        float yy = y;
        var mods = ModuleManager.getInstance().getAll().stream()
                .filter(m -> m.isEnabled())
                .sorted((a,b) -> Float.compare(ctx.stringWidth(b.getDisplayName()), ctx.stringWidth(a.getDisplayName())))
                .collect(Collectors.toList());
        for (var m : mods) {
            ctx.drawString(m.getDisplayName(), x, yy, t.textPrimary, true);
            yy += 10;
        }
        height = Math.max(getDefaultHeight(), 10 + mods.size()*10);
    }
}
