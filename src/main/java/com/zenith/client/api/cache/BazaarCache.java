package com.zenith.client.api.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of Bazaar product prices, populated by
 * {@link com.zenith.client.api.coflnet.CoflnetBazaarAPI} and read by the
 * Bazaar flip engine, craft-flip engine, and NPC-flip engine.
 *
 * <p>Conventions (Hypixel Bazaar semantics):
 * <ul>
 *   <li>{@code buyPrice} = the top <b>buy order</b> price (coins/unit someone
 *       is offering to buy at — this is what you receive when you
 *       <i>instant-sell</i>).</li>
 *   <li>{@code sellPrice} = the top <b>sell offer</b> price (coins/unit someone
 *       is asking — this is what you pay when you <i>instant-buy</i>).</li>
 * </ul>
 * Volumes are in units moved over the last week (Coflnet snapshot).
 */
public final class BazaarCache {

    public static final class BazaarEntry {
        private final double buyPrice;
        private final double sellPrice;
        private final long buyVolume;
        private final long sellVolume;
        private final long fetchedAtMs;

        public BazaarEntry(double buyPrice, double sellPrice, long buyVolume, long sellVolume, long fetchedAtMs) {
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.buyVolume = buyVolume;
            this.sellVolume = sellVolume;
            this.fetchedAtMs = fetchedAtMs;
        }

        public double buyPrice()      { return buyPrice; }
        public double sellPrice()     { return sellPrice; }
        public double instantBuyPrice()  { return sellPrice; }
        public double instantSellPrice() { return buyPrice; }
        public long buyVolume()       { return buyVolume; }
        public long sellVolume()      { return sellVolume; }
        public long fetchedAtMs()     { return fetchedAtMs; }
        public long ageMs()           { return System.currentTimeMillis() - fetchedAtMs; }
    }

    private static final BazaarCache INSTANCE = new BazaarCache();
    public static BazaarCache getInstance() { return INSTANCE; }

    private final Map<String, BazaarEntry> entries = new ConcurrentHashMap<>();

    private BazaarCache() {}

    public BazaarEntry get(String productId) {
        if (productId == null) return null;
        return entries.get(productId.toUpperCase());
    }

    public void put(String productId, BazaarEntry e) {
        if (productId == null || e == null) return;
        entries.put(productId.toUpperCase(), e);
    }

    public void putAll(Map<String, BazaarEntry> all) {
        if (all == null) return;
        for (var e : all.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) continue;
            entries.put(e.getKey().toUpperCase(), e.getValue());
        }
    }

    public int size() { return entries.size(); }
    public Set<String> keySet() { return Set.copyOf(entries.keySet()); }
    public void clear() { entries.clear(); }

    public long ageMs(String productId) {
        BazaarEntry e = get(productId);
        return e == null ? Long.MAX_VALUE : e.ageMs();
    }
}
