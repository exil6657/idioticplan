package com.zenith.client.gui.hud.editor;

import com.zenith.client.gui.hud.HudPanel;

/** Tracks which panel is being dragged in the HUD editor and mouse-drag deltas. */
public final class DragController {
    private HudPanel dragging;
    private float dragStartX, dragStartY, panelStartX, panelStartY;

    public void startDrag(HudPanel panel, int mx, int my) {
        this.dragging = panel;
        this.dragStartX = mx; this.dragStartY = my;
        this.panelStartX = panel.x; this.panelStartY = panel.y;
    }
    public void drag(int mx, int my) {
        if (dragging == null) return;
        dragging.x = panelStartX + (mx - dragStartX);
        dragging.y = panelStartY + (my - dragStartY);
    }
    public void stopDrag() { dragging = null; }
    public boolean isDragging() { return dragging != null; }
    public HudPanel current() { return dragging; }
}
