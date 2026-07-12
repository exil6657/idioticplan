package com.zenith.client.api.neu;

import com.google.gson.JsonObject;
import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.ItemDatabaseCache;
import com.zenith.client.core.util.ThreadUtils;

/**
 * Coordinates downloading and populating {@link ItemDatabaseCache} from NEU.
 * Called once at startup; refreshes can be requested manually via command.
 */
public final class NEUItemFetcher {

    private static final NEUItemFetcher INSTANCE = new NEUItemFetcher();
    public static NEUItemFetcher getInstance() { return INSTANCE; }

    private volatile boolean fetched = false;

    private NEUItemFetcher() {}

    public boolean isFetched() { return fetched; }

    public void fetchAsync() {
        ThreadUtils.runAsync(this::fetchNow);
    }

    public void fetchNow() {
        try {
            var future = NEURepoClient.getInstance().fetchItems();
            var el = future.join();
            if (el == null || !el.isJsonObject()) {
                ZenithClient.LOGGER.warn("[NEU.items] No data from NEU (offline?)");
                return;
            }
            JsonObject items = el.getAsJsonObject();
            ItemDatabaseCache.getInstance().setItems(items);
            fetched = true;
            ZenithClient.LOGGER.info("[NEU.items] Loaded {} items.", items.size());
        } catch (Exception e) {
            ZenithClient.LOGGER.warn("[NEU.items] failed", e);
        }
    }
}
