package com.zenith.client.core.util;

/** Input validation helpers for commands and config values. */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isNonNull(Object... values) {
        for (Object v : values) if (v == null) return false;
        return true;
    }

    public static boolean isInRange(double v, double min, double max) {
        return v >= min && v <= max;
    }

    public static boolean isValidUsername(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{1,16}");
    }

    public static boolean isPositive(double v) { return v > 0; }

    public static boolean isNonNegative(double v) { return v >= 0; }
}
