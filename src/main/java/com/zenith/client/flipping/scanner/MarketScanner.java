package com.zenith.client.flipping.scanner;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.api.coflnet.CoflnetAuctionAPI;
import com.zenith.client.api.moulberry.LowestBINFetcher;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.*;
import com.zenith.client.core.util.ThreadUtils;
import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipEngine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Continuously scans the auction house (Moulberry BIN snapshot + Coflnet active
 * auctions) and feeds worthwhile {@link FlipCandidate}s to the {@link FlipEngine}
 * for validation.
 *
 * <p>Scanner phases:
 * <ol>
 *   <li>Start-of-session: wait for Moulberry lowestbin.json to load.</li>
 *   <li>Every scan tick, pick a small batch of candidate ids from the filter.</li>
 *   <li>Ask Coflnet for active BIN listings for those ids.</li>
 *   <li>For each listing, run through {@link SpreadCalculator#evaluateBin(...)}.</li>
 *   <li>Hand viable candidates to FlipEngine for prioritisation.</li>
 * </ol>
 *
 * <p>Scanning runs on the I/O pool; never blocks the render thread.</p>
 */
public final class MarketScanner {

    private static final MarketScanner INSTANCE = new MarketScanner();
    public static MarketScanner getInstance() { return INSTANCE; }

    private final SpreadCalculator spread = new SpreadCalculator();
    private final ItemSelector selector = ItemSelector.getInstance();
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Candidate queue prioritised by expected profit descending. */
    private final PriorityBlockingQueue<FlipCandidate> queue =
            new PriorityBlockingQueue<>(1024, (a, b) -> Long.compare(b.expectedProfit, a.expectedProfit));

    private final List<FlipCandidate> found = new CopyOnWriteArrayList<>();
    private long lastScanMs;
    private long scans;

    private MarketScanner() {}

    public SpreadCalculator spread() { return spread; }
    public long scans() { return scans; }
    public int queueDepth() { return queue.size(); }

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        // Initial scan after Moulberry has had a chance to load (10 s).
        ThreadUtils.scheduler().scheduleWithFixedDelay(this::scanTick, 10, 4, TimeUnit.SECONDS);
        ZenithClient.LOGGER.info("[MarketScanner] Started (tick every 4 s).");
    }

    public void stop() { running.set(false); queue.clear(); }

    public void submitCandidate(FlipCandidate c) {
        if (c == null) return;
        queue.offer(c);
    }

    /** Non-blocking poll used by the FlipEngine. */
    public FlipCandidate poll() { return queue.poll(); }

    public List<FlipCandidate> recentFound() { return List.copyOf(found); }

    private void scanTick() {
        if (!running.get()) return;
        // 1. Wait until BINCache has at least a seed of data.
        if (BINCache.getInstance().size() < 100) return;
        // 2. Pick a small sample (8 ids) to deep-scan via Coflnet this tick.
        scans++;
        lastScanMs = System.currentTimeMillis();

        // Phase 9: scan a small random sample of known ids to keep request rate low.
        // A proper per-tier weight table will come in the polishing phase.
        List<String> sample = pickSample(8);
        for (String id : sample) {
            if (!selector.shouldCheckNow(id)) continue;
            selector.markChecked(id);
            CoflnetAuctionAPI.getInstance().fetchAuctions(id, 20).thenAccept(listings -> {
                if (listings == null || listings.isEmpty()) return;
                long cheapest = Long.MAX_VALUE;
                long median = median(listings);
                String name = null, seller = null, tier = null, uuid = null;
                int count = 1;
                for (var a : listings) {
                    spread.record(a.itemId(), a.price());
                    if (a.price() < cheapest) {
                        cheapest = a.price();
                        name = com.zenith.client.api.cache.ItemDatabaseCache.getInstance().getName(a.itemId());
                        seller = a.sellerName();
                        tier = a.tier();
                        uuid = a.auctionId();
                        count = a.count();
                    }
                }
                var cfg = com.zenith.client.flipping.FlipEngine.getInstance().config();
                FlipCandidate c = spread.evaluateBin(id, cheapest, median, name, count, uuid, seller, tier,
                        cfg.minProfitCoins, cfg.minProfitPercent);
                if (c != null) {
                    found.add(0, c);
                    while (found.size() > 30) found.remove(found.size() - 1);
                    ZenithEventBus.getInstance().post(
                            new com.zenith.client.core.event.events.FlipCompleteEvent(id, count, c.expectedProfit));
                    queue.offer(c);
                }
            });
        }
    }

    private List<String> pickSample(int n) {
        var cache = BINCache.getInstance();
        java.util.List<String> hot = FlipEngine.getInstance().config().hotItems;
        if (hot == null || hot.isEmpty()) hot = defaultHotItems();

        java.util.List<String> pool = new java.util.ArrayList<>(hot.size());
        for (String id : hot) {
            if (cache.get(id) > 0) pool.add(id);
        }
        // If config hotlist is too sparse, fall back to known items from ItemDatabaseCache.
        if (pool.size() < n) {
            var items = com.zenith.client.api.cache.ItemDatabaseCache.getInstance().items();
            if (items != null) {
                for (var e : items.entrySet()) {
                    String id = e.getKey();
                    if (cache.get(id) > 0 && !pool.contains(id)) pool.add(id);
                    if (pool.size() >= n * 8) break;
                }
            }
        }
        java.util.Collections.shuffle(pool);
        return pool.subList(0, Math.min(n, pool.size()));
    }

    private long median(List<CoflnetAuctionAPI.AuctionEntry> listings) {
        long[] prices = listings.stream().mapToLong(CoflnetAuctionAPI.AuctionEntry::price).sorted().toArray();
        if (prices.length == 0) return 0L;
        int mid = prices.length / 2;
        if (prices.length % 2 == 1) return prices[mid];
        return (prices[mid-1] + prices[mid]) / 2L;
    }

    private java.util.List<String> defaultHotItems() {
        return java.util.List.of(
                "ASPECT_OF_THE_END", "ASPECT_OF_THE_DRAGON", "HYPERION", "TERMINATOR", "JUJU_SHORTBOW",
                "NECRON_BLADE", "STARRED_NECRON_BLADE", "WITHER_CHESTPLATE", "NECRON_CHESTPLATE",
                "GOLDOR_CHESTPLATE", "MAXOR_CHESTPLATE", "STORM_CHESTPLATE", "SHADOW_FURY",
                "FLOWER_OF_TRUTH", "SPIRIT_BOW", "SPIRIT_SCEPTRE", "YETI_SWORD", "MIDAS_STAFF",
                "GIANTS_SWORD", "LIVID_DAGGER", "FROZEN_SCYTHE", "SOUL_WHIP", "TERRA_KNIFE",
                "ENCHANTED_DIAMOND", "ENCHANTED_EMERALD", "ENCHANTED_GOLD", "ENCHANTED_IRON",
                "ENCHANTED_COAL", "ENCHANTED_LAPIS_LAZULI", "ENCHANTED_REDSTONE",
                "HOT_POTATO_BOOK", "FUMING_POTATO_BOOK", "RECOMBOBULATOR_3000", "ART_OF_WAR",
                "ART_OF_PEACE", "FARMING_FOR_DUMMIES", "TALISMAN_OF_COINS"
        );
    }
}
