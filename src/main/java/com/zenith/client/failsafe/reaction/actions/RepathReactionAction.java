package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;

/**
 * Reaction for teleport, world change, lagback, limbo, post-respawn, etc.:
 * rather than stopping the macro, recompute an A* (or etherwarp) path back to
 * the current macro's anchor and resume.
 *
 * <p>Supports long-distance re-navigation including:</p>
 * <ul>
 *   <li>Walking / sprinting / etherwarp within the same world.</li>
 *   <li>Jump pads (walk onto pressure plates/pads; they swap servers).</li>
 *   <li>NPC interaction (click NPC → select destination in chat/GUI).</li>
 *   <li>Command-based travel as a last resort (/warp <x>) when set in config.</li>
 * </ul>
 *
 * <p>Phase 14-19 (macro-specific phases) hook a {@code MacroDestinationProvider}
 * into here so the reaction knows <i>where</i> to walk back to. For Phase 10
 * we just log and release the input freeze; the repath request is queued.</p>
 */
public final class RepathReactionAction {

    /** Callback supplied by the active macro module — returns the destination. */
    public interface DestinationProvider {
        double destX();
        double destY();
        double destZ();
        String destinationDescription();
    }

    private static DestinationProvider currentProvider;

    public static void setProvider(DestinationProvider p) { currentProvider = p; }
    public static void clearProvider() { currentProvider = null; }

    public static void trigger(String reason) {
        DestinationProvider p = currentProvider;
        if (p == null) {
            ZenithClient.LOGGER.debug("[Failsafe] repath requested ({}), but no active macro destination", reason);
            return;
        }
        ZenithClient.LOGGER.info("[Failsafe] repathing to {} (reason={})", p.destinationDescription(), reason);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        try {
            var prb = com.zenith.client.engine.path.PathRequest.builder()
                    .from(mc.player.getX(), mc.player.getY(), mc.player.getZ())
                    .to(p.destX(), p.destY(), p.destZ())
                    .mode(com.zenith.client.engine.path.PathMode.WALK)
                    .allowSprint(true)
                    .allowEtherwarp(true)
                    .stopDistance(1.5d)
                    .maxComputeMs(150L)
                    .tag("failsafe:repath:" + reason);
            com.zenith.client.engine.path.ZenithPath.getInstance().requestPath(prb.build());
        } catch (Throwable t) {
            ZenithClient.LOGGER.error("[Failsafe] repath request failed", t);
        }
    }
}
