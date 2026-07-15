package com.zenith.client.core.module;

/** Category buckets for the click GUI. Names are en_gb. */
public enum ModuleCategory {

    COMBAT("Combat"),
    FARMING("Farming"),
    MINING("Mining"),
    FORAGING("Foraging"),
    FISHING("Fishing"),
    DUNGEON("Dungeons"),
    KUUDRA("Kuudra"),
    RIFT("Rift"),
    SLAYER("Slayer"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    ESP("ESP"),
    CHAT("Chat"),
    MISC("Misc"),
    DEBUG("Debug"),
    EXPLOIT("Exploit");

    private final String displayName;

    ModuleCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
