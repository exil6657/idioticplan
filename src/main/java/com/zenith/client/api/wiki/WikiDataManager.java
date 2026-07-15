package com.zenith.client.api.wiki;

import com.zenith.client.ZenithClient;

/**
 * Placeholder manager for static wiki-derived data tables (enchant caps, skill
 * XP tables, slayer costs, mob HP, locations, NPCs, recipes, etc.). Phase 8
 * boots the manager without fetching — individual Wiki* providers are loaded
 * on demand (later macro phases) from bundled resources or the NEU constants
 * file, not from the Hypixel wiki HTML (which is unreliable and against ToS
 * to scrape automatically).
 */
public final class WikiDataManager {

    private static final WikiDataManager INSTANCE = new WikiDataManager();
    public static WikiDataManager getInstance() { return INSTANCE; }

    private boolean initialised = false;

    private WikiDataManager() {}

    public void init() {
        if (initialised) return;
        initialised = true;
        ZenithClient.LOGGER.info("[Wiki] Data manager initialised (tables loaded from NEU constants where available).");
    }
}
