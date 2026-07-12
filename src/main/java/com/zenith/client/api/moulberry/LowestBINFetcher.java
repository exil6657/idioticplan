package com.zenith.client.api.moulberry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.core.util.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

/** Periodically refetches Moulberry's lowestbin.json and updates BINCache. */
public final class LowestBINFetcher {

    private static final LowestBINFetcher INSTANCE = new LowestBINFetcher();
    public static LowestBINFetcher getInstance() { return INSTANCE; }

    private volatile boolean started = false;
    private long lastFetchAtMs;

    private LowestBINFetcher() {}

    public void start() {
        if (started) return;
        started = true;
        fetchAsync();
        // Re-fetch every 5 minutes via the shared scheduler.
        com.zenith.client.core.util.ThreadUtils.scheduler().scheduleAtFixedRate(
                this::fetchAsync, 5, 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    public long lastFetchAgeMs() { return System.currentTimeMillis() - lastFetchAtMs; }

    public void fetchAsync() { ThreadUtils.runAsync(this::fetchNow); }

    private void fetchNow() {
        try {
            String body = MoulberryClient.getInstance().fetchLowestBin().join();
            if (body == null) return;
            JsonElement el = JsonParser.parseString(body);
            if (!el.isJsonObject()) return;
            JsonObject o = el.getAsJsonObject();
            Map<String, Long> all = new HashMap<>(o.size());
            for (var e : o.entrySet()) {
                try { all.put(e.getKey(), e.getValue().getAsLong()); }
                catch (Exception ignored) {}
            }
            BINCache.getInstance().putAll(all);
            lastFetchAtMs = System.currentTimeMillis();
            ZenithClient.LOGGER.info("[Moulberry] Loaded {} lowest BIN prices.", all.size());
        } catch (Exception e) {
            ZenithClient.LOGGER.warn("[Moulberry] lowestbin parse failed", e);
        }
    }
}
