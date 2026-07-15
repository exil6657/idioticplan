package com.zenith.client.flipping.craft;

/** Stub for GUI crafting interactions (future macro phases). Phase 9 just needs the class present. */
public final class CraftingInteractor {
    private static final CraftingInteractor INSTANCE = new CraftingInteractor();
    public static CraftingInteractor getInstance() { return INSTANCE; }
    private CraftingInteractor() {}
    public void tick() {}
}
