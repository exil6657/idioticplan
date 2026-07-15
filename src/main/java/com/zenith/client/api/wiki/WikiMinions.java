package com.zenith.client.api.wiki;

/** WikiMinions — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiMinions {
    private static final WikiMinions INSTANCE = new WikiMinions();
    public static WikiMinions getInstance() { return INSTANCE; }
    private WikiMinions() {}
    public void init() { /* filled in later phase */ }
}
