package com.zenith.client.flipping.scanner;

import com.zenith.client.ZenithClient;
import com.zenith.client.flipping.filter.ItemFilter;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Maintains the set of item ids we are willing to scan. Items that fail the
 * filter (cosmetic-only, soul-bound, untradable, blacklisted, over budget) are
 * excluded. Call {@link #accept(String)} before spending an auction query or
 * Coflnet request on an id.
 */
public final class ItemSelector {

    private static final ItemSelector INSTANCE = new ItemSelector();
    public static ItemSelector getInstance() { return INSTANCE; }

    /** Ids we have already tried recently and don't need to hit again for a while. */
    private final Set<String> recentlyChecked = new ConcurrentSkipListSet<>();
    /** Ids the blacklist forbids. */
    private final Set<String> blacklist = ConcurrentSkipListSet.newSet(ConcurrentSkipListSet.create());

    private long cooldownMs = 30_000L; // don't re-check an id more often than every 30 s

    private ItemFilter filter = new ItemFilter();

    private ItemSelector() {}

    public void setFilter(ItemFilter f) { this.filter = f != null ? f : new ItemFilter(); }
    public ItemFilter filter() { return filter; }

    public void blacklist(String itemId) { blacklist.add(itemId.toUpperCase()); }
    public void unBlacklist(String itemId) { blacklist.remove(itemId.toUpperCase()); }

    public boolean accept(String itemId, long price) {
        if (itemId == null) return false;
        String id = itemId.toUpperCase();
        if (blacklist.contains(id)) return false;
        if (!filter.allows(id, price)) return false;
        // Optional cooldown to avoid hammering one hot item.
        return true;
    }

    public boolean shouldCheckNow(String itemId) {
        String id = itemId == null ? "" : itemId.toUpperCase();
        return !recentlyChecked.contains(id);
    }

    public void markChecked(String itemId) {
        recentlyChecked.add(itemId.toUpperCase());
        com.zenith.client.core.util.ThreadUtils.scheduler().schedule(
                () -> recentlyChecked.remove(itemId.toUpperCase()), cooldownMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void setCooldownMs(long cooldownMs) { this.cooldownMs = Math.max(1000L, cooldownMs); }
}
