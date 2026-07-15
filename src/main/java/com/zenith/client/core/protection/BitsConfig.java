package com.zenith.client.core.protection;

/** Bits protection configuration (loaded from config). */
public class BitsConfig {

    /** Block all clicks that would spend bits above this threshold. */
    public boolean enabled = true;

    /** Per-click threshold — clicks costing more than this many bits are blocked. */
    public long perClickThreshold = 0; // 0 = block ALL bits spending by default

    /** Allow bits spending only when shift is held (safeguard). */
    public boolean requireShiftForSpend = true;

    /** Play an audible alert when a bits spend is blocked. */
    public boolean alertOnBlock = true;

    /** Specific items/features to always block (matilda, experiment serum, etc.). */
    public java.util.List<String> blockList = new java.util.ArrayList<>(java.util.List.of(
        "experiment_serum",
        "matilda_flip",
        "kat_flower",
        "abiphone_contact"
    ));
}
