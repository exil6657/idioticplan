package com.zenith.client.core.util;

import java.awt.Color;

/** Colour helpers for HUD, ESP, and theme rendering. */
public final class ColorUtils {

    private ColorUtils() {}

    public static int toARGB(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int alpha(int argb) { return (argb >> 24) & 0xFF; }
    public static int red(int argb)   { return (argb >> 16) & 0xFF; }
    public static int green(int argb) { return (argb >>  8) & 0xFF; }
    public static int blue(int argb)  { return argb & 0xFF; }

    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Parse a hex colour string such as "#RRGGBB" or "#RRGGBBAA". */
    public static int parseHex(String hex) {
        if (hex == null) return 0xFFFFFFFF;
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            if (h.length() == 6) {
                return 0xFF000000 | Integer.parseInt(h, 16);
            } else if (h.length() == 8) {
                // AARRGGBB order
                return (int) Long.parseLong(h, 16);
            }
        } catch (NumberFormatException ignored) {}
        return 0xFFFFFFFF;
    }

    public static String toHex(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }

    public static int interpolate(int aARGB, int bARGB, float t) {
        t = MathUtils.clampf(t, 0f, 1f);
        int aA = alpha(aARGB), rA = red(aARGB), gA = green(aARGB), bA = blue(aARGB);
        int aB = alpha(bARGB), rB = red(bARGB), gB = green(bARGB), bB = blue(bARGB);
        return toARGB(
            (int)(rA + (rB - rA) * t),
            (int)(gA + (gB - gA) * t),
            (int)(bA + (bB - bA) * t),
            (int)(aA + (aB - aA) * t)
        );
    }

    public static int fromAwt(Color c) {
        return toARGB(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }
}
