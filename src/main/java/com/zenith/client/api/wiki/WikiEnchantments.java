package com.zenith.client.api.wiki;

/** WikiEnchantments — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiEnchantments {
    private static final WikiEnchantments INSTANCE = new WikiEnchantments();
    public static WikiEnchantments getInstance() { return INSTANCE; }
    private WikiEnchantments() {}
    public void init() { /* filled in later phase */ }
}
