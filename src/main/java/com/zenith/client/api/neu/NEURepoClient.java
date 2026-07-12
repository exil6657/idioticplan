package com.zenith.client.api.neu;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.HttpClient;
import com.zenith.client.api.ratelimit.RateLimiter;

import java.util.concurrent.CompletableFuture;

/**
 * Client for the NotEnoughUpdates public constants repo
 * ({@code https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates/master/...}).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code /constants/items.json} — the full items database</li>
 *   <li>{@code constants/recipes.json} — recipes keyed by item id</li>
 *   <li>{@code constants/constants.json} — misc numeric constants</li>
 * </ul>
 *
 * <p>We hit the raw.githubusercontent.com CDN directly rather than the NEU mod API,
 * which keeps us from depending on an auth token. TTL is long (6 hours) so we don't
 * hammer GitHub.</p>
 */
public final class NEURepoClient {

    private static final NEURepoClient INSTANCE = new NEURepoClient();
    public static NEURepoClient getInstance() { return INSTANCE; }

    private static final String REPO_BASE = "https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates/master/";
    private static final String ITEMS_JSON = REPO_BASE + "src/main/resources/assets/notenoughupdates/repo/constants/items.json";
    private static final String RECIPES_JSON = REPO_BASE + "src/main/resources/assets/notenoughupdates/repo/constants/recipes.json";
    private static final String CONSTANTS_JSON = REPO_BASE + "src/main/resources/assets/notenoughupdates/repo/constants/constants.json";

    private long lastItemsAttemptAt;
    private long lastRecipesAttemptAt;

    private NEURepoClient() {}

    public CompletableFuture<JsonElement> fetchItems() {
        return fetch("neu.items", ITEMS_JSON);
    }

    public CompletableFuture<JsonElement> fetchRecipes() {
        return fetch("neu.items", RECIPES_JSON);
    }

    public CompletableFuture<JsonElement> fetchConstants() {
        return fetch("neu.repo", CONSTANTS_JSON);
    }

    private CompletableFuture<JsonElement> fetch(String bucket, String url) {
        if (!RateLimiter.getInstance().tryAcquire(bucket)) {
            CompletableFuture<JsonElement> f = new CompletableFuture<>();
            f.complete(null); // rate limited; try next tick/cycle
            return f;
        }
        return HttpClient.getInstance().get(url).thenApply(s -> {
            try { return JsonParser.parseString(s); }
            catch (Exception ex) { ZenithClient.LOGGER.warn("[NEU] parse failed for {}: {}", url, ex.getMessage()); return null; }
        }).exceptionally(t -> {
            ZenithClient.LOGGER.warn("[NEU] fetch failed for {}: {}", url, t.getMessage());
            return null;
        });
    }
}
