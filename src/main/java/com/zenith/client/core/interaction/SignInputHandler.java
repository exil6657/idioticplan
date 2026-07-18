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

    private long lastSubmitMs = 0;

    /** Called each tick by {@link GUIInteractionEngine}. Types pending text and clicks Done. */
    public void tick() {
        if (pendingText == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof SignEditScreen sign)) return;
        // Debounce - don't spam onDone faster than 200ms
        if (System.currentTimeMillis() - lastSubmitMs < 200) return;
        String text = pendingText;
        pendingText = null;
        try {
            setSignLine(sign, 0, text);
            // Also clear other lines to avoid garbage
            for (int i = 1; i < 4; i++) {
                try { setSignLine(sign, i, ""); } catch (Throwable ignored) {}
            }
            Method onDone = findMethod(sign.getClass(), "onDone", "onClose", "close", "submit", "method_31460");
            if (onDone != null) {
                onDone.setAccessible(true);
                lastSubmitMs = System.currentTimeMillis();
                mc.execute(() -> {
                    try { onDone.invoke(sign); }
                    catch (Throwable t) { ZenithClient.LOGGER.warn("[SignInput] onDone failed", t); }
                });
            } else {
                // Fallback: try to invoke via button click — look for Done button in screen
                try {
                    var buttonField = findField(sign.getClass(), "doneButton", "confirmButton");
                    if (buttonField != null) {
                        buttonField.setAccessible(true);
                        Object btn = buttonField.get(sign);
                        if (btn != null) {
                            var onPressM = btn.getClass().getMethod("onPress");
                            onPressM.setAccessible(true);
                            mc.execute(() -> {
                                try { onPressM.invoke(btn); } catch (Throwable ignored) {}
                            });
                            lastSubmitMs = System.currentTimeMillis();
                            return;
                        }
                    }
                } catch (Throwable ignored) {}
                ZenithClient.LOGGER.warn("[SignInput] could not find onDone method on SignEditScreen — methods: {}", java.util.Arrays.toString(sign.getClass().getDeclaredMethods()));
            }
        } catch (Throwable t) {
            ZenithClient.LOGGER.warn("[SignInput] failed to type '{}'", text, t);
        }
    }

    // ---- Internals ----

    private static Method findMethod(Class<?> c, String... names) {
        for (String name : names) {
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return m;
        }
        if (c.getSuperclass() != null) {
            Method f = findMethod(c.getSuperclass(), names);
            if (f != null) return f;
        }
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
        Field messages = findField(obj.getClass(), "messages", "filteredMessages", "a", "b");
        if (messages != null) {
            messages.setAccessible(true);
            Object arr = messages.get(obj);
            if (arr instanceof Object[] txt) {
                if (line < txt.length) {
                    // Try to create Component via appropriate class (Component vs Text)
                    Object comp = tryCreateComponent(value);
                    txt[line] = comp;
                    return true;
                }
            }
            // Maybe it's a List
            if (arr instanceof java.util.List<?> list) {
                if (line < list.size()) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> l = (java.util.List<Object>) list;
                    l.set(line, tryCreateComponent(value));
                    return true;
                }
            }
        }
        // Try SignText record constructor path: if obj has method getMessages(int) maybe?
        // Look for field that is a record with messages + filteredMessages + color + glowing flag
        // As last resort, look for any Text[] field in the class
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.getType().isArray()) {
                f.setAccessible(true);
                Object arr = f.get(obj);
                if (arr instanceof Object[] txt && txt.length == 4) {
                    // likely the messages array
                    txt[line] = tryCreateComponent(value);
                    return true;
                }
            }
        }
        return false;
    }

    private static Object tryCreateComponent(String value) {
        try { return Component.literal(value); }
        catch (Throwable t) {
            try {
                // 1.20+ Text.literal?
                Class<?> textClz = Class.forName("net.minecraft.network.chat.Component");
                var m = textClz.getMethod("literal", String.class);
                return m.invoke(null, value);
            } catch (Throwable ignored) { return value; }
        }
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
