package com.zenith.client.api.wiki;

/** WikiRecipes — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiRecipes {
    private static final WikiRecipes INSTANCE = new WikiRecipes();
    public static WikiRecipes getInstance() { return INSTANCE; }
    private WikiRecipes() {}
    public void init() { /* filled in later phase */ }
}
