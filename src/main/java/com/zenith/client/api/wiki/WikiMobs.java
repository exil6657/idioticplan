package com.zenith.client.api.wiki;

/** WikiMobs — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiMobs {
    private static final WikiMobs INSTANCE = new WikiMobs();
    public static WikiMobs getInstance() { return INSTANCE; }
    private WikiMobs() {}
    public void init() { /* filled in later phase */ }
}
