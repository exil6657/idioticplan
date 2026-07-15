package com.zenith.client.flipping.craft;

import com.zenith.client.ZenithClient;
import com.zenith.client.api.cache.BINCache;
import com.zenith.client.flipping.FlipCandidate;
import com.zenith.client.flipping.FlipType;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.profit.ProfitCalculator;
import com.zenith.client.flipping.tax.TaxCalculator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Compares craft cost of known recipes to current BIN sell price to find
 * profitable craft flips. Phase 9 only finds candidates; the GUI clicks to
 * buy ingredients/craft/list are added in later macro phases.
 */
public final class CraftFlipEngine {

    private static final CraftFlipEngine INSTANCE = new CraftFlipEngine();
    public static CraftFlipEngine getInstance() { return INSTANCE; }

    private final AtomicBoolean started = new AtomicBoolean(false);
    private long lastScanMs;
    private int candidatesLastScan;

    private CraftFlipEngine() {}

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        com.zenith.client.core.util.ThreadUtils.scheduler().scheduleAtFixedRate(
                this::scan, 30, 120, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void scan() {
        try {
            RecipeDatabase.getInstance().refresh();
            int found = 0;
            var cfg = com.zenith.client.flipping.FlipEngine.getInstance().config();
            for (String output : new String[]{
                    "ENCHANTED_DIAMOND","ENCHANTED_IRON","ENCHANTED_GOLD","ENCHANTED_COAL",
                    "ENCHANTED_EMERALD","ENCHANTED_LAPIS_LAZULI","ENCHANTED_REDSTONE",
                    "SUPER_COMPACTOR_3000","HOT_POTATO_BOOK","FUMING_POTATO_BOOK"}) {
                long craftCost = RecipeDatabase.getInstance().costFor(output);
                long sellPrice = BINCache.getInstance().get(output);
                if (craftCost <= 0 || sellPrice <= 0) continue;
                long net = TaxCalculator.netFromBin(sellPrice);
                long profit = net - craftCost;
                if (profit < cfg.minProfitCoins) continue;
                double roi = (double) profit / (double) craftCost;
                if (roi < cfg.minProfitPercent) continue;
                var c = new FlipCandidate(output, output, FlipType.CRAFT, craftCost, sellPrice,
                        profit, roi, 100, 0.6, 600_000L, "", "", 1, "");
                if (BudgetManager.getInstance().canBuy(craftCost)) {
                    com.zenith.client.flipping.FlipEngine.getInstance().orders().enqueue(c);
                    found++;
                }
            }
            candidatesLastScan = found;
            lastScanMs = System.currentTimeMillis();
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[CraftFlip] scan failed", t);
        }
    }
}
