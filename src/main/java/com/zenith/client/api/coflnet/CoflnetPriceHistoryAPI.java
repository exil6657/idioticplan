package com.zenith.client.api.coflnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Fetches 7-day price history for an item (avg/min/max volume). */
public final class CoflnetPriceHistoryAPI {

    private static final CoflnetPriceHistoryAPI INSTANCE = new CoflnetPriceHistoryAPI();
    public static CoflnetPriceHistoryAPI getInstance() { return INSTANCE; }

    public record PricePoint(long timestampMs, double avg, double min, double max, long volume) {}

    private CoflnetPriceHistoryAPI() {}

    public CompletableFuture<List<PricePoint>> fetchHistory(String itemId) {
        return CoflnetClient.getInstance()
                .get("/api/v1/item/priceHistory/" + enc(itemId), Map.of("range", "7d"))
                .thenApply(body -> {
                    if (body == null) return List.<PricePoint>of();
                    try {
                        JsonElement el = JsonParser.parseString(body);
                        if (!el.isJsonArray()) return List.<PricePoint>of();
                        JsonArray arr = el.getAsJsonArray();
                        List<PricePoint> out = new ArrayList<>(arr.size());
                        for (JsonElement je : arr) {
                            if (!je.isJsonObject()) continue;
                            var o = je.getAsJsonObject();
                            out.add(new PricePoint(
                                    l(o, "time", 0L),
                                    d(o, "avg", 0d),
                                    d(o, "min", 0d),
                                    d(o, "max", 0d),
                                    l(o, "volume", 0L)));
                        }
                        return out;
                    } catch (Exception e) {
                        ZenithClient.LOGGER.warn("[Coflnet.history] failed for {}", itemId, e);
                        return List.of();
                    }
                });
    }

    private static String enc(String s) {
        try { return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
    private static long l(com.google.gson.JsonObject o, String k, long def) {
        var e = o.get(k); if (e == null || !e.isJsonPrimitive()) return def;
        try { return e.getAsLong(); } catch (Exception ex) { return def; }
    }
    private static double d(com.google.gson.JsonObject o, String k, double def) {
        var e = o.get(k); if (e == null || !e.isJsonPrimitive()) return def;
        try { return e.getAsDouble(); } catch (Exception ex) { return def; }
    }
}
