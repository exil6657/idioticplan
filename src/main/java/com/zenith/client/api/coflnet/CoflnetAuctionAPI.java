package com.zenith.client.api.coflnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lookup active auctions (BIN) for a given SkyBlock item id via Coflnet.
 *
 * <p>Used by the flipper (Phase 9) to estimate profit and by manual price
 * lookups. Results are cached in {@link BINCache} so the price for any id we
 * just fetched is immediately available without another round-trip.</p>
 */
public final class CoflnetAuctionAPI {

    private static final CoflnetAuctionAPI INSTANCE = new CoflnetAuctionAPI();
    public static CoflnetAuctionAPI getInstance() { return INSTANCE; }

    private CoflnetAuctionAPI() {}

    public record AuctionEntry(String auctionId, String itemId, long price, int count, String tier,
                               String sellerName, long startMs, long endMs) {}

    public CompletableFuture<List<AuctionEntry>> fetchAuctions(String itemId, int limit) {
        String path = "/api/v1/auctions/byItem/" + urlEncode(itemId);
        var q = Map.of("limit", Integer.toString(Math.max(1, Math.min(limit, 100))));
        return CoflnetClient.getInstance().get(path, q).thenApply(body -> {
            if (body == null) return List.<AuctionEntry>of();
            try {
                JsonElement el = JsonParser.parseString(body);
                if (!el.isJsonArray()) return List.<AuctionEntry>of();
                JsonArray arr = el.getAsJsonArray();
                List<AuctionEntry> out = new ArrayList<>(arr.size());
                long cheapest = Long.MAX_VALUE;
                for (JsonElement je : arr) {
                    if (!je.isJsonObject()) continue;
                    JsonObject o = je.getAsJsonObject();
                    long price = l(o, "startingBid", -1);
                    if (price <= 0) price = l(o, "price", -1);
                    if (price <= 0) continue;
                    String id = s(o, "itemId", itemId);
                    int count = i(o, "count", 1);
                    String tier = s(o, "tier", "");
                    String seller = s(o, "sellerName", "");
                    long start = l(o, "start", 0L);
                    long end = l(o, "end", 0L);
                    String auct = s(o, "uuid", "");
                    out.add(new AuctionEntry(auct, id, price, count, tier, seller, start, end));
                    if (price < cheapest) cheapest = price;
                }
                if (cheapest != Long.MAX_VALUE) BINCache.getInstance().put(itemId, cheapest);
                return out;
            } catch (Exception ex) {
                ZenithClient.LOGGER.warn("[Coflnet.auctions] parse failed for {}", itemId, ex);
                return List.of();
            }
        });
    }

    private static String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }

    private static String s(JsonObject o, String k, String def) {
        var e = o.get(k); return e == null || !e.isJsonPrimitive() ? def : e.getAsString();
    }
    private static long l(JsonObject o, String k, long def) {
        var e = o.get(k); if (e == null || !e.isJsonPrimitive()) return def;
        try { return e.getAsLong(); } catch (Exception ex) { return def; }
    }
    private static int i(JsonObject o, String k, int def) {
        var e = o.get(k); if (e == null || !e.isJsonPrimitive()) return def;
        try { return e.getAsInt(); } catch (Exception ex) { return def; }
    }
}
