package com.zenith.client.core.interaction;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Detects and types into sign-editor screens (used by Hypixel for AH search,
 * BIN price entry, Bazaar quantity/price, etc.). We inject text directly into
 * the sign's text field via reflection so that it appears on line 1, then
 * programmatically click Done, which sends the {@code ServerboundSignUpdatePacket}
 * and closes the screen.
 *
 * <p>We deliberately avoid calling {@code KeyboardHandler.keyPressed} directly —
 * the mixin on that method fires KeyInputEvent and routes Zenith keybinds,
 * which would double-fire if we synthesised keys through it.</p>
 *
 * <p><b>Research note (R-sign):</b> in 1.20+ the sign stores two {@code SignText}
 * objects (front/back) each holding a {@code Text[] messages} array, rather than
 * a single {@code List<Component> signText}. Our reflection walks the hierarchy
 * to find a writable message source for both the pre-1.20 {@code String[]}/
 * {@code List<Component>} forms and the 1.20+ {@code SignText} form.</p>
 */
public final class SignInputHandler {

    private static final SignInputHandler INSTANCE = new SignInputHandler();
    public static SignInputHandler getInstance() { return INSTANCE; }

    private String pendingText = null;

    private SignInputHandler() {}

    public boolean isInSignScreen() {
        return Minecraft.getInstance().screen instanceof SignEditScreen;
    }

    /** Queue text to be typed into the sign the next tick we're on one. */
    public void requestType(String text) { this.pendingText = text; }

    /** Called each tick by {@link GUIInteractionEngine}. Types pending text and clicks Done. */
    public void tick() {
        if (pendingText == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof SignEditScreen sign)) return;
        String text = pendingText;
        pendingText = null;
        try {
            setSignLine(sign, 0, text);
            Method onDone = findMethod(sign.getClass(), "onDone");
            if (onDone != null) {
                onDone.setAccessible(true);
                mc.execute(() -> {
                    try { onDone.invoke(sign); }
                    catch (Throwable t) { ZenithClient.LOGGER.warn("[SignInput] onDone failed", t); }
                });
            } else {
                ZenithClient.LOGGER.warn("[SignInput] could not find onDone method on SignEditScreen");
            }
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[SignInput] failed to type '{}'", text, t);
        }
    }

    // ---- Internals ----

    private static Method findMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return m;
        if (c.getSuperclass() != null) return findMethod(c.getSuperclass(), name);
        return null;
    }

    private static Field findField(Class<?> c, String... names) {
        for (String n : names) {
            try { return c.getDeclaredField(n); } catch (NoSuchFieldException ignored) {}
        }
        if (c.getSuperclass() != null) {
            Field f = findField(c.getSuperclass(), names);
            if (f != null) return f;
        }
        return null;
    }

    /**
     * Walks all possible sign-text field forms to set line {@code line} to
     * {@code value} across Minecraft versions (1.8 through 1.21 / 26.1).
     */
    private static void setSignLine(SignEditScreen sign, int line, String value) throws Exception {
        // Try modern 1.20+ "front"/"back" SignText fields first (Mojang names).
        for (String fname : new String[] { "signText", "frontText", "backText", "message" }) {
            Field f = findField(sign.getClass(), fname);
            if (f == null) continue;
            f.setAccessible(true);
            Object obj = f.get(sign);
            if (trySetLineOn(obj, line, value)) return;
        }
        // Try direct array/list forms (older MC).
        Field f = findField(sign.getClass(), "signText");
        if (f != null) {
            f.setAccessible(true);
            Object target = f.get(sign);
            if (setDirectLine(target, line, value)) return;
        }
        throw new IllegalStateException("Could not find a writable sign-text field on SignEditScreen");
    }

    /** Try to set a line on an opaque sign-text object (SignText wrapper or raw list/array). */
    private static boolean trySetLineOn(Object obj, int line, String value) throws Exception {
        if (obj == null) return false;
        // Direct list/array?
        if (setDirectLine(obj, line, value)) return true;
        // It might be a SignText with a `messages` (Text[]) field (1.20+).
        Field messages = findField(obj.getClass(), "messages", "filteredMessages");
        if (messages != null) {
            messages.setAccessible(true);
            Object arr = messages.get(obj);
            if (arr instanceof Object[] txt) {
                if (line < txt.length) {
                    txt[line] = Component.literal(value);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean setDirectLine(Object target, int line, String value) {
        if (target instanceof java.util.List<?> list) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<Object> l = (java.util.List<Object>) list;
                if (line < l.size()) {
                    // Accept Component, String, or anything — best effort.
                    l.set(line, Component.literal(value));
                    return true;
                }
                // If list is shorter, append until size (defensive).
                while (l.size() <= line) l.add(Component.literal(""));
                l.set(line, Component.literal(value));
                return true;
            } catch (UnsupportedOperationException e) {
                return false; // immutable
            }
        }
        if (target instanceof String[] arr) { if (line < arr.length) { arr[line] = value; return true; } }
        if (target instanceof Component[] arr) { if (line < arr.length) { arr[line] = Component.literal(value); return true; } }
        return false;
    }
}
