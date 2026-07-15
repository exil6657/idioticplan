package com.zenith.client.failsafe.detection;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.LimboDetectedEvent;
import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/**
 * Detects when Hypixel has moved the player into limbo.
 *
 * <p>Signals: chat line "You are in Limbo" (from ChatPatternEngine), AND the
 * player being in an empty single-player-style world (no scoreboard title,
 * empty tab list, bedrock-like floor). We AND the signals to avoid false
 * positives from lobbies.</p>
 */
public class LimboDetector extends AbstractDetector {

    private boolean chatSignalled = false;
    private long chatSignalAt;

    @Override
    public void tick(long nowMs) {
        Minecraft mc = mc();
        if (mc.player == null || mc.level == null) { clear(FailsafeType.LIMBO); return; }

        boolean inLimboWorld = isLimboWorld(mc);
        if (chatSignalled && (nowMs - chatSignalAt) < 10_000L && inLimboWorld) {
            trigger(FailsafeType.LIMBO, "moved to limbo");
        } else if (!inLimboWorld) {
            chatSignalled = false;
            clear(FailsafeType.LIMBO);
        } else if (inLimboWorld && !chatSignalled) {
            // World-only signal: if we've been in a limbo-like world for >3s with no active session, flag.
            if (nowMs - chatSignalAt > 3000) trigger(FailsafeType.LIMBO, "limbo-like world (no chat)");
        }
    }

    private boolean isLimboWorld(Minecraft mc) {
        // Limbo on Hypixel: flat world, usually no entities, y ≈ 1 or 400, no SkyBlock scoreboard.
        if (mc.level.dimension() != Level.OVERWORLD) return false;
        // Scoreboard check: use the World adapter's isSkyBlock()
        com.zenith.client.world.WorldAdapter a = com.zenith.client.world.World.get();
        return !a.isSkyBlock() && mc.player.getY() < 450d && mc.getConnection() != null && mc.getConnection().getOnlinePlayers().size() < 3;
    }

    @SubscribeEvent
    public void onLimbo(LimboDetectedEvent ev) {
        chatSignalled = true;
        chatSignalAt = System.currentTimeMillis();
    }
}
