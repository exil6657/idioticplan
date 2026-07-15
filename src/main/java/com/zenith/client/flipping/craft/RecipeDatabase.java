package com.zenith.client.flipping.craft;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.api.cache.BazaarCache;
import com.zenith.client.api.cache.RecipeCache;
import com.zenith.client.api.cache.RecipeCache.Recipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Computes the cheapest ingredient cost for every known recipe. Used by
 * {@link CraftFlipEngine} to find craft → resell profit.
 *
 * <p>Cost is the minimum of (a) BIN buy price from AH or (b) Bazaar instant-buy
 * price. Ingredients without either price are skipped.</p>
 */
public final class RecipeDatabase {

    private static final RecipeDatabase INSTANCE = new RecipeDatabase();
    public static RecipeDatabase getInstance() { return INSTANCE; }

    private final Map<String, Long> craftCost = new HashMap<>();
    private long lastRefreshMs;

    private RecipeDatabase() {}

    public void refresh() {
        craftCost.clear();
        int computed = 0;
        // For now compute over a curated seed list of outputs we know we have recipes for.
        // A full BINCache.keySet() scan is added once crafting macros land (Phase 12).
        for (var e : gatherAllOutputs().entrySet()) {
            long cost = computeCost(e.getValue());
            if (cost > 0) { craftCost.put(e.getKey(), cost); computed++; }
        }
        lastRefreshMs = System.currentTimeMillis();
        ZenithClient.LOGGER.info("[RecipeDB] Computed craft cost for {} outputs.", computed);
    }

    public long costFor(String outputId) { return craftCost.getOrDefault(outputId.toUpperCase(), -1L); }
    public long ageMs() { return System.currentTimeMillis() - lastRefreshMs; }

    private Map<String, Recipe> gatherAllOutputs() {
        Map<String, Recipe> out = new HashMap<>();
        for (String output : java.util.List.of("ENCHANTED_DIAMOND", "ENCHANTED_IRON", "ENCHANTED_GOLD",
                "ENCHANTED_LAPIS_LAZULI", "ENCHANTED_EMERALD", "ENCHANTED_COAL", "ENCHANTED_REDSTONE",
                "SUPER_COMPACTOR_3000", "PERSONAL_COMPACTOR_4000", "HOT_POTATO_BOOK", "FUMING_POTATO_BOOK")) {
            var rs = RecipeCache.getInstance().recipesFor(output);
            if (!rs.isEmpty()) out.put(output, rs.get(0));
        }
        return out;
    }

    private long computeCost(Recipe r) {
        long total = 0L;
        for (var ing : r.ingredients()) {
            long perUnit = ingredientPrice(ing.skyblockId());
            if (perUnit <= 0) return -1L;
            total += perUnit * ing.count();
        }
        return Math.max(0L, total / Math.max(1, r.outputCount()));
    }

    private long ingredientPrice(String id) {
        long bin = BINCache.getInstance().get(id);
        var bz = BazaarCache.getInstance().get(id);
        long bzBuy = bz != null ? (long) Math.ceil(bz.buyPrice()) : Long.MAX_VALUE;
        long min = Math.min(bin > 0 ? bin : Long.MAX_VALUE, bzBuy);
        return min == Long.MAX_VALUE ? -1L : min;
    }
}
