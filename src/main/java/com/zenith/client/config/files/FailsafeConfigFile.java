package com.zenith.client.config.files;

import com.zenith.client.failsafe.FailsafeConfig;

/**
 * Persisted failsafe config (config/failsafe.json). Stores per-detector
 * enabled flags, severity overrides, alert sound preferences, etc.
 */
public class FailsafeConfigFile extends FailsafeConfig {

    /** Schema version — bump when adding/renaming fields. */
    public int schemaVersion = 1;

    public FailsafeConfigFile() {
        super();
    }
}
