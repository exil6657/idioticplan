package com.zenith.client.gui.hud;

import com.zenith.client.gui.component.Component;

/**
 * Base class for HUD panels that can be positioned, dragged, and resized in
 * the HUD editor. Concrete panels (WatermarkPanel etc.) override render().
 */
public abstract class HudPanel extends Component {
    public String panelId;
    public HudPanelAnchor anchor = HudPanelAnchor.TOP_LEFT;
    public boolean pinned = true;
    public float offsetX, offsetY; // offset from anchor

    public abstract float getDefaultWidth();
    public abstract float getDefaultHeight();
}
