package com.zenith.client.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Small reflection helpers for accessing Minecraft fields where mixins aren't appropriate. */
public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static Field getField(Class<?> cls, String... names) {
        for (String n : names) {
            try {
                Field f = cls.getDeclaredField(n);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    public static Object getValue(Object instance, Field field) {
        if (field == null) return null;
        try { return field.get(instance); }
        catch (IllegalAccessException e) { return null; }
    }

    public static boolean setValue(Object instance, Field field, Object value) {
        if (field == null) return false;
        try { field.set(instance, value); return true; }
        catch (IllegalAccessException e) { return false; }
    }

    public static Method getMethod(Class<?> cls, String name, Class<?>... params) {
        try {
            Method m = cls.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
