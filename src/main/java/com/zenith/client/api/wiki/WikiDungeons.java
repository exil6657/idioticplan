package com.zenith.client.api.wiki;

/** WikiDungeons — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiDungeons {
    private static final WikiDungeons INSTANCE = new WikiDungeons();
    public static WikiDungeons getInstance() { return INSTANCE; }
    private WikiDungeons() {}
    public void init() { /* filled in later phase */ }
}
