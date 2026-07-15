package com.zenith.client.api.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory lowest-BIN (Buy It Now) price cache, populated by
 * {@link com.zenith.client.api.moulberry.LowestBINFetcher} (Moulberry
 * lowestbin.json) and augmented by {@link com.zenith.client.api.coflnet.CoflnetAuctionAPI}.
 */
public final class BINCache {

    private static final BINCache INSTANCE = new BINCache();
    public static BINCache getInstance() { return INSTANCE; }

    private final Map<String, Long> prices = new ConcurrentHashMap<>();
    private volatile long lastFullUpdateMs;

    private BINCache() {}

    /** @return lowest BIN price for {@code itemId}, or -1 if unknown. */
    public long get(String itemId) {
        if (itemId == null) return -1L;
        Long v = prices.get(itemId.toUpperCase());
        return v == null ? -1L : v;
    }

    public void put(String itemId, long price) {
        if (itemId == null || price <= 0) return;
        prices.put(itemId.toUpperCase(), price);
    }

    public void putAll(Map<String, Long> all) {
        if (all == null) return;
        for (var e : all.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() <= 0) continue;
            prices.put(e.getKey().toUpperCase(), e.getValue());
        }
        lastFullUpdateMs = System.currentTimeMillis();
    }

    public int size() { return prices.size(); }
    public Set<String> keySet() { return Set.copyOf(prices.keySet()); }
    public long ageMs(String itemId) {
        if (lastFullUpdateMs == 0) return Long.MAX_VALUE;
        return System.currentTimeMillis() - lastFullUpdateMs;
    }
    public void clear() { prices.clear(); lastFullUpdateMs = 0; }
}
