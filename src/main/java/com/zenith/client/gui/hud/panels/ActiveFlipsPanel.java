package com.zenith.client.gui.hud.panels;

import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudPanel;

/** ActiveFlipsPanel — HUD panel (filled in by macro/flip/economy phases). */
public class ActiveFlipsPanel extends HudPanel {
    { panelId = "ActiveFlipsPanel"; width=getDefaultWidth(); height=getDefaultHeight(); }
    @Override public float getDefaultWidth() { return 100; }
    @Override public float getDefaultHeight() { return 12; }
    @Override public void render(GuiDrawContext ctx, int mx, int my, float td) {
        // Filled in later phases.
    }
}
