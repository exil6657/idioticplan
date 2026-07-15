package com.zenith.client.macro;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for all {@link MacroModule}s. Tick loop (called from ModuleManager or
 * ClientTickDispatcher) drives dispatchTick() on the currently-running macro.
 * Only one macro runs at a time (you don't farm and fish simultaneously).
 */
public final class MacroManager {

    private static final MacroManager INSTANCE = new MacroManager();
    public static MacroManager getInstance() { return INSTANCE; }

    private final Map<String, MacroModule> byId = new LinkedHashMap<>();
    private MacroModule active;

    private MacroManager() {}

    public void register(MacroModule m) {
        if (m == null) return;
        byId.putIfAbsent(m.id(), m);
        ZenithClient.LOGGER.debug("[MacroManager] registered {}", m.id());
    }

    public Collection<MacroModule> all() { return Collections.unmodifiableCollection(byId.values()); }
    public MacroModule get(String id) { return byId.get(id); }
    public MacroModule active() { return active; }

    public boolean start(String id) {
        MacroModule m = byId.get(id);
        if (m == null) { ZenithChat.getInstance().error("Unknown macro: {}", id); return false; }
        if (active != null && active != m) {
            active.stop("switching to " + id);
        }
        m.start();
        active = m;
        return true;
    }

    public void stopAll(String reason) {
        if (active != null) {
            active.stop(reason);
            active = null;
        }
    }

    public void pauseAll(String reason) {
        for (MacroModule m : byId.values()) if (m.isRunning()) m.pause(reason);
    }

    public void resumeActive() {
        if (active != null && active.state() == MacroModule.MacroState.PAUSED) active.resume();
    }

    /** Called every client tick (from ModuleManager.tickAll or ClientTickDispatcher). */
    public void tick() {
        if (active != null) {
            // Auto-resume after failsafe clears: if active macro is PAUSED and
            // FailsafeManager says macros aren't paused, resume.
            if (active.state() == MacroModule.MacroState.PAUSED
                    && !com.zenith.client.failsafe.FailsafeManager.getInstance().areMacrosPaused()) {
                active.resume();
            }
            active.dispatchTick();
        }
    }
}
