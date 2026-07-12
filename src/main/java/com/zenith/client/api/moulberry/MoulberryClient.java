package com.zenith.client.api.moulberry;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.HttpClient;
import com.zenith.client.api.ratelimit.RateLimiter;

import java.util.concurrent.CompletableFuture;

/**
 * Client for Moulberry's {@code moulberry.net} NEU BIN API.
 *
 * <p>{@code https://moulberry.codes/lowestbin.json} returns a simple JSON map
 * of skyblock id → lowest BIN price (coins). The endpoint updates every few
 * minutes so we use a long TTL and don't refresh more than once every 10 s.</p>
 */
public final class MoulberryClient {

    private static final MoulberryClient INSTANCE = new MoulberryClient();
    public static MoulberryClient getInstance() { return INSTANCE; }

    private static final String LOWEST_BIN = "https://moulberry.codes/lowestbin.json";

    private MoulberryClient() {}

    public CompletableFuture<String> fetchLowestBin() {
        if (!RateLimiter.getInstance().tryAcquire("moulberry.bin")) {
            return CompletableFuture.completedFuture(null);
        }
        return HttpClient.getInstance().get(LOWEST_BIN).exceptionally(t -> {
            ZenithClient.LOGGER.warn("[Moulberry] fetch failed: {}", t.getMessage());
            return null;
        });
    }
}
