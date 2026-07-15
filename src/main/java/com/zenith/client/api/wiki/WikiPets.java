package com.zenith.client.api.wiki;

/** WikiPets — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiPets {
    private static final WikiPets INSTANCE = new WikiPets();
    public static WikiPets getInstance() { return INSTANCE; }
    private WikiPets() {}
    public void init() { /* filled in later phase */ }
}
