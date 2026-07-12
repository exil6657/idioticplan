package com.zenith.client.flipping.npc;

import java.util.Map;

/** Known NPC sell prices (hard-coded seed; expanded by later phases). */
public final class NPCDatabase {
    private static final NPCDatabase INSTANCE = new NPCDatabase();
    public static NPCDatabase getInstance() { return INSTANCE; }
    private static final Map<String, Long> SELL_PRICES = Map.ofEntries(
            // Farming NPC buys — basic seed prices.
            // Future phase fills in from NEU constants (npc_sell_price field).
            Map.entry("WHEAT", 6L),
            Map.entry("CARROT_ITEM", 3L),
            Map.entry("POTATO_ITEM", 3L),
            Map.entry("MELON", 2L),
            Map.entry("PUMPKIN", 10L),
            Map.entry("SUGAR_CANE", 4L),
            Map.entry("BROWN_MUSHROOM", 10L),
            Map.entry("RED_MUSHROOM", 10L),
            Map.entry("CACTUS", 3L),
            Map.entry("BLAZE_ROD", 9L)
    );

    private NPCDatabase() {}

    public long buyPriceFromNPC(String id) { return -1L; }
    public long sellPriceToNPC(String id) {
        Long v = SELL_PRICES.get(id == null ? "" : id.toUpperCase());
        return v == null ? -1L : v;
    }
    public boolean hasNPC(String id) { return SELL_PRICES.containsKey(id == null ? "" : id.toUpperCase()); }
}
