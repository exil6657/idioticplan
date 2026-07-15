package com.zenith.client.failsafe;

import com.google.gson.annotations.Expose;

import java.util.EnumMap;
import java.util.Map;

/**
 * Persisted configuration for the failsafe system.
 *
 * <p>Every detector is individually toggleable and has an override severity;
 * when {@code overrideSeverity} is {@code null} the detector uses the
 * {@link FailsafeType#defaultSeverity()}.</p>
 *
 * <p>Master doc says failsafes are always-on while any macro is active. The
 * {@code globalEnabled} switch lets the player disable the entire subsystem
 * for non-macro play (not recommended).</p>
 */
public class FailsafeConfig {

    @Expose public boolean globalEnabled = true;

    /** If true, plays a configurable alert sound on any trigger above NOTIFY. */
    @Expose public boolean soundAlert = true;
    @Expose public String soundAlertName = "block.note_block.pling";
    @Expose public float soundVolume = 1.0f;
    @Expose public float soundPitch = 1.6f;
    @Expose public int soundRepeatCount = 3;

    /** If true, shows a toast overlay with the trigger reason. */
    @Expose public boolean toastAlert = true;

    /** If true, sends a Discord webhook/DM when a failsafe triggers (Phase 18). */
    @Expose public boolean discordAlert = false;

    /** Time (ms) a condition must be active before escalation to next severity. */
    @Expose public long escalationStepMs = 3500;

    /** Max severity the system can escalate to without player input. */
    @Expose public FailsafeStrictness maxAutoSeverity = FailsafeStrictness.DISCONNECT;

    /** How long (ms) after an all-clear we consider the trigger resolved. */
    @Expose public long gracePeriodMs = 1500;

    /** If true, auto-reconnect after a disconnect-triggered failsafe (cooldown). */
    @Expose public boolean autoReconnect = false;
    @Expose public int autoReconnectDelayMs = 5000;

    /** Per-detector toggles and severity overrides. */
    @Expose public Map<FailsafeType, DetectorSetting> detectors = new EnumMap<>(FailsafeType.class);

    public FailsafeConfig() {
        for (FailsafeType t : FailsafeType.values()) {
            detectors.put(t, new DetectorSetting(true, null));
        }
    }

    public boolean isDetectorEnabled(FailsafeType type) {
        DetectorSetting s = detectors.get(type);
        return s != null ? s.enabled : true;
    }

    public FailsafeStrictness severityFor(FailsafeType type) {
        DetectorSetting s = detectors.get(type);
        if (s != null && s.overrideSeverity != null) return s.overrideSeverity;
        return type.defaultSeverity();
    }

    public static class DetectorSetting {
        @Expose public boolean enabled;
        @Expose public FailsafeStrictness overrideSeverity;
        public DetectorSetting() { this(true, null); }
        public DetectorSetting(boolean enabled, FailsafeStrictness overrideSeverity) {
            this.enabled = enabled;
            this.overrideSeverity = overrideSeverity;
        }
    }
}
