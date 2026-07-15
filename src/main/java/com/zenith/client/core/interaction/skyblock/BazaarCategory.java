package com.zenith.client.core.interaction.skyblock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Best-effort mapping from SkyBlock item id → Hypixel Bazaar category, so the
 * BazaarExecutor can walk Catalog → Category → Product when buying/selling.
 *
 * <p>Hypixel uses five top-level categories:
 * <ul>
 *   <li><b>Farming</b> — crops, seeds, food, animal drops.</li>
 *   <li><b>Mining</b> — ores, gems, stones, powder-related.</li>
 *   <li><b>Combat</b> — mob drops (rotten flesh, bones, ender pearls, blaze rods).</li>
 *   <li><b>Woods &amp; Fishes</b> — logs, fish, prismarine, ice.</li>
 *   <li><b>Oddities</b> — everything else (redstone intermediates, flowers, etc.).</li>
 * </ul>
 *
 * <p>[RESEARCH NEEDED] DevDataMacro tour will capture the exact category of
 * every product; this hard-coded seed map bootstraps limit-order placement
 * for hot products used by BazaarFlipEngine.</p>
 */
public final class BazaarCategory {

    public static final String FARMING       = "Farming";
    public static final String MINING        = "Mining";
    public static final String COMBAT        = "Combat";
    public static final String WOODS_FISHES  = "Woods & Fishes";
    public static final String ODDITIES      = "Oddities";

    private static final Map<String, String> ID_TO_CATEGORY = new HashMap<>();

    static {
        seed(FARMING,
                "WHEAT","CARROT_ITEM","POTATO_ITEM","SUGAR_CANE","MELON","PUMPKIN",
                "SEEDS","CROPS","ENCHANTED_WHEAT","ENCHANTED_CARROT","ENCHANTED_POTATO",
                "ENCHANTED_SUGAR_CANE","ENCHANTED_MELON","ENCHANTED_PUMPKIN","ENCHANTED_GOLDEN_CARROT",
                "ENCHANTED_BAKED_POTATO","ENCHANTED_HAY_BLOCK","HAY_BLOCK",
                "MUTTON","PORK","RAW_CHICKEN","RAW_BEEF","RABBIT",
                "ENCHANTED_PORK","ENCHANTED_RAW_CHICKEN","ENCHANTED_RAW_BEEF","ENCHANTED_MUTTON",
                "LEATHER","FEATHER","EGG","RABBIT_HIDE",
                "BROWN_MUSHROOM","RED_MUSHROOM","NETHER_STALK","CACTUS","CACTUS_GREEN",
                "INK_SACK:3","INK_SACK:4","INK_SACK:2","INK_SACK:1",
                "COCOA","ENCHANTED_COCOA","ENCHANTED_BROWN_MUSHROOM","ENCHANTED_RED_MUSHROOM",
                "ENCHANTED_CACTUS_GREEN","ENCHANTED_MELON_BLOCK","ENCHANTED_COOKIE",
                "ENCHANTED_GLISTERING_MELON","HONEYCOMB","HONEY_BOTTLE");
        seed(MINING,
                "COAL","COBBLESTONE","GRAVEL","SAND","OBSIDIAN","ENDER_STONE",
                "DIAMOND","IRON_INGOT","GOLD_INGOT","EMERALD","REDSTONE","LAPIS_LAZULI",
                "QUARTZ","FLINT","CLAY_BALL","GLOWSTONE_DUST","NETHERRACK","SOUL_SAND",
                "MITHRIL_ORE","TITANIUM_ORE","HARD_STONE",
                "ENCHANTED_COAL","ENCHANTED_DIAMOND","ENCHANTED_IRON","ENCHANTED_GOLD",
                "ENCHANTED_EMERALD","ENCHANTED_REDSTONE","ENCHANTED_LAPIS_LAZULI",
                "ENCHANTED_COBBLESTONE","ENCHANTED_GRAVEL","ENCHANTED_SAND","ENCHANTED_OBSIDIAN",
                "ENCHANTED_ENDSTONE","ENCHANTED_QUARTZ","ENCHANTED_CLAY_BALL","ENCHANTED_GLOWSTONE_DUST",
                "REFINED_MITHRIL","REFINED_TITANIUM",
                "ICE","PACKED_ICE","ENCHANTED_PACKED_ICE","ENCHANTED_ICE","BLUE_ICE","ENCHANTED_BLUE_ICE",
                "DIAMOND_ORE","EMERALD_ORE","LAPIS_ORE","GOLD_ORE","IRON_ORE","COAL_ORE","REDSTONE_ORE");
        seed(COMBAT,
                "ROTTEN_FLESH","BONE","STRING","SPIDER_EYE","GUNPOWDER","ENDER_PEARL","BLAZE_ROD",
                "GHAST_TEAR","MAGMA_CREAM","SLIME_BALL","BLAZE_POWDER","ENDER_EYE","FERMENTED_SPIDER_EYE",
                "ENCHANTED_ENDER_PEARL","ENCHANTED_BLAZE_POWDER","ENCHANTED_ROTTEN_FLESH","ENCHANTED_BONE",
                "ENCHANTED_STRING","ENCHANTED_SPIDER_EYE","ENCHANTED_GUNPOWDER","ENCHANTED_SLIME_BALL",
                "ENCHANTED_MAGMA_CREAM","ENCHANTED_GHAST_TEAR","ENCHANTED_ENDER_EYE",
                "WOLF_TOOTH","SPECTRE_DUST","REVENANT_FLESH","TARANTULA_WEB");
        seed(WOODS_FISHES,
                "OAK_LOG","SPRUCE_LOG","BIRCH_LOG","JUNGLE_LOG","DARK_OAK_LOG","ACACIA_LOG",
                "LOG","LOG:1","LOG:2","LOG_2","LOG_2:1",
                "ENCHANTED_OAK_LOG","ENCHANTED_SPRUCE_LOG","ENCHANTED_BIRCH_LOG",
                "ENCHANTED_JUNGLE_LOG","ENCHANTED_DARK_OAK_LOG","ENCHANTED_ACACIA_LOG",
                "RAW_FISH","RAW_FISH:1","RAW_FISH:2","RAW_FISH:3",
                "ENCHANTED_RAW_FISH","ENCHANTED_RAW_FISH:1","ENCHANTED_RAW_FISH:2","ENCHANTED_RAW_FISH:3",
                "PRISMARINE_SHARD","PRISMARINE_CRYSTALS","ENCHANTED_PRISMARINE_SHARD","ENCHANTED_PRISMARINE_CRYSTALS",
                "WATER_LILY","PUFFERFISH","ENCHANTED_PUFFERFISH","SPONGE","ENCHANTED_SPONGE");
        seed(ODDITIES,
                "SULPHUR","SUPER_COMPACTOR_3000","PERSONAL_COMPACTOR_4000","PERSONAL_COMPACTOR_5000",
                "HOT_POTATO_BOOK","FUMING_POTATO_BOOK","TALISMAN_OF_COINS",
                "COMPOSTER","PLASMA_BUCKET","MAGMA_BUCKET","CORRUPTED_FRAGMENT");
    }

    private static void seed(String cat, String... ids) {
        for (String id : ids) {
            if (id == null) continue;
            ID_TO_CATEGORY.put(id.toUpperCase(Locale.ROOT), cat);
        }
    }

    public static String forProduct(String itemId) {
        if (itemId == null) return ODDITIES;
        return ID_TO_CATEGORY.getOrDefault(itemId.toUpperCase(Locale.ROOT), ODDITIES);
    }

    public static String fuzzyName(String in) {
        if (in == null) return ODDITIES;
        String s = in.toLowerCase(Locale.ROOT).trim();
        if (s.contains("farm") || s.contains("crop")) return FARMING;
        if (s.contains("mine") || s.contains("ore") || s.contains("stone") || s.contains("gem")) return MINING;
        if (s.contains("combat") || s.contains("mob") || s.contains("loot")) return COMBAT;
        if (s.contains("wood") || s.contains("fish") || s.contains("log") || s.contains("prismar")) return WOODS_FISHES;
        if (s.contains("odd")) return ODDITIES;
        return in;
    }

    private BazaarCategory() {}
}
