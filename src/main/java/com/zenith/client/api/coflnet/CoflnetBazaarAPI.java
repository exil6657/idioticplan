package com.zenith.client.api.coflnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BazaarCache;
import com.zenith.client.core.util.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fetches the Coflnet bazaar snapshot endpoint and populates {@link BazaarCache}.
 * Rate-limited to ≤2 req/s; scheduled refresh every 45 seconds.
 */
public final class CoflnetBazaarAPI {

    private static final CoflnetBazaarAPI INSTANCE = new CoflnetBazaarAPI();
    public static CoflnetBazaarAPI getInstance() { return INSTANCE; }

    private volatile boolean started = false;

    private CoflnetBazaarAPI() {}

    public void start() {
        if (started) return;
        started = true;
        refreshAsync();
        ThreadUtils.scheduler().scheduleAtFixedRate(this::refreshAsync, 45, 45, java.util.concurrent.TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> refreshAsync() {
        return CompletableFuture.runAsync(this::fetchNow, ThreadUtils.ioPool());
    }

    private void fetchNow() {
        try {
            String body = CoflnetClient.getInstance().get("/api/v1/bazaar").join();
            if (body == null) return;
            JsonElement el = JsonParser.parseString(body);
            if (!el.isJsonArray()) return;
            JsonArray arr = el.getAsJsonArray();
            Map<String, BazaarCache.BazaarEntry> out = new HashMap<>();
            for (JsonElement je : arr) {
                if (!je.isJsonObject()) continue;
                JsonObject o = je.getAsJsonObject();
                String id = string(o, "productId");
                if (id == null) continue;
                double buy = d(o, "buyPrice", -1d);
                double sell = d(o, "sellPrice", -1d);
                double bv = d(o, "buyVolume", 0d);
                double sv = d(o, "sellVolume", 0d);
                out.put(id, new BazaarCache.BazaarEntry(buy, sell, bv, sv, System.currentTimeMillis()));
            }
            BazaarCache.getInstance().putAll(out);
            ZenithClient.LOGGER.info("[Coflnet.bazaar] Loaded {} products.", out.size());
        } catch (Exception e) {
            ZenithClient.LOGGER.warn("[Coflnet.bazaar] failed", e);
        }
    }

    private static String string(JsonObject o, String k) {
        var e = o.get(k); return e == null || !e.isJsonPrimitive() ? null : e.getAsString();
    }

    private static double d(JsonObject o, String k, double def) {
        var e = o.get(k);
        if (e == null || !e.isJsonPrimitive()) return def;
        try { return e.getAsDouble(); } catch (Exception ex) { return def; }
    }
}
