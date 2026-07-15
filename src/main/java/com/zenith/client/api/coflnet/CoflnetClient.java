package com.zenith.client.api.coflnet;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.HttpClient;
import com.zenith.client.api.ratelimit.RateLimiter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Client for the Coflnet SkyBlock API (api.coflnet.com) — primary source for
 * flips, price history, bazaar prices, mayor data.
 *
 * <p>Endpoints we use:
 * <ul>
 *   <li>{@code /api/v1/auctions/active/byItem/{itemId}} — active BINs for an item.</li>
 *   <li>{@code /api/v1/bazaar} — latest bazaar snapshot.</li>
 *   <li>{@code /api/v1/item/priceHistory/{itemId}?id=itemId} — price history over time.</li>
 *   <li>{@code /api/v1/timer} — mayor, event, next Jacob contest.</li>
 *   <li>{@code /api/v1/flips} — subscription-mode low-latency flip feed (Phase 9).</li>
 * </ul>
 */
public final class CoflnetClient {

    private static final CoflnetClient INSTANCE = new CoflnetClient();
    public static CoflnetClient getInstance() { return INSTANCE; }

    private static final String BASE = "https://api.coflnet.com";

    private CoflnetClient() {}

    public CompletableFuture<String> get(String path) {
        return get(path, Map.of());
    }

    public CompletableFuture<String> get(String path, Map<String, String> query) {
        String bucket = path.startsWith("/api/v1/flips") ? "coflnet.flips" : "coflnet.prices";
        if (!RateLimiter.getInstance().tryAcquire(bucket)) {
            return CompletableFuture.completedFuture(null);
        }
        String url = BASE + path + HttpClient.buildQuery(query);
        return HttpClient.getInstance().get(url).exceptionally(t -> {
            ZenithClient.LOGGER.warn("[Coflnet] GET {} failed: {}", path, t.getMessage());
            return null;
        });
    }
}
