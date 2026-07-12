package com.zenith.client.gui.hud.editor;

import com.zenith.client.gui.hud.HudLayout;
import com.zenith.client.gui.hud.HudLayoutManager;
import com.zenith.client.gui.hud.HudPanel;

final class HudLayoutSaver {
    static void save() {
        var mgr = HudLayoutManager.getInstance();
        HudLayout layout = mgr.layout();
        for (var p : com.zenith.client.gui.hud.HudManager.getInstance().allPanels()) {
            var e = layout.panels.computeIfAbsent(p.panelId, k -> new HudLayout.PanelEntry());
            e.offsetX = p.x; e.offsetY = p.y; e.width = p.width; e.height = p.height;
            e.anchor = p.anchor; e.visible = p.visible;
        }
        mgr.save();
    }
}
