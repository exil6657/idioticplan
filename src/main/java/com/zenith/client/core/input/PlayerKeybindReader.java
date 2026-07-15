package com.zenith.client.core.input;

import com.zenith.client.ZenithClient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reads the player's configured Minecraft keybinds.
 *
 * <p>Phase 2 note: concrete wiring to {@code net.minecraft.client.Options} via
 * {@code OptionsAccessor} lands in Phase 3 (once MixinOptions is fully
 * implemented). For Phase 2 we return defaults (WASD + standard controls) but
 * all callers MUST go through this class; no hard-coded GLFW values elsewhere.</p>
 */
public final class PlayerKeybindReader {

    private static PlayerKeybindReader instance;
    private final Map<KeybindMapper, Integer> lastResolved = new EnumMap<>(KeybindMapper.class);
    private final List<KeybindChangeListener> listeners = new CopyOnWriteArrayList<>();
    private long lastResolveMs = 0;

    private PlayerKeybindReader() {
        resetToDefaults();
    }

    public static PlayerKeybindReader getInstance() {
        if (instance == null) instance = new PlayerKeybindReader();
        return instance;
    }

    /** Glfw key code currently bound to the action (WASD etc.). Resolves from Options once per second. */
    public int getKey(KeybindMapper id) {
        long now = System.currentTimeMillis();
        if (now - lastResolveMs > 1000) {
            resolveFromOptions();
            lastResolveMs = now;
        }
        return lastResolved.getOrDefault(id, 0);
    }

    private void resolveFromOptions() {
        // Phase 3: use OptionsAccessor on Minecraft.getInstance().options to read key->code mappings.
        // Phase 2: leave defaults as populated below.
    }

    /** Default WASD layout — only used until Options wiring is in place. */
    private void resetToDefaults() {
        // GLFW key codes for reference (from org.lwjgl.glfw.GLFW):
        // W=87, A=65, S=83, D=68, SPACE=32, LSHIFT=340, LCTRL=341, E=69, T=84, /=47, F=70, Q=81
        lastResolved.put(KeybindMapper.FORWARD,   87);  // W
        lastResolved.put(KeybindMapper.BACK,      83);  // S
        lastResolved.put(KeybindMapper.LEFT,      65);  // A
        lastResolved.put(KeybindMapper.RIGHT,     68);  // D
        lastResolved.put(KeybindMapper.JUMP,      32);  // Space
        lastResolved.put(KeybindMapper.SNEAK,     340); // LShift
        lastResolved.put(KeybindMapper.SPRINT,    341); // LCtrl
        lastResolved.put(KeybindMapper.ATTACK,     0);  // mouse 0 — resolved by Phase 3
        lastResolved.put(KeybindMapper.USE,        1);  // mouse 1
        lastResolved.put(KeybindMapper.DROP,      81);  // Q
        lastResolved.put(KeybindMapper.INVENTORY, 69);  // E
        lastResolved.put(KeybindMapper.CHAT,      84);  // T
        lastResolved.put(KeybindMapper.COMMAND,   47);  // /
        lastResolved.put(KeybindMapper.SWAP_HANDS,70);  // F
        for (int i = 0; i < 9; i++) {
            // GLFW_KEY_1 = 49 ... GLFW_KEY_9 = 57
            lastResolved.put(KeybindMapper.valueOf("HOTBAR_" + (i+1)), 49 + i);
        }
    }

    /** Force a full re-resolve (called after the user rebinds keys in MC controls). */
    public void invalidate() {
        lastResolveMs = 0;
        for (KeybindChangeListener l : listeners) {
            try { l.onKeybindsChanged(); } catch (Throwable t) {
                ZenithClient.LOGGER.warn("[KeybindReader] listener failed", t);
            }
        }
    }

    public void addListener(KeybindChangeListener l) { if (l != null) listeners.add(l); }
    public void removeListener(KeybindChangeListener l) { listeners.remove(l); }
}
