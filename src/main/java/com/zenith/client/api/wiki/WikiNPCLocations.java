package com.zenith.client.api.wiki;

/** WikiNPCLocations — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiNPCLocations {
    private static final WikiNPCLocations INSTANCE = new WikiNPCLocations();
    public static WikiNPCLocations getInstance() { return INSTANCE; }
    private WikiNPCLocations() {}
    public void init() { /* filled in later phase */ }
}
