package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.*;
import com.zenith.client.core.protection.BitsSpendBlocker;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.input.KeySimulator;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.failsafe.detection.*;
import com.zenith.client.failsafe.reaction.ReactionEngine;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator for the entire failsafe subsystem.
 *
 * <p>Each tick the manager asks every registered {@link AbstractDetector}
 * whether it is currently triggered. If so, the relevant {@link TriggerState}
 * is advanced up the severity ladder (subject to {@link FailsafeConfig#escalationStepMs}).
 * Severity changes fire {@link FailsafeTriggerEvent} on the event bus and feed
 * into the {@link ReactionEngine} for mistake-simulation / ban-action /
 * notification logic.</p>
 *
 * <p>When the highest active severity reaches a given threshold, the manager
 * pauses all macros, freezes the {@link KeySimulator}, tells {@link ZenithPath}
 * to stop, disables {@link ZenithEyes} automatic rotation, and (at the top of
 * the ladder) sends {@code /home}, {@code /hub}, or disconnects.</p>
 *
 * <p>Master doc §7: failsafes are the last line of defence — they must never
 * throw and they must never block the main thread for longer than a tick.</p>
 */
public final class FailsafeManager {

    private static FailsafeManager instance;

    public static FailsafeManager getInstance() {
        if (instance == null) instance = new FailsafeManager();
        return instance;
    }

    private final FailsafeConfig config = new FailsafeConfig();

    /** @return the currently active failsafe config (backed by the config file once loaded). */
    public FailsafeConfig config() { return config; }

    /** Replace the live config with a loaded-from-disk copy (called by ConfigManager post-init). */
    public void loadConfig(FailsafeConfig loaded) {
        if (loaded == null) return;
        // Copy the simple fields. detectors map is EnumMap<FailsafeType, DetectorSetting>; re-point.
        config.globalEnabled = loaded.globalEnabled;
        config.soundAlert = loaded.soundAlert;
        config.soundAlertName = loaded.soundAlertName;
        config.soundVolume = loaded.soundVolume;
        config.soundPitch = loaded.soundPitch;
        config.soundRepeatCount = loaded.soundRepeatCount;
        config.toastAlert = loaded.toastAlert;
        config.discordAlert = loaded.discordAlert;
        config.escalationStepMs = loaded.escalationStepMs;
        config.maxAutoSeverity = loaded.maxAutoSeverity;
        config.gracePeriodMs = loaded.gracePeriodMs;
        config.autoReconnect = loaded.autoReconnect;
        config.autoReconnectDelayMs = loaded.autoReconnectDelayMs;
        if (loaded.detectors != null && !loaded.detectors.isEmpty()) config.detectors.putAll(loaded.detectors);
    }
    private final Map<FailsafeType, TriggerState> active = new EnumMap<>(FailsafeType.class);
    private final List<AbstractDetector> detectors = new ArrayList<>();
    private final FailsafeSoundPlayer sounds = new FailsafeSoundPlayer();
    private final BanActionHandler banActions = new BanActionHandler();
    private final PlayerNotifier notifier = new PlayerNotifier();
    private final SafetyStatusMonitor statusMonitor = new SafetyStatusMonitor(this);
    private final PanicButton panic = new PanicButton(this);
    private final TabInHandler tabIn = new TabInHandler(this);
    private final ReactionEngine reactions = new ReactionEngine(this);

    private FailsafeStrictness highestSeverity = FailsafeStrictness.NONE;
    private FailsafeType dominantType;
    private String dominantReason = "";
    private long dominantSinceMs;
    private long lastAllClearMs = System.currentTimeMillis();
    private long lastTriggerMs;
    private int triggersSinceStartup = 0;
    private boolean inputFrozen = false;
    private boolean macrosPaused = false;
    private boolean disconnecting = false;
    private boolean initialised = false;

    private FailsafeManager() {}

    public FailsafeStrictness highestSeverity() { return highestSeverity; }
    public boolean isActive() { return highestSeverity.level() >= FailsafeStrictness.PAUSE.level(); }
    public boolean isInputFrozen() { return inputFrozen; }
    public boolean areMacrosPaused() { return macrosPaused; }
    public ReactionEngine reactions() { return reactions; }

    private void addDetector(AbstractDetector d) { detectors.add(d); }
    public FailsafeSoundPlayer sounds() { return sounds; }
    public BanActionHandler banActions() { return banActions; }
    public PlayerNotifier notifier() { return notifier; }
    public PanicButton panicButton() { return panic; }

    public void init() {
        if (initialised) return;
        initialised = true;

        // Register all detectors. Order is not significant; each reports independently.
        addDetector(new PlayerNearbyDetector());
        addDetector(new BanDetector());
        addDetector(new LimboDetector());
        addDetector(new TeleportDetector());
        addDetector(new VelocityDetector());
        addDetector(new ItemSwapDetector());
        addDetector(new RotationDetector());
        addDetector(new WorldChangeDetector());
        addDetector(new GUICloseDetector());
        addDetector(new WrongItemDetector());
        addDetector(new YawFlipDetector());
        addDetector(new InventoryFullDetector());
        addDetector(new LagbackDetector());
        addDetector(new DismountDetector());
        addDetector(new DeathDetector());
        addDetector(new LowHealthDetector());
        addDetector(new EtherwarpDetector());
        addDetector(new PlayerCloneDetector());
        addDetector(new DisconnectDetector());

        // Subscribe manager + detector @SubscribeEvent methods to the event bus.
        ZenithEventBus.getInstance().register(this);
        for (AbstractDetector d : detectors) ZenithEventBus.getInstance().register(d);
        reactions.init();
        banActions.init();
        tabIn.init();

        // Keybind wiring. Panic = halt + (held 400 ms) disconnect. Emergency stop = pause.
        com.zenith.client.keybind.KeybindManager.getInstance().register(
                com.zenith.client.keybind.ZenithKeybinds.PANIC_BUTTON, panic::onPress);
        com.zenith.client.keybind.KeybindManager.getInstance().register(
                com.zenith.client.keybind.ZenithKeybinds.EMERGENCY_STOP, () -> {
                    trigger(FailsafeType.CUSTOM, "emergency-stop hotkey", FailsafeStrictness.PAUSE);
                    ZenithChat.getInstance().error("Emergency stop pressed — macros paused.");
                });

        ZenithClient.LOGGER.info("[Failsafe] Initialised ({} detectors).", detectors.size());
    }

    /** Called from {@link com.zenith.client.core.ClientTickDispatcher} every client tick. */
    public void tick() {
        if (!config.globalEnabled || !initialised) return;

        long now = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            // Not in-game — reset all triggers.
            clearAll(now);
            return;
        }

        // 1. Poll every detector.
        for (AbstractDetector d : detectors) {
            try {
                pollDetector(d, now);
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Failsafe] Detector {} threw", d.getClass().getSimpleName(), t);
            }
        }

        // 2. Sweep stale triggers that haven't fired within the grace period.
        sweepStale(now);

        // 3. Recompute highest severity across all active triggers.
        recomputeHighest(now);

        // 4. Apply the effects of the current severity level.
        applySeverity(now);

        // 5. Drive the reaction engine (mistake-simulation sequences, chat responses, etc.).
        reactions.tick(now);

        // 6. Status monitor updates the HUD toast/indicator.
        statusMonitor.tick(now);

        // 7. Panic-button hold detection.
        panic.tick(now);

        // 8. Window-focus watchdog.
        tabIn.tick(now);
    }

    // ---- Trigger API (called by detectors & external events) -------------------

    /**
     * Called by detectors when they believe a failsafe condition has occurred.
     *
     * @param type    which failsafe
     * @param reason  human-readable reason (shown to the user, logged)
     * @param raw     raw severity requested by the detector (may be {@code null} → use configured default)
     */
    public void trigger(FailsafeType type, String reason, FailsafeStrictness raw) {
        if (!initialised || !config.globalEnabled) return;
        if (!config.isDetectorEnabled(type)) return;

        FailsafeStrictness targetSeverity = (raw != null) ? raw : config.severityFor(type);
        long now = System.currentTimeMillis();

        TriggerState s = active.get(type);
        if (s == null) {
            s = new TriggerState(type, reason, targetSeverity, now);
            active.put(type, s);
            triggersSinceStartup++;
            lastTriggerMs = now;
            postFire(type, reason, targetSeverity, now);
        } else {
            s.lastSeenMs = now;
            s.lastReason = reason;
            if (targetSeverity.level() > s.currentSeverity.level()) {
                escalate(type, targetSeverity, reason, now);
            }
        }
    }

    /** Indicate that a previously triggered condition has cleared. */
    public void clear(FailsafeType type) {
        TriggerState s = active.get(type);
        if (s == null) return;
        s.clearedAtMs = System.currentTimeMillis();
    }

    /** Full reset of all triggers (called by player pressing the "resume" key). */
    public void clearAll(long now) {
        active.clear();
        if (highestSeverity != FailsafeStrictness.NONE) {
            highestSeverity = FailsafeStrictness.NONE;
            dominantType = null;
            dominantReason = "";
            inputFrozen = false;
            macrosPaused = false;
            disconnecting = false;
            BitsSpendBlocker.setBlocked(false);
            // Re-enable eyes (reaction engine may have left them on for mistake-simulation, but
            // clearing resets to a known state). Path remains stopped — macro modules must decide
            // whether to re-request their route.
            ZenithEyes.getInstance().setEnabled(true);
            ZenithChat.getInstance().success("Failsafe cleared — macros released.");
            lastAllClearMs = now;
            reactions.onClear();
        }
    }

    public void resumeFromUser() {
        long now = System.currentTimeMillis();
        clearAll(now);
    }

    // ---- Event listeners -------------------------------------------------------

    /**
     * External systems can request a manual trigger via FailsafeTriggerEvent being
     * posted (e.g. MacroStuckEvent fires a failsafe). This listener picks up those
     * events and routes them into our trigger pipeline.
     */
    @com.zenith.client.core.event.annotation.SubscribeEvent
    public void onExternalTrigger(FailsafeTriggerEvent ev) {
        if (!initialised || !config.globalEnabled) return;
        FailsafeType type = FailsafeType.CUSTOM;
        FailsafeStrictness sev = switch (ev.getSeverity()) {
            case 0  -> FailsafeStrictness.NOTIFY;
            case 1  -> FailsafeStrictness.WARP_HOME;
            case 2  -> FailsafeStrictness.DISCONNECT;
            default -> FailsafeStrictness.PAUSE;
        };
        trigger(type, ev.getFailsafeName() + ": " + ev.getReason(), sev);
    }

    // ---- Internals -------------------------------------------------------------

    private void pollDetector(AbstractDetector d, long now) {
        d.tick(now);
    }

    private void sweepStale(long now) {
        List<FailsafeType> toRemove = new ArrayList<>();
        for (Map.Entry<FailsafeType, TriggerState> e : active.entrySet()) {
            TriggerState s = e.getValue();
            if (s.clearedAtMs != 0 && (now - s.clearedAtMs) > config.gracePeriodMs) {
                toRemove.add(e.getKey());
            } else if (s.clearedAtMs == 0 && (now - s.lastSeenMs) > config.gracePeriodMs * 2L) {
                // Detector hasn't fired for a while — mark for expiry.
                s.clearedAtMs = now;
            }
        }
        for (FailsafeType t : toRemove) active.remove(t);
    }

    private void recomputeHighest(long now) {
        FailsafeStrictness top = FailsafeStrictness.NONE;
        FailsafeType topType = null;
        String topReason = "";
        long topSinceMs = Long.MAX_VALUE;

        for (TriggerState s : active.values()) {
            if (s.currentSeverity.level() > top.level()) {
                top = s.currentSeverity;
                topType = s.type;
                topReason = s.lastReason;
                topSinceMs = s.startedAtMs;
            } else if (s.currentSeverity.level() == top.level() && s.startedAtMs < topSinceMs) {
                topType = s.type;
                topReason = s.lastReason;
                topSinceMs = s.startedAtMs;
            }
            // Escalation: if condition persists, climb one rung every escalationStepMs (if autoEscalates).
            if (s.type.autoEscalates() && (now - s.lastEscalationMs) >= config.escalationStepMs) {
                FailsafeStrictness next = nextSeverity(s.currentSeverity);
                if (next != null && next.level() <= config.maxAutoSeverity.level()) {
                    escalate(s.type, next, s.lastReason, now);
                }
                s.lastEscalationMs = now;
            }
        }

        highestSeverity = top;
        dominantType = topType;
        dominantReason = topReason;
        if (topSinceMs != Long.MAX_VALUE) dominantSinceMs = topSinceMs;
        if (top == FailsafeStrictness.NONE) lastAllClearMs = now;
    }

    private FailsafeStrictness nextSeverity(FailsafeStrictness cur) {
        FailsafeStrictness[] v = FailsafeStrictness.values();
        int i = cur.ordinal() + 1;
        if (i >= v.length) return null;
        return v[i];
    }

    private void escalate(FailsafeType type, FailsafeStrictness to, String reason, long now) {
        TriggerState s = active.get(type);
        if (s == null) return;
        if (to.level() <= s.currentSeverity.level()) return;
        s.currentSeverity = to;
        s.lastEscalationMs = now;
        postFire(type, reason, to, now);
    }

    private void postFire(FailsafeType type, String reason, FailsafeStrictness severity, long now) {
        FailsafeTriggerEvent ev = new FailsafeTriggerEvent(type.displayName(), reason, severity.level());
        ZenithEventBus.getInstance().post(ev);
        if (ev.isCancelled()) {
            active.remove(type);
            return;
        }

        ZenithChat.getInstance().warn("§c[Failsafe] §f{}: {} (severity={})", type.displayName(), reason, severity.name());

        if (config.soundAlert) sounds.playAlert();
        if (config.toastAlert) notifier.onTrigger(type, reason, severity);

        // Let the reaction engine run a mistake-simulation sequence.
        reactions.onTrigger(type, reason, severity, now);
    }

    private void applySeverity(long now) {
        FailsafeStrictness s = highestSeverity;
        if (s.level() < FailsafeStrictness.PAUSE.level()) {
            if (inputFrozen || macrosPaused) {
                inputFrozen = false;
                macrosPaused = false;
                disconnecting = false;
                BitsSpendBlocker.setBlocked(false);
                // Do NOT re-enable ZenithEyes automatically — leave it off until the player
                // presses resume / clears the trigger, so we don't silently resume rotation.
                ZenithChat.getInstance().info("Failsafe condition cleared — press resume to re-enable macros.");
            }
            return;
        }

        // PAUSE tier and above: freeze input and macros.
        if (!inputFrozen) {
            inputFrozen = true;
            macrosPaused = true;
            BitsSpendBlocker.setBlocked(true);
            ZenithPath.getInstance().stop();
            ZenithEyes.getInstance().setEnabled(false);
            KeySimulator keys = InputEngine.getInstance().keys();
            if (keys != null) keys.halt();
        }

        if (s.atLeast(FailsafeStrictness.WARP_HOME)) {
            if (!disconnecting) {
                banActions.sendHome();
            }
        }
        if (s.atLeast(FailsafeStrictness.WARP_SPAWN)) {
            if ((now - dominantSinceMs) > config.escalationStepMs * 3L) {
                banActions.sendHub();
            }
        }
        if (s.atLeast(FailsafeStrictness.DISCONNECT)) {
            disconnecting = true;
            banActions.disconnect("Failsafe: " + (dominantType != null ? dominantType.displayName() : "unknown"));
        }
    }

    // ---- Debug / HUD -----------------------------------------------------------

    public FailsafeDebugData debugData() {
        List<String> recent = new ArrayList<>();
        long cutoff = System.currentTimeMillis() - 60_000L;
        for (TriggerState s : active.values()) {
            recent.add(s.type.displayName() + "=" + s.currentSeverity.name() + "(" + s.lastReason + ")");
            if (recent.size() >= 5) break;
        }
        return new FailsafeDebugData(
                isActive(), dominantType, highestSeverity, dominantReason,
                dominantType != null ? (System.currentTimeMillis() - dominantSinceMs) : 0,
                recent, lastTriggerMs, triggersSinceStartup,
                inputFrozen, macrosPaused,
                reactions.stateName()
        );
    }

    // ---- Internal state holder -------------------------------------------------

    private static final class TriggerState {
        final FailsafeType type;
        String lastReason;
        FailsafeStrictness currentSeverity;
        long startedAtMs;
        long lastSeenMs;
        long lastEscalationMs;
        long clearedAtMs;

        TriggerState(FailsafeType type, String reason, FailsafeStrictness severity, long now) {
            this.type = type;
            this.lastReason = reason;
            this.currentSeverity = severity;
            this.startedAtMs = now;
            this.lastSeenMs = now;
            this.lastEscalationMs = now;
        }
    }
}
