package com.zenith.client.api.wiki;

/** WikiCollections — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiCollections {
    private static final WikiCollections INSTANCE = new WikiCollections();
    public static WikiCollections getInstance() { return INSTANCE; }
    private WikiCollections() {}
    public void init() { /* filled in later phase */ }
}
