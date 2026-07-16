package com.zenith.client.core.player;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.ChatReceivedEvent;
import com.zenith.client.world.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks health/food with Hypixel SkyBlock custom HP support.
 * <p>
 * In SkyBlock, vanilla player.getHealth() is always 20. Real HP is displayed
 * on the action bar, e.g. "1,234/2,345❤   +123♣   450❈ Defense" or
 * "1,234❤  123/123❈  456✎ Mana". We parse that via ActionBarScraper →
 * ChatReceivedEvent typeId=2 (or SystemChat if server uses that for action bar).
 *
 * <p>Falls back to vanilla health when no action bar data for 5 s.
 */
public final class PlayerHealthMonitor {
    private static final PlayerHealthMonitor INSTANCE = new PlayerHealthMonitor();
    public static PlayerHealthMonitor getInstance() { return INSTANCE; }

    private float lastHealth = 20f;
    private int lastFood = 20;
    private long lastDamageAt;
    private long lastActionBarMs = 0L;

    // Action bar cache
    private float sbHealth = -1f;
    private float sbMaxHealth = -1f;
    private float sbAbsorption = 0f;
    private float sbDefense = 0f;
    private float sbMana = -1f;
    private float sbMaxMana = -1f;

    // Regexes for action bar parsing — [RESEARCH NEEDED] exact symbols vary per SB update,
    // but core pattern is "<cur>/<max>❤" or "<cur>❤".
    private static final Pattern HP_BOTH = Pattern.compile("(\\d[\\d,]*)/(\\d[\\d,]*)\\s*❤");
    private static final Pattern HP_SINGLE = Pattern.compile("(\\d[\\d,]*)\\s*❤");
    private static final Pattern DEFENSE = Pattern.compile("(\\d[\\d,]*)\\s*❈\\s*Defense");
    private static final Pattern MANA = Pattern.compile("(\\d[\\d,]*)/(\\d[\\d,]*)\\s*✎\\s*Mana");

    private PlayerHealthMonitor() {
        // Register on bus for action-bar chat events
        try {
            com.zenith.client.core.event.ZenithEventBus.getInstance().register(this);
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onChat(ChatReceivedEvent ev) {
        if (ev == null) return;
        // type 2 = action bar in old system; but some servers send as type 0/1 too,
        // so parse any message containing ❤
        String msg = ev.getMessage();
        if (msg == null || !msg.contains("❤")) return;
        parseActionBar(msg);
    }

    public void parseActionBar(String plain) {
        if (plain == null) return;
        lastActionBarMs = System.currentTimeMillis();
        try {
            Matcher mBoth = HP_BOTH.matcher(plain);
            if (mBoth.find()) {
                float cur = parseNum(mBoth.group(1));
                float max = parseNum(mBoth.group(2));
                sbHealth = cur;
                sbMaxHealth = max;
                sbAbsorption = 0f; // will be overwritten if absorption present
            } else {
                Matcher mSingle = HP_SINGLE.matcher(plain);
                if (mSingle.find()) {
                    float cur = parseNum(mSingle.group(1));
                    sbHealth = cur;
                    // don't overwrite max if we already have it
                }
            }
            Matcher mDef = DEFENSE.matcher(plain);
            if (mDef.find()) sbDefense = parseNum(mDef.group(1));

            Matcher mMana = MANA.matcher(plain);
            if (mMana.find()) {
                sbMana = parseNum(mMana.group(1));
                sbMaxMana = parseNum(mMana.group(2));
            }
        } catch (Throwable ignored) {}
    }

    public void tick() {
        float vanillaH = World.get().playerHealth(); // fallback
        int f = World.get().playerFood();
        float effectiveH;

        long now = System.currentTimeMillis();
        boolean hasSB = sbHealth >= 0 && (now - lastActionBarMs) < 5000;
        if (hasSB) {
            effectiveH = sbHealth;
        } else {
            effectiveH = vanillaH;
        }

        if (effectiveH < lastHealth - 0.5f) lastDamageAt = now;
        lastHealth = effectiveH;
        lastFood = f;
    }

    public float health() { return lastHealth; }
    public float maxHealth() { return sbMaxHealth > 0 ? sbMaxHealth : 20f; }
    public float absorption() { return sbAbsorption; }
    public float defense() { return sbDefense; }
    public float mana() { return sbMana; }
    public float maxMana() { return sbMaxMana > 0 ? sbMaxMana : 100f; }
    public int food() { return lastFood; }
    public float healthPct() {
        float max = maxHealth();
        if (max <= 0) return 1f;
        return lastHealth / max;
    }
    public boolean recentlyDamaged(long windowMs) { return System.currentTimeMillis() - lastDamageAt < windowMs; }
    public boolean isUsingSkyblockHealth() { return (System.currentTimeMillis() - lastActionBarMs) < 5000 && sbHealth >= 0; }

    public void resetSkyblockCache() { sbHealth = -1; sbMaxHealth = -1; sbMana = -1; sbMaxMana = -1; lastActionBarMs = 0; }

    private static float parseNum(String s) {
        if (s == null) return 0;
        return Float.parseFloat(s.replace(",", "").trim());
    }
}
