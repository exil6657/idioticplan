package com.zenith.client;

/**
 * Central constants for the Zenith Client mod.
 *
 * <p>Version, build date, and git commit are injected by the {@code generateVersion}
 * Gradle task at build time. The default values below are used when running from an
 * IDE without that task.</p>
 *
 * <p>R001 research note: minimum Fabric Loader for MC 26.1.2 is 0.18.4 per the Fabric
 * 26.1 release announcement; we pin to 0.18.9 to pull in post-release fixes.</p>
 *
 * @author Exil
 */
public final class ZenithClientInfo {

    public static final String MOD_ID = "zenithclient";
    public static final String MOD_NAME = "Zenith Client";
    public static final String DEVELOPER = "Exil";
    public static final String MC_VERSION = "26.1.2";

    /** Injected by Gradle; defaults to {@code "1.0.0-dev"} in IDE runs. */
    public static final String VERSION = "1.0.0";

    /** Injected by Gradle ({@code BUILD_DATE_INJECTED_BY_GRADLE} placeholder replaced). */
    public static final String BUILD_DATE = "BUILD_DATE_INJECTED_BY_GRADLE";

    /** Injected by Gradle ({@code GIT_HASH_INJECTED_BY_GRADLE} placeholder replaced). */
    public static final String GIT_COMMIT = "GIT_HASH_INJECTED_BY_GRADLE";

    /** Minimum Fabric Loader version (per R001 research). */
    public static final String MIN_FABRIC_LOADER = "0.18.4";

    // Feature flags — disabled until their respective phases (per master doc Part 5).
    public static final boolean DUNGEON_ENABLED = false;  // Phase 19
    public static final boolean KUUDRA_ENABLED = false;   // Phase 19

    private ZenithClientInfo() {
        // Utility class — prevent instantiation.
    }
}
