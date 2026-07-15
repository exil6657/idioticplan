package com.zenith.client.engine.input;

/** Input engine tunables. */
public class InputConfig {
    /** If true, input overrides player keys (used by macros / autopilot). */
    public boolean inputOverride = false;
    /** Minimum repeat delay between physical key toggles (ms) — prevents jitter. */
    public long keyRepeatMinMs = 30;
    /** Maximum synthetic click rate (clicks per second). */
    public int maxCps = 14;
    /** Minimum synthetic click rate (added jitter keeps CPS natural). */
    public int minCps = 7;
}
