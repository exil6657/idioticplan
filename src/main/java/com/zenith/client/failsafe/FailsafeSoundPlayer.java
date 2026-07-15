package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Plays a configurable alert sound when a failsafe triggers.
 *
 * <p>Uses Minecraft's {@link Minecraft#submit} to schedule plays from the main
 * thread because SoundManager must only be touched on the render thread. Repeats
 * {@link FailsafeConfig#soundRepeatCount} times with a short gap.</p>
 */
public final class FailsafeSoundPlayer {

    private long nextPlayAtMs = 0;
    private int remaining = 0;

    public void playAlert() {
        FailsafeConfig cfg = FailsafeManager.getInstance().config();
        if (!cfg.soundAlert) return;
        remaining = cfg.soundRepeatCount;
        nextPlayAtMs = 0; // first play on next tick()
        tick(System.currentTimeMillis());
    }

    /** Called each tick from FailsafeManager via SafetyStatusMonitor. */
    public void tick(long nowMs) {
        if (remaining <= 0) return;
        if (nowMs < nextPlayAtMs) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) { remaining = 0; return; }

        FailsafeConfig cfg = FailsafeManager.getInstance().config();
        SoundEvent evt = resolve(cfg.soundAlertName);
        try {
            mc.execute(() -> {
                if (mc.player == null) return;
                // SoundEvents.NOTE_BLOCK_PLING is a Holder<SoundEvent> in 26.1; .value() unwraps.
                mc.player.playSound(evt, cfg.soundVolume, cfg.soundPitch);
            });
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[Failsafe] Failed to play alert sound", t);
        }
        remaining--;
        nextPlayAtMs = nowMs + 180L;
    }

    @SuppressWarnings("deprecation")
    private SoundEvent resolve(String name) {
        if (name == null) return SoundEvents.NOTE_BLOCK_PLING.value();
        return switch (name) {
            case "block.anvil.land"             -> SoundEvents.ANVIL_LAND;
            case "entity.experience_orb.pickup" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "entity.enderman.scream"       -> SoundEvents.ENDERMAN_SCREAM;
            case "block.note_block.pling", "pling" -> SoundEvents.NOTE_BLOCK_PLING.value();
            default -> SoundEvents.NOTE_BLOCK_PLING.value();
        };
    }
}
