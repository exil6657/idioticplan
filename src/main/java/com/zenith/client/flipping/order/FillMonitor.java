package com.zenith.client.flipping.order;

import com.zenith.client.api.cache.BINCache;
import com.zenith.client.core.interaction.GUIParser;
import com.zenith.client.core.interaction.GUIState;

/**
 * Checks whether our listed auctions have sold. The AH "Your Bids" / "Auctions
 * You Started" tab shows sold auctions. When an item disappears from that tab
 * and our inventory gains coins or the item leaves our inventory, we mark the
 * order COMPLETED.
 *
 * <p>Phase 9 provides the monitoring stub; full GUI-scraped detection is
 * implemented with AuctionGUIScraper in Phase 10.</p>
 */
public final class FillMonitor {

    private static final FillMonitor INSTANCE = new FillMonitor();
    public static FillMonitor getInstance() { return INSTANCE; }

    private FillMonitor() {}

    public void tick() {
        for (Order o : OrderManager.getInstance().activeOrders()) {
            if (o.state != OrderState.LISTED) continue;
            // Phase 10 will look at the "Auctions You Started" tab and match by item id.
            UndercutDetector.getInstance().check(o);
        }
    }
}
