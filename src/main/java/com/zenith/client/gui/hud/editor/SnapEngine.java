package com.zenith.client.gui.hud.editor;

import com.zenith.client.gui.hud.HudPanel;

/**
 * Snap-to-grid and snap-to-edge for HUD editor. Panels snap to a 4-pixel grid by
 * default and to screen edges/corners when within 8 pixels.
 */
public final class SnapEngine {
    public static final int GRID = 4;
    public static final int EDGE = 8;

    public static void applySnap(HudPanel p, int sw, int sh) {
        // Snap to grid
        p.x = Math.round(p.x / GRID) * GRID;
        p.y = Math.round(p.y / GRID) * GRID;
        // Snap to edges
        if (p.x < EDGE) p.x = 0;
        if (p.y < EDGE) p.y = 0;
        if (p.x + p.width > sw - EDGE) p.x = sw - p.width;
        if (p.y + p.height > sh - EDGE) p.y = sh - p.height;
    }
}
