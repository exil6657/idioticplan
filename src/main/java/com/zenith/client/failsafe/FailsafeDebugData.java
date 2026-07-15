package com.zenith.client.failsafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable telemetry snapshot rendered by the Brain View / Failsafe HUD panel. */
public final class FailsafeDebugData {

    public final boolean active;
    public final FailsafeType activeType;
    public final FailsafeStrictness currentSeverity;
    public final String activeReason;
    public final long activeForMs;
    public final List<String> recentTriggers;
    public final long lastTriggerMs;
    public final int triggersSinceStartup;
    public final boolean inputFrozen;
    public final boolean macrosPaused;
    public final String reactionState;

    public FailsafeDebugData(boolean active,
                             FailsafeType activeType,
                             FailsafeStrictness currentSeverity,
                             String activeReason,
                             long activeForMs,
                             List<String> recentTriggers,
                             long lastTriggerMs,
                             int triggersSinceStartup,
                             boolean inputFrozen,
                             boolean macrosPaused,
                             String reactionState) {
        this.active = active;
        this.activeType = activeType;
        this.currentSeverity = currentSeverity;
        this.activeReason = activeReason;
        this.activeForMs = activeForMs;
        this.recentTriggers = Collections.unmodifiableList(new ArrayList<>(recentTriggers));
        this.lastTriggerMs = lastTriggerMs;
        this.triggersSinceStartup = triggersSinceStartup;
        this.inputFrozen = inputFrozen;
        this.macrosPaused = macrosPaused;
        this.reactionState = reactionState;
    }
}
