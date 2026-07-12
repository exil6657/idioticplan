package com.zenith.client.flipping.filter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of filter presets (lowball, high-volume, big-ticket, user-defined). */
public final class ItemFilterManager {

    private static final ItemFilterManager INSTANCE = new ItemFilterManager();
    public static ItemFilterManager getInstance() { return INSTANCE; }

    private final Map<String, FilterPreset> presets = new LinkedHashMap<>();
    private String active = "high-volume";

    private ItemFilterManager() {
        register(FilterPreset.lowball());
        register(FilterPreset.highVolume());
        register(FilterPreset.bigTicket());
    }

    public void register(FilterPreset p) { presets.put(p.name, p); }

    public ItemFilter activeFilter() {
        var p = presets.get(active);
        return p != null ? p.filter : new ItemFilter();
    }

    public String activeName() { return active; }
    public void setActive(String name) { if (presets.containsKey(name)) this.active = name; }
    public Collection<String> presetNames() { return presets.keySet(); }
}
