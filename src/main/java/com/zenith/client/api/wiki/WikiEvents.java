package com.zenith.client.api.wiki;

/** WikiEvents — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiEvents {
    private static final WikiEvents INSTANCE = new WikiEvents();
    public static WikiEvents getInstance() { return INSTANCE; }
    private WikiEvents() {}
    public void init() { /* filled in later phase */ }
}
