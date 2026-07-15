package com.zenith.client.flipping.ah;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/** Tracks the last N sales events (completions/failures) for reporting. */
public final class AHSalesTracker {

    private static final AHSalesTracker INSTANCE = new AHSalesTracker();
    public static AHSalesTracker getInstance() { return INSTANCE; }

    private static final int WINDOW = 64;
    private final Deque<Long> profits = new LinkedList<>();
    private final AtomicLong totalProfit = new AtomicLong();
    private final AtomicLong totalCompleted = new AtomicLong();
    private final AtomicLong totalFailed = new AtomicLong();

    private AHSalesTracker() {}

    public void recordSale(long profit) {
        synchronized (profits) {
            profits.addLast(profit);
            if (profits.size() > WINDOW) profits.pollFirst();
        }
        totalProfit.addAndGet(profit);
        totalCompleted.incrementAndGet();
    }

    public void recordFail() { totalFailed.incrementAndGet(); }

    public long totalProfit() { return totalProfit.get(); }
    public long completed() { return totalCompleted.get(); }
    public long failed() { return totalFailed.get(); }

    public double successRate() {
        long a = totalCompleted.get(), f = totalFailed.get();
        long t = a + f;
        return t == 0 ? 1.0d : (double) a / (double) t;
    }

    public void reset() {
        synchronized (profits) { profits.clear(); }
        totalProfit.set(0); totalCompleted.set(0); totalFailed.set(0);
    }
}
