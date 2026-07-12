package com.zenith.client.flipping.bazaar;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BazaarCache;
import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.profit.ProfitCalculator;
import com.zenith.client.flipping.tax.TaxCalculator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bazaar flip engine.
 *
 * <p>Strategy: each tick, compare the top-of-book buy and sell prices for every
 * bazaar product. If the spread after fees exceeds the configured minimum,
 * create a FlipCandidate of type BAZAAR_SPREAD and enqueue it for the
 * FlipEngine. We use a very small per-tick scan budget (rate-limited by the
 * fact that BazaarCache refreshes only every 45 s).</p>
 */
public final class BazaarFlipEngine {

    private static final BazaarFlipEngine INSTANCE = new BazaarFlipEngine();
    public static BazaarFlipEngine getInstance() { return INSTANCE; }

    private final PriorityBlockingQueue<FlipCandidate> out = new PriorityBlockingQueue<>(64,
            (a, b) -> Long.compare(b.expectedProfit, a.expectedProfit));
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    private BazaarFlipEngine() {}

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        // Scan every 50 s (shortly after each refresh).
        com.zenith.client.core.util.ThreadUtils.scheduler().scheduleAtFixedRate(
                this::scan, 15, 50, java.util.concurrent.TimeUnit.SECONDS);
        ZenithClient.LOGGER.info("[BazaarFlipEngine] Started.");
    }

    public FlipCandidate pollCandidate() { return out.poll(); }

    private void scan() {
        try {
            var cache = BazaarCache.getInstance();
            int found = 0;
            var keys = new java.util.ArrayList<String>();
            // BazaarCache doesn't expose its keys directly, so we probe known hot products.
            for (String pid : HOT_PRODUCTS) {
                var e = cache.get(pid);
                if (e == null) continue;
                if (e.buyPrice() <= 0 || e.sellPrice() <= 0) continue;
                long buyCoins = TaxCalculator.bazaarInstantBuyCost((long) e.sellPrice(), 1); // we buy at sell-offer
                long sellCoins = TaxCalculator.bazaarInstantSellReceived((long) e.buyPrice(), 1);
                long profit = sellCoins - buyCoins;
                double roi = buyCoins > 0 ? (double) profit / buyCoins : 0d;
                var cfg = com.zenith.client.flipping.FlipEngine.getInstance().config();
                if (profit < cfg.minProfitCoins || roi < cfg.minProfitPercent) continue;
                // Volume threshold: skip illiquid pairs.
                if (e.buyVolume() < 50_000 || e.sellVolume() < 50_000) continue;
                ProfitCalculator.getInstance().setVolume(pid, (long) Math.min(e.buyVolume(), e.sellVolume()) / 24L);
                FlipCandidate c = new FlipCandidate(pid, pid, FlipType.BAZAAR_SPREAD,
                        buyCoins, e.buyPrice(), profit, roi,
                        (long) Math.min(e.buyVolume(), e.sellVolume()) / 24L, 0.9d, 30_000L, "", "", 1, "");
                out.offer(c);
                found++;
            }
            if (found > 0) ZenithClient.LOGGER.debug("[BazaarFlip] found {} candidates", found);
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[BazaarFlip] scan failed", t);
        }
    }

    /** A hand-picked list of commonly-traded bazaar products (safe starting point). */
    private static final java.util.List<String> HOT_PRODUCTS = java.util.List.of(
            "WHEAT","CARROT_ITEM","POTATO_ITEM","SUGAR_CANE","MELON","PUMPKIN",
            "ENCHANTED_WHEAT","ENCHANTED_CARROT","ENCHANTED_POTATO","ENCHANTED_SUGAR_CANE",
            "ENCHANTED_MELON","ENCHANTED_PUMPKIN","ENCHANTED_GOLDEN_CARROT",
            "DIAMOND","IRON_INGOT","GOLD_INGOT","COAL","EMERALD","REDSTONE","LAPIS_LAZULI",
            "ENCHANTED_DIAMOND","ENCHANTED_IRON","ENCHANTED_GOLD","ENCHANTED_COAL",
            "ENCHANTED_EMERALD","ENCHANTED_REDSTONE","ENCHANTED_LAPIS_LAZULI",
            "OAK_LOG","ENCHANTED_OAK_LOG","SPRUCE_LOG","ENCHANTED_SPRUCE_LOG",
            "ROTTEN_FLESH","BONE","STRING","SPIDER_EYE","GUNPOWDER","ENDER_PEARL",
            "ENCHANTED_ENDER_PEARL","BLAZE_ROD","GHAST_TEAR","MAGMA_CREAM","SLIME_BALL",
            "RAW_FISH","RAW_FISH:1","RAW_FISH:2","RAW_FISH:3","PRISMARINE_SHARD","PRISMARINE_CRYSTALS",
            "PACKED_ICE","ENCHANTED_PACKED_ICE","ICE",
            "COBBLESTONE","GRAVEL","SAND","OBSIDIAN","ENDER_STONE","MITHRIL_ORE","TITANIUM_ORE",
            "ENCHANTED_COBBLESTONE","ENCHANTED_GRAVEL","ENCHANTED_SAND","ENCHANTED_OBSIDIAN",
            "WHEAT_COLLECTION","CARROT_COLLECTION", // these are noise, will be filtered.
            "INK_SACK:3","INK_SACK:4","INK_SACK:2","INK_SACK:1","RAW_CHICKEN","PORK","RAW_BEEF",
            "ENCHANTED_PORK","ENCHANTED_RAW_CHICKEN","ENCHANTED_RAW_BEEF",
            "RED_MUSHROOM","BROWN_MUSHROOM","NETHER_STALK","CACTUS","CACTUS_GREEN"
    );
}
