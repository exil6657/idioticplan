package com.zenith.client.flipping.npc;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BazaarCache;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.profit.ProfitCalculator;
import com.zenith.client.flipping.tax.TaxCalculator;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NPC flip engine: looks for items that an NPC sells for less than the BIN /
 * bazaar buy price (or buys for more than bazaar sell price). Most flips here
 * are small-margin commodity trades (seeds, packed ice, etc.), but they are
 * high volume and low risk.
 */
public final class NPCFlipEngine {

    private static final NPCFlipEngine INSTANCE = new NPCFlipEngine();
    public static NPCFlipEngine getInstance() { return INSTANCE; }

    private final PriorityBlockingQueue<FlipCandidate> out = new PriorityBlockingQueue<>(64,
            (a, b) -> Long.compare(b.expectedProfit, a.expectedProfit));
    private final AtomicBoolean started = new AtomicBoolean(false);

    private NPCFlipEngine() {}

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        com.zenith.client.core.util.ThreadUtils.scheduler().scheduleAtFixedRate(
                this::scan, 60, 120, java.util.concurrent.TimeUnit.SECONDS);
    }

    public FlipCandidate pollCandidate() { return out.poll(); }

    private void scan() {
        try {
            int found = 0;
            var cfg = com.zenith.client.flipping.FlipEngine.getInstance().config();
            // Probe NPC "sell" prices (what the NPC pays the player) — if NPC pays more than
            // bazaar instant-sell, that's an NPC sell flip (rare, but e.g. Adam for candy).
            for (String id : NPC_DATABASE_KEYS) {
                long npcBuy = NPCDatabase.getInstance().sellPriceToNPC(id); // coins the NPC pays
                var bz = BazaarCache.getInstance().get(id);
                long bin = BINCache.getInstance().get(id);
                long sellToBz = bz != null ? TaxCalculator.bazaarInstantSellReceived((long) bz.buyPrice(), 1) : -1;
                long sellToBin = bin > 0 ? TaxCalculator.netFromBin(bin) : -1;
                long market = Math.max(sellToBz, sellToBin);
                if (npcBuy <= 0 || market <= 0) continue;
                long profit = npcBuy - market;
                double roi = market > 0 ? (double) profit / market : 0;
                if (profit < cfg.minProfitCoins || roi < cfg.minProfitPercent) continue;
                FlipCandidate c = new FlipCandidate(id, id, FlipType.NPC_RESELL,
                        market, npcBuy, profit, roi, 1000, 0.4, 60_000L, "", "", 1, "");
                if (BudgetManager.getInstance().canBuy(market)) {
                    out.offer(c);
                    found++;
                }
            }
            if (found > 0) ZenithClient.LOGGER.debug("[NPCFlip] found {} candidates", found);
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[NPCFlip] scan failed", t);
        }
    }

    private static final String[] NPC_DATABASE_KEYS = {
            "WHEAT","CARROT_ITEM","POTATO_ITEM","MELON","PUMPKIN","SUGAR_CANE",
            "BROWN_MUSHROOM","RED_MUSHROOM","CACTUS","BLAZE_ROD","COBBLESTONE",
            "DIAMOND","IRON_INGOT","GOLD_INGOT","COAL","EMERALD"
    };
}
