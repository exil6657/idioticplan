package com.zenith.client.api.update;

import com.google.gson.annotations.Expose;

/** Persisted configuration for the update checker. */
public class UpdateConfig {
    @Expose public boolean enabled = true;
    @Expose public long checkIntervalMs = 6L * 60L * 60L * 1000L; // 6 h
    @Expose public boolean notifyToasts = true;
    @Expose public String lastSeenVersion = "";
}
