package com.zenith.client.gui.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Loads/saves HUD layout from config/hud_layout.json. */
public final class HudLayoutManager {
    private static final HudLayoutManager INSTANCE = new HudLayoutManager();
    private static final Path FILE = Paths.get("config","hud_layout.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private HudLayout layout = new HudLayout();

    public static HudLayoutManager getInstance() { return INSTANCE; }

    public void init() { load(); }
    public HudLayout layout() { return layout; }

    public void load() {
        if (FileUtils.fileExists(FILE)) {
            try { layout = gson.fromJson(FileUtils.readString(FILE), HudLayout.class); }
            catch (Exception e) { ZenithClient.LOGGER.warn("[HudLayout] Failed to load, using defaults", e); layout = new HudLayout(); }
        }
        if (layout == null) layout = new HudLayout();
    }

    public void save() {
        try { FileUtils.writeString(FILE, gson.toJson(layout)); }
        catch (Exception e) { ZenithClient.LOGGER.error("[HudLayout] Save failed", e); }
    }
}
