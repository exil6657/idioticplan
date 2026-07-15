package com.zenith.client.api.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory NEU items.json cache (display name, NPC sell price, tier, etc.).
 * Populated by {@link com.zenith.client.api.neu.NEUItemFetcher} at startup.
 */
public final class ItemDatabaseCache {

    public static final class ItemInfo {
        private final String itemId;
        private final String name;
        private final long npcSell;
        private final String tier;
        private final JsonObject raw;

        public ItemInfo(String itemId, String name, long npcSell, String tier, JsonObject raw) {
            this.itemId = itemId;
            this.name = name;
            this.npcSell = npcSell;
            this.tier = tier;
            this.raw = raw;
        }

        public String itemId()   { return itemId; }
        public String name()     { return name == null ? itemId : name; }
        public long npcSell()    { return npcSell; }
        public String tier()     { return tier; }
        public JsonObject raw()  { return raw; }
    }

    private static final ItemDatabaseCache INSTANCE = new ItemDatabaseCache();
    public static ItemDatabaseCache getInstance() { return INSTANCE; }

    private volatile Map<String, ItemInfo> items = Collections.emptyMap();
    private volatile JsonObject rawJson = new JsonObject();

    private ItemDatabaseCache() {}

    public void setItems(JsonObject json) {
        if (json == null) return;
        Map<String, ItemInfo> map = new LinkedHashMap<>();
        for (var e : json.entrySet()) {
            String id = e.getKey();
            JsonElement el = e.getValue();
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            String name = o.has("name") ? o.get("name").getAsString() : id;
            long npc = -1L;
            if (o.has("npc_sell_price") && !o.get("npc_sell_price").isJsonNull()) {
                try { npc = o.get("npc_sell_price").getAsLong(); } catch (Exception ignored) {}
            }
            String tier = o.has("tier") ? o.get("tier").getAsString() : "";
            map.put(id.toUpperCase(), new ItemInfo(id, name, npc, tier, o));
        }
        this.items = Map.copyOf(map);
        this.rawJson = json;
    }

    public String getName(String itemId) {
        if (itemId == null) return "";
        ItemInfo i = items.get(itemId.toUpperCase());
        if (i == null) return itemId;
        return i.name().replaceAll("§.", "");
    }

    public long getNpcSell(String itemId) {
        if (itemId == null) return -1L;
        ItemInfo i = items.get(itemId.toUpperCase());
        return i == null ? -1L : i.npcSell();
    }

    public ItemInfo getItem(String itemId) {
        if (itemId == null) return null;
        return items.get(itemId.toUpperCase());
    }

    public Map<String, ItemInfo> items() { return items; }
    public JsonObject raw() { return rawJson; }
    public int size() { return items.size(); }
}
