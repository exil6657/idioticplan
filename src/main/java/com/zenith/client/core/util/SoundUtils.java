package com.zenith.client.core.util;

/**
 * Sound playback helpers (clicks, alert beeps).
 *
 * <p>Phase 2 stub; Phase 5 wires this to {@code SoundManager}.</p>
 */
public final class SoundUtils {

    private SoundUtils() {}

    public static void playClick() {
        // Phase 5: MC.getInstance().getSoundManager().play(SoundInstance);
    }

    public static void playAlert() {
        // Phase 5: short high-pitched beep for failsafe triggers.
    }
}
