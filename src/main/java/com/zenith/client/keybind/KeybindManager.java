package com.zenith.client.keybind;

import com.zenith.client.ZenithClient;
import com.zenith.client.config.ConfigManager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registers Zenith's keybinds with Fabric and routes key-press events.
 *
 * <p>Phase 2: config plumbing only — Fabric KeyBinding registration happens in
 * Phase 3 when the ClientTickEvents/Key event hooks are wired (requires the
 * MixinKeyboardHandler to be fully implemented). This class provides a stable
 * facade the rest of Phase 2 code can already call.</p>
 */
public final class KeybindManager {

    private static KeybindManager instance;

    private final KeybindConfig config = new KeybindConfig();
    private final Map<ZenithKeybinds, List<Runnable>> listeners = new EnumMap<>(ZenithKeybinds.class);

    private KeybindManager() {}

    public static KeybindManager getInstance() {
        if (instance == null) instance = new KeybindManager();
        return instance;
    }

    public void init() {
        // Load persisted binds from config.
        var main = ConfigManager.getInstance().main();
        for (ZenithKeybinds k : ZenithKeybinds.values()) {
            String val = main.keybinds.get(k.getConfigId());
            if (val != null) config.set(k, val);
        }
        List<String> conflicts = KeybindConflictChecker.findConflicts(config);
        for (String c : conflicts) {
            ZenithClient.LOGGER.warn("[Keybind] Conflict: {}", c);
        }
        ZenithClient.LOGGER.info("[KeybindManager] Initialized ({} binds)", ZenithKeybinds.values().length);
    }

    public KeybindConfig getConfig() { return config; }

    public void onKeyPress(ZenithKeybinds key) {
        List<Runnable> ls = listeners.get(key);
        if (ls == null) return;
        for (Runnable r : ls) {
            try { r.run(); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Keybind] handler for {} failed", key, t);
            }
        }
    }

    public void register(ZenithKeybinds key, Runnable action) {
        listeners.computeIfAbsent(key, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(action);
    }

    /** Persist a new bind and request a config save. */
    public void rebind(ZenithKeybinds key, String glfwName) {
        config.set(key, glfwName);
        ConfigManager.getInstance().main().keybinds.put(key.getConfigId(), glfwName);
        ConfigManager.getInstance().requestSave();
    }
}
