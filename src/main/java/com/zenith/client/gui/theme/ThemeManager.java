package com.zenith.client.gui.theme;

import com.zenith.client.config.ConfigManager;

/** Manages active theme and persists theme choice to config. */
public final class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();
    private Theme current;

    private ThemeManager() { current = Theme.dark(); }
    public static ThemeManager getInstance() { return INSTANCE; }

    public void init() {
        String name = ConfigManager.getInstance().main().theme;
        setTheme(name);
    }

    public Theme current() { return current; }

    public void setTheme(String name) {
        switch (name == null ? "dark" : name.toLowerCase()) {
            case "light" -> current = Theme.light();
            default      -> current = Theme.dark();
        }
        current.name = name == null ? "dark" : name.toLowerCase();
        ConfigManager.getInstance().main().theme = current.name;
        ConfigManager.getInstance().requestSave();
    }

    public void setCustomHue(float hue) {
        ConfigManager.getInstance().main().customThemeHue = hue;
        ConfigManager.getInstance().requestSave();
    }
}
