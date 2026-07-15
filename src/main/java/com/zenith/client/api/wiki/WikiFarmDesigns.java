package com.zenith.client.api.wiki;

/** WikiFarmDesigns — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiFarmDesigns {
    private static final WikiFarmDesigns INSTANCE = new WikiFarmDesigns();
    public static WikiFarmDesigns getInstance() { return INSTANCE; }
    private WikiFarmDesigns() {}
    public void init() { /* filled in later phase */ }
}
