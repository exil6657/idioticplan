package com.zenith.client.engine.path.learning.analysis;

/** Welford's online algorithm for mean and variance. */
public final class StreamingStats {
    private int n;
    private double mean, m2;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    public void add(double v) {
        n++;
        double delta = v - mean;
        mean += delta / n;
        double delta2 = v - mean;
        m2 += delta * delta2;
        if (v < min) min = v;
        if (v > max) max = v;
    }

    public int count() { return n; }
    public double mean() { return n == 0 ? 0 : mean; }
    public double variance() { return n < 2 ? 0 : m2 / (n - 1); }
    public double stddev() { return Math.sqrt(variance()); }
    public double min() { return min; }
    public double max() { return max; }
}
