package com.zenith.client.api.scraper;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.ZenithEventBus;

/**
 * AuctionGUIScraper — lightweight in-game data scraper.
 *
 * <p>Phase 8 registers the scraper on the event bus but leaves detailed
 * parsing to the macro/economy phases that consume the data. This keeps Phase 8
 * free of gameplay-specific heuristics.</p>
 */
public final class AuctionGUIScraper {

    private static final AuctionGUIScraper INSTANCE = new AuctionGUIScraper();
    public static AuctionGUIScraper getInstance() { return INSTANCE; }
    private boolean registered = false;

    private AuctionGUIScraper() {}

    public void init() {
        if (registered) return;
        ZenithEventBus.getInstance().register(this);
        registered = true;
    }
}
