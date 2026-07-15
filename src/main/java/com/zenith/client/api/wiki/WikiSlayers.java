package com.zenith.client.api.wiki;

/** WikiSlayers — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiSlayers {
    private static final WikiSlayers INSTANCE = new WikiSlayers();
    public static WikiSlayers getInstance() { return INSTANCE; }
    private WikiSlayers() {}
    public void init() { /* filled in later phase */ }
}
