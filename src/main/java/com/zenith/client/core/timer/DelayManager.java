package com.zenith.client.core.timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of per-action cooldown timers.
 *
 * <p>Macros call {@link #isReady(String)} before performing an action, and
 * {@link #reset(String, long)} after; this ensures humanised spacing between
 * interactions without {@code Thread.sleep}.</p>
 */
public final class DelayManager {

    private static DelayManager instance;
    private final Map<String, Long> nextActionAt = new ConcurrentHashMap<>();

    private DelayManager() {}

    public static DelayManager getInstance() {
        if (instance == null) instance = new DelayManager();
        return instance;
    }

    /** Mark the given action as ready only after {@code cooldownMs} from now. */
    public void reset(String action, long cooldownMs) {
        nextActionAt.put(action, System.currentTimeMillis() + cooldownMs);
    }

    /** Reset with a humanised uniform delay in [minMs, maxMs). */
    public void resetHumanised(String action, long minMs, long maxMs) {
        reset(action, RandomDelay.uniform(minMs, maxMs));
    }

    public boolean isReady(String action) {
        Long t = nextActionAt.get(action);
        return t == null || System.currentTimeMillis() >= t;
    }

    public long remainingMs(String action) {
        Long t = nextActionAt.get(action);
        if (t == null) return 0;
        return Math.max(0, t - System.currentTimeMillis());
    }

    public void clearAll() { nextActionAt.clear(); }

    public void clear(String action) { nextActionAt.remove(action); }
}
