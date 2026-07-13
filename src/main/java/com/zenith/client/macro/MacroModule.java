package com.zenith.client.macro;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.BreakEndEvent;
import com.zenith.client.core.event.events.BreakStartEvent;
import com.zenith.client.core.event.events.ClientTickEvent;
import com.zenith.client.core.event.events.FailsafeTriggerEvent;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.reaction.actions.RepathReactionAction;
import com.zenith.client.flipping.break_.BreakScheduler;
import net.minecraft.client.Minecraft;

/**
 * Base class for all Zenith macros. Provides lifecycle (start/pause/resume/stop/break),
 * failsafe integration (auto-pause on PAUSE+ tier, auto-resume after REPATH), and
 * automatic registration as a {@link RepathReactionAction.DestinationProvider} so the
 * failsafe system knows where to walk back to after a teleport/respawn.
 *
 * <p>Subclasses implement {@link #onTick()}, {@link #onStart()}, {@link #onStop()},
 * {@link #destinationX()/Y()/Z()/description()}, and the optional hooks.</p>
 *
 * <p>Master rules §6 (state machines) and §10 (research first) apply.</p>
 */
public abstract class MacroModule implements RepathReactionAction.DestinationProvider {

    public enum MacroState { IDLE, STARTING, RUNNING, PAUSED, ON_BREAK, STOPPING, ERROR }

    private volatile MacroState state = MacroState.IDLE;
    private long startedAtMs;
    private long pausedAtMs;
    private long totalPausedMs;
    private String lastError = "";
    private boolean registeredFailsafe;

    // ---- Lifecycle API (subclass overrides) ----

    /** Unique id, e.g. "farming:melon". */
    public abstract String id();
    /** Human-readable name for HUD/dashboard, e.g. "Melon Farming". */
    public abstract String displayName();
    /** Icon glyph (emoji/Unicode) for the HUD panel header. */
    public abstract String icon();
    /** Skill family, e.g. "Farming", "Mining", "Combat", "Fishing", "Foraging". */
    public abstract String skillFamily();

    /** Called once when the macro is started (path to start location, equip tool, etc.). */
    protected abstract void onStart();
    /** Called every client tick while RUNNING. Do your macro logic here. */
    protected abstract void onTick();
    /** Called once when the macro is stopped (cleanup, release keys, etc.). */
    protected abstract void onStop();
    /** Called each time a scheduled break begins (stop farming, move to safe spot if needed). */
    protected void onBreakStart(long breakMs) {}
    /** Called when a scheduled break ends (resume position). */
    protected void onBreakEnd() {}
    /** Called when a failsafe pauses the macro (reaction is already running; release any held clicks). */
    protected void onFailsafePause() {}
    /** Called after failsafe clears AND repath completes (return to ticking). */
    protected void onFailsafeResume() {}

    @Override
    public abstract double destX();
    @Override
    public abstract double destY();
    @Override
    public abstract double destZ();
    @Override
    public String destinationDescription() { return displayName() + " anchor"; }

    // ---- Public controls ----

    public final void start() {
        if (state != MacroState.IDLE && state != MacroState.ERROR) {
            ZenithClient.LOGGER.warn("[Macro] {} already running (state={})", id(), state);
            return;
        }
        ZenithChat.getInstance().info("Starting {} macro...", displayName());
        state = MacroState.STARTING;
        startedAtMs = System.currentTimeMillis();
        totalPausedMs = 0;
        lastError = "";
        try {
            registerFailsafeHookOnce();
            RepathReactionAction.setProvider(this);
            onStart();
            state = MacroState.RUNNING;
            // Update the HUD stats panel with our icon/activity/skill family.
            try {
                var panelClass = Class.forName("com.zenith.client.gui.hud.panels.FarmingStatsPanel");
                var m = panelClass.getMethod("setActiveMacro", String.class, String.class, String.class);
                m.invoke(null, icon(), displayName(), skillFamily());
            } catch (Throwable ignored) {}
            ZenithClient.LOGGER.info("[Macro] {} started.", id());
        } catch (Throwable t) {
            state = MacroState.ERROR;
            lastError = t.getMessage();
            ZenithClient.LOGGER.error("[Macro] {} start failed", id(), t);
            ZenithChat.getInstance().error("Macro {} failed to start: {}", displayName(), t.getMessage());
        }
    }

    public final void stop(String reason) {
        if (state == MacroState.IDLE) return;
        ZenithChat.getInstance().info("Stopping {} macro ({})...", displayName(), reason == null ? "manual" : reason);
        state = MacroState.STOPPING;
        try { onStop(); }
        catch (Throwable t) { ZenithClient.LOGGER.error("[Macro] {} stop threw", id(), t); }
        releaseKeys();
        RepathReactionAction.setProvider(null);
        state = MacroState.IDLE;
    }

    public final void pause(String reason) {
        if (state != MacroState.RUNNING && state != MacroState.ON_BREAK) return;
        state = MacroState.PAUSED;
        pausedAtMs = System.currentTimeMillis();
        releaseKeys();
        try { onFailsafePause(); }
        catch (Throwable t) { ZenithClient.LOGGER.error("[Macro] {} onFailsafePause threw", id(), t); }
        ZenithChat.getInstance().warn("Macro {} paused: {}", displayName(), reason);
    }

    public final void resume() {
        if (state != MacroState.PAUSED) return;
        totalPausedMs += System.currentTimeMillis() - pausedAtMs;
        state = MacroState.RUNNING;
        try { onFailsafeResume(); }
        catch (Throwable t) { ZenithClient.LOGGER.error("[Macro] {} resume threw", id(), t); }
        ZenithChat.getInstance().success("Macro {} resumed.", displayName());
    }

    public final boolean isRunning() { return state == MacroState.RUNNING; }
    public final MacroState state() { return state; }
    public final long sessionMs() {
        long end = System.currentTimeMillis();
        if (state == MacroState.PAUSED) end = pausedAtMs;
        return Math.max(0, end - startedAtMs - totalPausedMs);
    }
    public final String lastError() { return lastError; }

    // ---- Internal tick dispatch (called by MacroManager) ----

    public final void dispatchTick() {
        if (state != MacroState.RUNNING) return;
        // Skip ticks if macros are paused at the failsafe PAUSE+ tier.
        if (FailsafeManager.getInstance().areMacrosPaused()) {
            if (state == MacroState.RUNNING) pause("failsafe");
            return;
        }
        // Skip ticks during scheduled breaks.
        if (BreakScheduler.getInstance().isOnBreak()) {
            if (state == MacroState.RUNNING) {
                state = MacroState.ON_BREAK;
                onBreakStart(0L);
            }
            return;
        }
        if (state == MacroState.ON_BREAK) {
            state = MacroState.RUNNING;
            onBreakEnd();
        }
        try { onTick(); }
        catch (Throwable t) {
            ZenithClient.LOGGER.error("[Macro] {} tick threw", id(), t);
            lastError = t.getMessage();
        }
    }

    // ---- Helpers ----

    protected final void releaseKeys() {
        try {
            var keys = com.zenith.client.engine.input.InputEngine.getInstance().keys();
            if (keys != null) keys.halt();
        } catch (Throwable ignored) {}
        try {
            com.zenith.client.engine.eyes.ZenithEyes.getInstance().setEnabled(true);
        } catch (Throwable ignored) {}
        try {
            com.zenith.client.engine.path.ZenithPath.getInstance().stop();
        } catch (Throwable ignored) {}
    }

    private void registerFailsafeHookOnce() {
        if (registeredFailsafe) return;
        registeredFailsafe = true;
        com.zenith.client.core.event.ZenithEventBus.getInstance().register(new Object() {
            @SubscribeEvent
            public void onBreakStart(BreakStartEvent ev) { if (isRunning()) { state = MacroState.ON_BREAK; onBreakStart(ev.getScheduledDurationMs()); } }
            @SubscribeEvent
            public void onBreakEnd(BreakEndEvent ev)   { if (state == MacroState.ON_BREAK) { state = MacroState.RUNNING; onBreakEnd(); } }
        });
    }
}
