package com.zenith.client.core.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Generic per-id cooldown tracker for attacks/abilities/items. */
public final class CooldownTracker {
    private static final CooldownTracker INSTANCE = new CooldownTracker();
    public static CooldownTracker getInstance() { return INSTANCE; }
    private final Map<String, Long> readyAt = new ConcurrentHashMap<>();

    public void set(String id, long cooldownMs) { readyAt.put(id, System.currentTimeMillis() + cooldownMs); }
    public boolean ready(String id) {
        Long t = readyAt.get(id); return t == null || System.currentTimeMillis() >= t;
    }
    public long remaining(String id) {
        Long t = readyAt.get(id); return t == null ? 0 : Math.max(0, t - System.currentTimeMillis());
    }
}
