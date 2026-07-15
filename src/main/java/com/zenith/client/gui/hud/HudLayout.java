package com.zenith.client.gui.hud;

import java.util.HashMap;
import java.util.Map;

/**
 * Persisted HUD layout: positions, visibility, sizes per panel id.
 * Loaded/saved by {@link HudLayoutManager}.
 */
public class HudLayout {
    public static final int VERSION = 1;
    public int version = VERSION;
    public Map<String, PanelEntry> panels = new HashMap<>();

    public static class PanelEntry {
        public float offsetX, offsetY;
        public HudPanelAnchor anchor = HudPanelAnchor.TOP_LEFT;
        public float width, height;
        public boolean visible = true;
    }
}
