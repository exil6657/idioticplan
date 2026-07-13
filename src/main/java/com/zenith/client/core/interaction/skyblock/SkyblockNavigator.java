package com.zenith.client.core.interaction.skyblock;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.interaction.CommandSender;
import net.minecraft.client.Minecraft;

/**
 * Cross-server SkyBlock navigation: warps, NPC clicks, and jump-pad routing to
 * common destinations. Used by RepathReactionAction and by macros that need to
 * return to a specific area after being teleported away (death, limbo, lobby).
 *
 * <p>Warp commands are always sent server-side (they must reach Hypixel).</p>
 */
public final class SkyblockNavigator {

    private static final SkyblockNavigator INSTANCE = new SkyblockNavigator();
    public static SkyblockNavigator getInstance() { return INSTANCE; }

    private SkyblockNavigator() {}

    /** Travel to the player's private island. */
    public void toIsland()  { CommandSender.send("/is"); }
    /** Travel to the Hub. */
    public void toHub()     { CommandSender.send("/hub"); }
    /** /warp <name> */
    public void warp(String name) { CommandSender.send("/warp " + name); }
    /** Fast travel to the Bazaar (Hub warp + walk to bazaar NPC is handled by ZenithPath). */
    public void toBazaar()  { CommandSender.send("/bz"); }
    /** Fast travel to the Auction House (master AH in Hub). */
    public void toAuctionHouse() { CommandSender.send("/ah"); }
    /** Travel to the Farming Islands. */
    public void toBarn()    { CommandSender.send("/warp barn"); }
    /** Travel to the Park / forest. */
    public void toPark()    { CommandSender.send("/warp park"); }
    /** Travel to the Deep Caverns (mining). */
    public void toDeepCaverns() { CommandSender.send("/warp deep"); }
    /** Travel to the Gold Mine. */
    public void toGoldMine(){ CommandSender.send("/warp gold"); }
    /** Travel to the Crimson Isle (Blazing Fortress replacement). */
    public void toCrimson() { CommandSender.send("/warp isle"); }
    /** Travel to the Spider's Den. */
    public void toSpider()  { CommandSender.send("/warp spider"); }
    /** Travel to The End. */
    public void toEnd()     { CommandSender.send("/warp end"); }
    /** Travel to Dwarven Mines. */
    public void toDwarven() { CommandSender.send("/warp mines"); }
    /** Travel to the Park (foraging). */
    public void toPark()    { CommandSender.send("/warp park"); }
    /** Travel to the Barn (farming). */
    public void toBarn()    { CommandSender.send("/warp barn"); }

    /** @return true if the player appears to be in SkyBlock (scoreboard title heuristic). */
    public boolean isInSkyblock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return false;
        // SKIPPED for now: Phase 13 fills a proper ScoreboardContent check.
        return true;
    }

    /** Escape limbo: send /lobby then /skyblock. */
    public void escapeLimbo() {
        CommandSender.send("/lobby");
        com.zenith.client.core.timer.DelayManager.getInstance().resetHumanised("limbo", 1200, 1800);
        ZenithClient.LOGGER.info("[SkyNav] Escaping limbo…");
        // Scheduled by the reaction engine / macro — caller must call back toSkyblock()
        // after a short delay (we can't Thread.sleep per rule §5).
    }

    /** Join SkyBlock from lobby. */
    public void toSkyblock() { CommandSender.send("/play skyblock"); }
}
