package com.zenith.client.gui.dashboard;

import com.zenith.client.gui.ZenithScreen;
import com.zenith.client.gui.component.GuiDrawContext;

/** Main dashboard GUI (opened by .z dashboard or Right-Ctrl). Phase 5 adds full tabs/widgets. */
public class DashboardScreen extends ZenithScreen {
    public DashboardScreen() { title = "Zenith Dashboard"; }

    @Override public void init(int w, int h) { /* Phase 5: add sidebar, tab panels */ }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        super.render(ctx, mx, my, td);
    }
}
