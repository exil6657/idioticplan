package com.zenith.client.core.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/** Text/string utilities — mostly chat formatting and number shortening. */
public final class TextUtils {

    /** Regex to strip MC § colour codes. */
    private static final Pattern MC_COLOUR = Pattern.compile("§[0-9a-fk-orx]");
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.UK);

    private TextUtils() {}

    /** Strip all Minecraft-style § formatting codes from a string. */
    public static String stripColours(String s) {
        if (s == null) return "";
        return MC_COLOUR.matcher(s).replaceAll("");
    }

    /** Human-friendly coin formatting: 1.23M, 45.6k, 789. */
    public static String formatCoins(long coins) {
        double abs = Math.abs((double) coins);
        String sign = coins < 0 ? "-" : "";
        if (abs >= 1_000_000_000d) return sign + String.format("%.2fB", abs / 1_000_000_000d);
        if (abs >= 1_000_000d)     return sign + String.format("%.2fM", abs / 1_000_000d);
        if (abs >= 1_000d)         return sign + String.format("%.1fk", abs / 1_000d);
        return sign + NF.format(coins);
    }

    public static String formatNumber(long n) { return NF.format(n); }

    public static String formatDuration(long ms) {
        if (ms < 0) ms = 0;
        long s = ms / 1000;
        long h = s / 3600; s %= 3600;
        long m = s / 60;   s %= 60;
        if (h > 0) return String.format("%dh%02dm%02ds", h, m, s);
        if (m > 0) return String.format("%dm%02ds", m, s);
        return String.format("%ds", s);
    }

    /** Normalise a player name: trim, lowercase for map-key use. */
    public static String normaliseName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase(Locale.ROOT);
    }

    /** Safe equals that tolerates nulls. */
    public static boolean safeEq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && needle != null &&
               haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}
