package com.zenith.client.flipping.scanner;

import java.util.Arrays;

/**
 * Rolling price window for an item. Keeps the last N observed prices and
 * exposes mean, median, std-dev, and percentile for outlier detection. Used
 * by SpreadCalculator to reject outlier listings (manipulated auctions).
 */
public final class PriceHistory {

    private final long[] buf;
    private int idx = 0;
    private int count = 0;
    private long sum = 0;

    public PriceHistory(int window) { this.buf = new long[Math.max(8, window)]; }

    public synchronized void add(long price) {
        if (price <= 0) return;
        if (count == buf.length) sum -= buf[idx];
        buf[idx] = price;
        sum += price;
        idx = (idx + 1) % buf.length;
        if (count < buf.length) count++;
    }

    public synchronized long mean() { return count == 0 ? 0L : sum / count; }
    public synchronized int count() { return count; }

    public synchronized long median() {
        if (count == 0) return 0L;
        long[] copy = Arrays.copyOf(buf, count);
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    public synchronized double stdDev() {
        if (count < 2) return 0d;
        double m = (double) sum / count;
        double s = 0d;
        for (int i = 0; i < count; i++) s += (buf[i] - m) * (buf[i] - m);
        return Math.sqrt(s / count);
    }

    /** Returns true if the price is more than {@code sigma} standard deviations below the mean. */
    public synchronized boolean isOutlierLow(long price, double sigma) {
        if (count < 4) return false;
        double sd = stdDev();
        if (sd <= 0d) return false;
        double m = (double) sum / count;
        return price < m - sigma * sd;
    }
}
