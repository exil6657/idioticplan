package com.zenith.client.gui.lang;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zenith.client.ZenithClient;
import com.zenith.client.config.ConfigManager;
import com.zenith.client.core.util.FileUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal i18n: loads {@code assets/zenithclient/lang/<code>.json} and falls back to en_gb.
 * Phase 4 stub: supports loading from resource + user override from config/lang/<code>.json.
 */
public final class LanguageManager {
    private static final LanguageManager INSTANCE = new LanguageManager();
    private final Gson gson = new Gson();
    private final Map<String,String> strings = new HashMap<>();
    private String current = "en_gb";

    public static LanguageManager getInstance() { return INSTANCE; }

    public void init() {
        current = ConfigManager.getInstance().main().language;
        load(current);
    }

    public String currentLang() { return current; }

    public void setLang(String code) {
        this.current = code;
        ConfigManager.getInstance().main().language = code;
        ConfigManager.getInstance().requestSave();
        load(code);
    }

    public String tr(String key) { return strings.getOrDefault(key, key); }
    public String tr(String key, Object... args) { return String.format(tr(key), args); }

    private void load(String code) {
        strings.clear();
        // Load en_gb first as base, then overlay requested language.
        loadResource("en_gb");
        if (!"en_gb".equals(code)) loadResource(code);
        // User overrides from config/lang/<code>.json.
        Path override = Paths.get("config", "lang", code + ".json");
        if (FileUtils.fileExists(override)) {
            try {
                JsonObject o = gson.fromJson(FileUtils.readString(override), JsonObject.class);
                if (o != null) for (var e : o.entrySet()) strings.put(e.getKey(), e.getValue().getAsString());
            } catch (Exception ex) {
                ZenithClient.LOGGER.warn("[Lang] Failed to load override {}", override, ex);
            }
        }
    }

    private void loadResource(String code) {
        String path = "/assets/zenithclient/lang/" + code + ".json";
        try (InputStream in = LanguageManager.class.getResourceAsStream(path)) {
            if (in == null) return;
            JsonObject o = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            for (Map.Entry<String, JsonElement> e : o.entrySet()) strings.put(e.getKey(), e.getValue().getAsString());
        } catch (Exception ex) {
            ZenithClient.LOGGER.warn("[Lang] Failed to load resource {}", path, ex);
        }
    }
}
