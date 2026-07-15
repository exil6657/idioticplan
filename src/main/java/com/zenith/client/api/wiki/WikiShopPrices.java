package com.zenith.client.api.wiki;

/** WikiShopPrices — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiShopPrices {
    private static final WikiShopPrices INSTANCE = new WikiShopPrices();
    public static WikiShopPrices getInstance() { return INSTANCE; }
    private WikiShopPrices() {}
    public void init() { /* filled in later phase */ }
}
