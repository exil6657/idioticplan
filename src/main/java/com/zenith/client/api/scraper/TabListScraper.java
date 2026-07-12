package com.zenith.client.api.scraper;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.ZenithEventBus;

/**
 * TabListScraper — lightweight in-game data scraper.
 *
 * <p>Phase 8 registers the scraper on the event bus but leaves detailed
 * parsing to the macro/economy phases that consume the data. This keeps Phase 8
 * free of gameplay-specific heuristics.</p>
 */
public final class TabListScraper {

    private static final TabListScraper INSTANCE = new TabListScraper();
    public static TabListScraper getInstance() { return INSTANCE; }
    private boolean registered = false;

    private TabListScraper() {}

    public void init() {
        if (registered) return;
        ZenithEventBus.getInstance().register(this);
        registered = true;
    }
}
