package com.zenith.client.flipping.budget;

import com.google.gson.annotations.Expose;

import java.util.concurrent.atomic.AtomicLong;

/** Budget and profit targets for the flipper. Thread-safe. */
public class BudgetConfig {
    @Expose public volatile long maxCoinsPerFlip = 5_000_000L;
    @Expose public volatile long bankReserve = 1_000_000L;
    @Expose public volatile long sessionProfitTarget = 5_000_000L;
    @Expose public volatile long sessionLossCeiling = 500_000L;
    @Expose public volatile long coinsAvailable = 0L;
    @Expose public volatile int maxOpenOrders = 6;
    @Expose public volatile long minProfitCoins = 15_000L;
    @Expose public volatile double minProfitPercent = 0.08d;

    private final AtomicLong spentThisSession = new AtomicLong();
    private final AtomicLong earnedThisSession = new AtomicLong();

    public boolean canAfford(long price) {
        return (coinsAvailable - spentThisSession.get()) >= price && price <= maxCoinsPerFlip && price > bankReserve;
    }

    public void recordBuy(long price) { spentThisSession.addAndGet(price); }
    public void recordSell(long profit) { earnedThisSession.addAndGet(profit); }
    public long sessionNet() { return earnedThisSession.get() - spentThisSession.get(); }
    public void resetSession() { spentThisSession.set(0); earnedThisSession.set(0); }
}
