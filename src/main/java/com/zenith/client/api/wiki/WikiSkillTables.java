package com.zenith.client.api.wiki;

/** WikiSkillTables — placeholder wiki data table. Populated by later macro phases from NEU constants. */
public final class WikiSkillTables {
    private static final WikiSkillTables INSTANCE = new WikiSkillTables();
    public static WikiSkillTables getInstance() { return INSTANCE; }
    private WikiSkillTables() {}
    public void init() { /* filled in later phase */ }
}
