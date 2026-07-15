package com.zenith.client.api;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.coflnet.*;
import com.zenith.client.api.moulberry.LowestBINFetcher;
import com.zenith.client.api.neu.NEUConstantsFetcher;
import com.zenith.client.api.neu.NEUItemFetcher;
import com.zenith.client.api.neu.NEURecipeFetcher;
import com.zenith.client.api.ratelimit.RateLimitConfig;
import com.zenith.client.api.ratelimit.RateLimiter;
import com.zenith.client.api.scraper.*;
import com.zenith.client.api.update.UpdateChecker;
import com.zenith.client.api.wiki.WikiDataManager;
import com.zenith.client.core.chat.ZenithChat;

/**
 * Orchestrates all external data integrations for Zenith:
 *
 * <ul>
 *   <li>Rate-limit defaults applied to every HTTP bucket.</li>
 *   <li>NEU item/recipe/constants fetched on background threads at startup.</li>
 *   <li>Moulberry lowestbin poller started (every 5 minutes).</li>
 *   <li>Coflnet mayor + bazaar started (bazaar every 45 s, mayor every 60 s).</li>
 *   <li>Coflnet flip feed poller started (Phase 9 will consume the results).</li>
 *   <li>Scraper stubs registered on the event bus for future phases.</li>
 *   <li>Update checker started.</li>
 *   <li>Wiki data manager booted (tables filled as-needed).</li>
 * </ul>
 *
 * <p>All network work runs on daemon thread pools; the tick/render threads are
 * never blocked.</p>
 */
public final class ApiManager {

    private static ApiManager instance;
    public static ApiManager getInstance() {
        if (instance == null) instance = new ApiManager();
        return instance;
    }

    private boolean initialised = false;

    private ApiManager() {}

    public void init() {
        if (initialised) return;
        initialised = true;

        // 1. Rate limits.
        RateLimitConfig.applyDefaults(RateLimiter.getInstance());

        // 2. Static wiki tables.
        WikiDataManager.getInstance().init();

        // 3. Scraper stubs.
        ActionBarScraper.getInstance().init();
        ChatScraper.getInstance().init();
        ScoreboardScraper.getInstance().init();
        TabListScraper.getInstance().init();
        AuctionGUIScraper.getInstance().init();
        BazaarGUIScraper.getInstance().init();
        CollectionGUIScraper.getInstance().init();
        InventoryGUIScraper.getInstance().init();

        // 4. NEU repo — item + recipes + constants (async, 1–3 s).
        NEUItemFetcher.getInstance().fetchAsync();
        NEURecipeFetcher.getInstance().fetchAsync();
        NEUConstantsFetcher.getInstance().fetchAsync();

        // 5. Moulberry lowestbin (every 5 min).
        LowestBINFetcher.getInstance().start();

        // 6. Coflnet endpoints — bazaar (45 s), mayor (60 s), flips (3 s poll).
        CoflnetBazaarAPI.getInstance().start();
        CoflnetMayorAPI.getInstance().start();
        CoflnetFlipAPI.getInstance().start();

        // 7. Update checker.
        UpdateChecker.getInstance().start();

        ZenithClient.LOGGER.info("[ApiManager] Initialised (NEU, Moulberry, Coflnet, wiki, update, scrapers).");
        ZenithChat.getInstance().success("API/data layer initialised — NEU/Moulberry/Coflnet fetching in background.");
    }
}
