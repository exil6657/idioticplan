package com.zenith.client.flipping.filter;

import java.util.*;

/**
 * Determines whether an item should be considered for flipping. Blacklists
 * untradable / soul-bound / cosmetic / overly common items, applies a budget
 * ceiling, and supports custom allow/deny lists.
 */
public class ItemFilter {

    public long maxPrice = Long.MAX_VALUE;
    public long minPrice = 1_000L;
    public long minDailyVolume = 1;
    public boolean excludeSoulbound = true;
    public boolean excludeCosmetic = true;
    public boolean excludeDungeon = false;
    public boolean excludeReforgeStones = false;
    public Set<String> allowList = new HashSet<>();
    public Set<String> denyList = new HashSet<>();

    private static final Set<String> DENY_PREFIXES = Set.of(
            "PARTY_HAT_CRACKER", "PARTY_HAT_", "NEW_YEAR_CAKE", "JERRY", "CREATIVE_MIND",
            "GAME_BREAKER", "BARRIER", "COMMAND", "SPELL", "COSMETIC", "HAT_ACCESSORY",
            "RANDOMIZER", "ABICASE", "BLOCK_OPERATOR", "HISTORICAL", "DEAD_BUSH"
    );

    public boolean allows(String itemId, long price) {
        if (itemId == null) return false;
        String id = itemId.toUpperCase(Locale.ROOT);
        if (!allowList.isEmpty() && !allowList.contains(id)) return false;
        if (denyList.contains(id)) return false;
        if (price < minPrice || price > maxPrice) return false;
        if (excludeSoulbound && id.contains("SOULBOUND") || id.contains("SPIRIT_BONE")) return false;
        if (excludeCosmetic) {
            for (String p : DENY_PREFIXES) if (id.startsWith(p) || id.contains(p)) return false;
            if (id.contains("SKIN_") || id.contains("AURA_") || id.contains("TRAIL_") || id.contains("BALLOON_")) return false;
        }
        return true;
    }

    public void presetLowball() {
        minPrice = 100_000L;
        maxPrice = 5_000_000L;
        minDailyVolume = 20;
        excludeCosmetic = true;
        excludeSoulbound = true;
    }

    public void presetHighVolume() {
        minPrice = 10_000L;
        maxPrice = 50_000_000L;
        minDailyVolume = 50;
    }

    public void presetBigTicket() {
        minPrice = 10_000_000L;
        maxPrice = 400_000_000L;
        minDailyVolume = 3;
    }
}
