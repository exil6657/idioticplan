package com.zenith.client.core.module;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.module.settings.Setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton registry of all {@link Module} instances.
 *
 * <p>{@link #registerAll()} is called from the main init (Phase 2+). Subsequent
 * phases register their modules during their own init steps.</p>
 */
public final class ModuleManager {

    private static ModuleManager instance;

    private final Map<String, Module> byId = new LinkedHashMap<>();
    private boolean initialised = false;

    private ModuleManager() {}

    public static ModuleManager getInstance() {
        if (instance == null) instance = new ModuleManager();
        return instance;
    }

    /** Called once at startup. Phase 2 registers built-in core modules; macro phases add more. */
    public void registerAll() {
        // Phase 2: no production modules yet — core scaffolding only.
        // Subsystems in later phases call register(...) during their init.
        initialised = true;
        ZenithClient.LOGGER.info("[ModuleManager] Initialized ({} modules registered)", byId.size());
    }

    public void register(Module m) {
        if (m == null) return;
        if (byId.putIfAbsent(m.getId(), m) != null) {
            ZenithClient.LOGGER.warn("[ModuleManager] Duplicate module id '{}' ignored", m.getId());
            return;
        }
        if (m.isEnabledByDefault()) m.enable();
    }

    public Module getById(String id) { return byId.get(id); }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getByClass(Class<T> cls) {
        for (Module m : byId.values()) {
            if (cls.isInstance(m)) return (T) m;
        }
        return null;
    }

    public Collection<Module> getAll() { return Collections.unmodifiableCollection(byId.values()); }

    public List<Module> getByCategory(ModuleCategory cat) {
        List<Module> out = new ArrayList<>();
        for (Module m : byId.values()) if (m.getCategory() == cat) out.add(m);
        return out;
    }

    public void tickAll() {
        for (Module m : byId.values()) if (m.isEnabled()) {
            try { m.onTick(); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[ModuleManager] tick({}) failed", m.getId(), t);
            }
        }
    }

    public void renderAll(float tickDelta) {
        for (Module m : byId.values()) if (m.isEnabled()) {
            try { m.onRender(tickDelta); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[ModuleManager] render({}) failed", m.getId(), t);
            }
        }
    }

    public int count() { return byId.size(); }
    public boolean isInitialised() { return initialised; }

    /** @return flattened list of every setting across all modules (for the config GUI). */
    public List<Setting<?>> allSettings() {
        List<Setting<?>> out = new ArrayList<>();
        for (Module m : byId.values()) out.addAll(m.getSettings());
        return out;
    }
}
