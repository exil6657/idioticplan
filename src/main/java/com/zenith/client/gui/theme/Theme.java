package com.zenith.client.gui.theme;

import com.zenith.client.engine.render.Color4f;

/**
 * Colour palette for the current GUI/HUD theme. Dark/light/custom are predefined;
 * ThemeManager (Phase 4) lets users customise and persists to config.
 */
public final class Theme {

    public String name = "dark";
    public Color4f accent       = new Color4f(0.45f, 0.30f, 0.95f, 1f);  // purple
    public Color4f background   = new Color4f(0.07f, 0.07f, 0.09f, 0.92f);
    public Color4f panel        = new Color4f(0.11f, 0.11f, 0.14f, 0.94f);
    public Color4f panelAlt     = new Color4f(0.15f, 0.15f, 0.19f, 0.94f);
    public Color4f border       = new Color4f(0.22f, 0.22f, 0.28f, 1f);
    public Color4f textPrimary  = new Color4f(0.95f, 0.95f, 0.97f, 1f);
    public Color4f textSecondary= new Color4f(0.60f, 0.60f, 0.68f, 1f);
    public Color4f success      = new Color4f(0.30f, 0.82f, 0.45f, 1f);
    public Color4f warning      = new Color4f(0.95f, 0.72f, 0.18f, 1f);
    public Color4f error        = new Color4f(0.90f, 0.25f, 0.30f, 1f);

    public static Theme dark() {
        Theme t = new Theme(); t.name="dark"; return t;
    }

    public static Theme light() {
        Theme t = new Theme();
        t.name="light";
        t.accent = new Color4f(0.35f, 0.22f, 0.85f, 1f);
        t.background = new Color4f(0.93f,0.93f,0.96f,0.92f);
        t.panel = new Color4f(0.97f,0.97f,0.99f,0.94f);
        t.panelAlt = new Color4f(0.88f,0.88f,0.92f,0.94f);
        t.border = new Color4f(0.75f,0.75f,0.8f,1f);
        t.textPrimary = new Color4f(0.1f,0.1f,0.12f,1f);
        t.textSecondary = new Color4f(0.4f,0.4f,0.46f,1f);
        return t;
    }

    public Color4f withAccentHue(float hue) {
        // Phase 4: simple HSL hue rotation stub; full HSL in Phase 5.
        return accent;
    }
}
