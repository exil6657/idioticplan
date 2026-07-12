package com.zenith.client.flipping;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/** Per-session configurable limits for the flipper (wired to the HUD/dashboard later). */
public class FlipperConfig {

    @Expose public boolean enabled = true;
    @Expose public boolean ahBinEnabled = true;
    @Expose public boolean bazaarEnabled = true;
    @Expose public boolean craftFlipsEnabled = false;
    @Expose public boolean npcFlipsEnabled = false;
    @Expose public long minProfitCoins = 15_000L;
    @Expose public double minProfitPercent = 0.08d;
    @Expose public long maxBudget() { return maxCoinsPerFlip; }
    @Expose public long maxCoinsPerFlip = 5_000_000L;
    @Expose public int candidatesPerTick = 2;
    @Expose public boolean autoBuy = true;
    @Expose public boolean autoList = true;
    @Expose public boolean automaticBreaks = true;
    @Expose public boolean chatAlerts = true;
    @Expose public List<String> hotItems = new ArrayList<>(defaultHotItems());

    private static List<String> defaultHotItems() {
        return List.of(
                "ASPECT_OF_THE_END","HYPERION","JUJU_SHORTBOW","TERMINATOR","NECRON_BLADE",
                "SHADOW_FURY","LIVID_DAGGER","FLOWER_OF_TRUTH","SPIRIT_BOW","SPIRIT_SCEPTRE",
                "YETI_SWORD","MIDAS_STAFF","WITHER_CHESTPLATE","NECRON_CHESTPLATE","FROZEN_SCYTHE",
                "RECOMBOBULATOR_3000","HOT_POTATO_BOOK","FUMING_POTATO_BOOK","ART_OF_WAR","ART_OF_PEACE",
                "TALISMAN_OF_COINS","FARMING_FOR_DUMMIES","ENCHANTED_DIAMOND","ENCHANTED_EMERALD",
                "ENCHANTED_GOLD","ENCHANTED_COAL","ENCHANTED_IRON","ENCHANTED_LAPIS_LAZULI",
                "ENCHANTED_REDSTONE","ENCHANTED_ENDER_PEARL"
        );
    }
}
