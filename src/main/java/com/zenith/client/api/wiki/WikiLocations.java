package com.zenith.client.api.wiki;

/** WikiLocations — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiLocations {
    private static final WikiLocations INSTANCE = new WikiLocations();
    public static WikiLocations getInstance() { return INSTANCE; }
    private WikiLocations() {}
    public void init() { /* filled in later phase */ }
}
