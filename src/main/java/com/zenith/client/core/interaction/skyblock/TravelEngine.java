package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.interaction.CommandSender;
import com.zenith.client.core.interaction.GUIWaiter;
import net.minecraft.client.Minecraft;

/**
 * State machine that brings the player from wherever they are (limbo, lobby,
 * another island, hub) back to a known destination (e.g. the Garden for
 * farming, Deep Caverns for mining). Used by {@code RepathReactionAction} when
 * the player has been teleported off-server.
 *
 * <p>Sequence (simplified): detect current location → send the correct
 * {@code /warp} or /command (or walk-to-NPC for Garden) → wait for the
 * destination world/scoreboard → then hand off to ZenithPath for fine-grained
 * walking.</p>
 *
 * <p>Phase 11: only the command-dispatch part is implemented; world-change
 * waits and NPC routing are Phase 13/14.</p>
 */
public final class TravelEngine {

    private static final TravelEngine INSTANCE = new TravelEngine();
    public static TravelEngine getInstance() { return INSTANCE; }

    private String pendingDest = "";
    private long sentAt;

    private TravelEngine() {}

    public void travelTo(String destinationId) {
        pendingDest = destinationId == null ? "" : destinationId;
        sentAt = System.currentTimeMillis();
        switch (destinationId == null ? "" : destinationId.toLowerCase()) {
            case "island", "is", "home" -> SkyblockNavigator.getInstance().toIsland();
            case "hub"                   -> SkyblockNavigator.getInstance().toHub();
            case "bazaar", "bz"          -> SkyblockNavigator.getInstance().toBazaar();
            case "ah", "auction"         -> SkyblockNavigator.getInstance().toAuctionHouse();
            case "barn", "farming"       -> SkyblockNavigator.getInstance().toBarn();
            case "park", "foraging"      -> SkyblockNavigator.getInstance().toPark();
            case "deepcaverns", "mining" -> SkyblockNavigator.getInstance().toDeepCaverns();
            case "gold", "goldmine"      -> SkyblockNavigator.getInstance().toGoldMine();
            case "nether", "blazing"     -> SkyblockNavigator.getInstance().toBlazing();
            case "spider"                -> SkyblockNavigator.getInstance().toSpider();
            case "end"                   -> SkyblockNavigator.getInstance().toEnd();
            case "limbo"                 -> SkyblockNavigator.getInstance().escapeLimbo();
            default -> {
                // Assume it's a raw /warp name.
                CommandSender.send("/warp " + destinationId);
            }
        }
        GUIWaiter.waitForTitle("SkyBlock", 8_000L);
        ZenithClient.LOGGER.info("[Travel] → {}", destinationId);
    }

    public boolean isTravelling() {
        return !pendingDest.isEmpty() && System.currentTimeMillis() - sentAt < 10_000L;
    }

    public String pendingDestination() { return pendingDest; }

    public void tick() {
        if (!isTravelling()) { pendingDest = ""; return; }
        Minecraft mc = Minecraft.getInstance();
        // Once world/screen suggests arrival, hand off back to macro (which has
        // its own DestinationProvider and will request a ZenithPath).
        if (mc.player != null && mc.player.onGround()
                && System.currentTimeMillis() - sentAt > 2500) {
            pendingDest = "";
        }
    }
}
