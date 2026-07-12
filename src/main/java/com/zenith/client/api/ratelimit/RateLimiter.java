package com.zenith.client.api.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token-bucket rate limiter used to throttle outbound HTTP calls.
 *
 * <p>Each named bucket tracks a refill rate (tokens per second) and capacity.
 * Thread-safe; non-blocking — {@link #tryAcquire(String)} returns immediately
 * with {@code false} if a request would exceed the bucket.</p>
 */
public final class RateLimiter {

    private static final RateLimiter INSTANCE = new RateLimiter();
    public static RateLimiter getInstance() { return INSTANCE; }

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private RateLimiter() {}

    public void configure(String name, double tokensPerSecond, long capacityMs) {
        buckets.put(name, new Bucket(tokensPerSecond, capacityMs));
    }

    public boolean tryAcquire(String name) {
        Bucket b = buckets.get(name);
        if (b == null) return true; // no limit configured
        return b.tryAcquire();
    }

    /** Milliseconds until a token will be available (0 = ready now). */
    public long waitMs(String name) {
        Bucket b = buckets.get(name);
        if (b == null) return 0L;
        long now = System.nanoTime();
        double available = b.available(now);
        if (available >= 1d) return 0L;
        double deficit = 1d - available;
        return (long) Math.ceil((deficit / b.tokensPerNano) / 1_000_000d);
    }

    private static final class Bucket {
        final double tokensPerNano;
        final long capacityNanos;
        final AtomicLong lastRefillNanos;
        double tokens;

        Bucket(double tokensPerSecond, long capacityMs) {
            this.tokensPerNano = tokensPerSecond / 1_000_000_000d;
            this.capacityNanos = capacityMs * 1_000_000L;
            this.tokens = 2d; // seed
            this.lastRefillNanos = new AtomicLong(System.nanoTime());
        }

        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos.get();
            tokens = Math.min(tokens + elapsed * tokensPerNano, 2d);
            lastRefillNanos.set(now);
            if (tokens >= 1d) {
                tokens -= 1d;
                return true;
            }
            return false;
        }

        synchronized double available(long now) {
            long elapsed = now - lastRefillNanos.get();
            return Math.min(tokens + elapsed * tokensPerNano, 2d);
        }
    }
}
