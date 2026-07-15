package com.zenith.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zenith.client.ZenithClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gson-backed JSON config manager. Holds the {@link MainConfig} (zenith.json)
 * plus additional per-subsystem config files registered at startup.
 *
 * <p>Saves are debounced: {@link #requestSave()} marks dirty and a single
 * write happens on the next tick via {@link #tick()}.</p>
 *
 * <p>Master rule §8: config persists via Gson JSON (13 files total).</p>
 */
public final class ConfigManager {

    /** The main zenith.json — preferences not fitting in subsystem files. */
    public static final class MainConfig {
        public int schemaVersion = 1;
        public String commandPrefix = ".z";
        public String language = "en_gb";
        public String theme = "dark";
        public float customThemeHue = 0.58f;
        public boolean openDashboardOnStart = false;
        public boolean devDataAuto = true;
        public Map<String, String> keybinds = new ConcurrentHashMap<>();
    }

    private static final ConfigManager INSTANCE = new ConfigManager();
    public static ConfigManager getInstance() { return INSTANCE; }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private MainConfig main = new MainConfig();
    private Path configDir;
    private final List<Entry<?>> entries = new ArrayList<>();
    private volatile boolean dirty;

    private ConfigManager() {}

    public void init() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            Path gameDir = mc.gameDirectory.toPath();
            configDir = gameDir.resolve("config").resolve("zenithclient");
            Files.createDirectories(configDir);
        } catch (Throwable t) {
            configDir = Path.of("config/zenithclient");
            try { Files.createDirectories(configDir); } catch (IOException ignored) {}
        }
        register("zenith", MainConfig.class, () -> main, (o) -> main = (MainConfig) o, new MainConfig());
        loadAll();
        ZenithClient.LOGGER.info("[Config] Loaded from {} ({} entries).", configDir, entries.size());
    }

    public MainConfig main() { return main; }
    public Path configDir() { return configDir; }

    public <T> void register(String filename, Class<T> type, java.util.function.Supplier<T> getter,
                             java.util.function.Consumer<T> setter, T initial) {
        entries.add(new Entry<>(filename + ".json", type, getter, setter, initial));
    }

    public void requestSave() { dirty = true; }

    public void tick() {
        if (!dirty) return;
        dirty = false;
        saveAll();
    }

    private void loadAll() { for (Entry<?> e : entries) load(e); }
    private void saveAll() { for (Entry<?> e : entries) save(e); }

    private <T> void load(Entry<T> e) {
        Path p = configDir.resolve(e.filename);
        if (!Files.exists(p)) { e.setter.accept(e.initial); save(e); return; }
        try (Reader r = Files.newBufferedReader(p)) {
            T v = gson.fromJson(r, e.type);
            if (v != null) { e.setter.accept(v); return; }
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[Config] Failed to load {}, writing defaults.", e.filename, t);
        }
        e.setter.accept(e.initial);
        save(e);
    }

    private <T> void save(Entry<T> e) {
        try {
            T v = e.getter.get();
            if (v == null) v = e.initial;
            Path tmp = configDir.resolve(e.filename + ".tmp");
            try (Writer w = Files.newBufferedWriter(tmp)) { gson.toJson(v, e.type, w); }
            Files.move(tmp, configDir.resolve(e.filename),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[Config] Failed to save {}.", e.filename, t);
        }
    }

    private static final class Entry<T> {
        final String filename; final Class<T> type;
        final java.util.function.Supplier<T> getter;
        final java.util.function.Consumer<T> setter;
        final T initial;
        Entry(String filename, Class<T> type, java.util.function.Supplier<T> getter,
              java.util.function.Consumer<T> setter, T initial) {
            this.filename=filename; this.type=type; this.getter=getter;
            this.setter=setter; this.initial=initial;
        }
    }
}
