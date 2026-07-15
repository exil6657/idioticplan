package com.zenith.client.flipping.ah;

/** Placeholder: AH craft-flip engine (buy materials on AH, craft, resell). Wiring in Phase 12 (craft macros). */
public final class AHCraftFlipEngine {
    private static final AHCraftFlipEngine INSTANCE = new AHCraftFlipEngine();
    public static AHCraftFlipEngine getInstance() { return INSTANCE; }
    private AHCraftFlipEngine() {}
    public void tick() {}
}
