package com.zenith.client.api.neu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.ThreadUtils;

/**
 * Downloads NEU constants.json which includes (among other things) pet numbers,
 * enchant caps, skill xp tables, essence costs, etc. Populated into a plain
 * JsonObject for later phases to query.
 */
public final class NEUConstantsFetcher {

    private static final NEUConstantsFetcher INSTANCE = new NEUConstantsFetcher();
    public static NEUConstantsFetcher getInstance() { return INSTANCE; }

    private volatile JsonObject constants;
    private volatile boolean fetched = false;

    private NEUConstantsFetcher() {}

    public JsonObject constants() { return constants; }
    public boolean isFetched() { return fetched; }

    public void fetchAsync() { ThreadUtils.runAsync(this::fetchNow); }

    public void fetchNow() {
        try {
            JsonElement el = NEURepoClient.getInstance().fetchConstants().join();
            if (el == null || !el.isJsonObject()) return;
            constants = el.getAsJsonObject();
            fetched = true;
            ZenithClient.LOGGER.info("[NEU.constants] Loaded.");
        } catch (Exception e) {
            ZenithClient.LOGGER.warn("[NEU.constants] failed", e);
        }
    }
}
