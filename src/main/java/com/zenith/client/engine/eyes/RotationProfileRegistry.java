package com.zenith.client.engine.eyes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of named {@link RotationProfile}s. Default profiles boot at init; user profiles loaded from data/. */
public final class RotationProfileRegistry {

    private static final RotationProfileRegistry INSTANCE = new RotationProfileRegistry();
    private final Map<String, RotationProfile> profiles = new LinkedHashMap<>();
    private String defaultId = "legit";

    private RotationProfileRegistry() {
        register(RotationProfile.smooth());
        register(RotationProfile.snappy());
        register(RotationProfile.legit());
        register(RotationProfile.silent());
    }

    public static RotationProfileRegistry getInstance() { return INSTANCE; }

    public void register(RotationProfile p) { profiles.put(p.id, p); }

    public RotationProfile get(String id) {
        if (id == null) return profiles.get(defaultId);
        RotationProfile p = profiles.get(id);
        return p != null ? p : profiles.get(defaultId);
    }

    public RotationProfile getDefault() { return profiles.get(defaultId); }
    public void setDefault(String id) { if (profiles.containsKey(id)) defaultId = id; }
    public Collection<RotationProfile> all() { return Collections.unmodifiableCollection(profiles.values()); }
}
