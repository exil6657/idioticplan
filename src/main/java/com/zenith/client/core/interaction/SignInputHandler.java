package com.zenith.client.core.interaction;

import com.zenith.client.ZenithClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Detects and types into sign-editor screens (used by Hypixel for AH search,
 * BIN price entry, Bazaar quantity entry, etc.). We inject text directly into
 * the sign's {@code signText} field via reflection so that it appears on line 1,
 * then programmatically click the Done button, which sends the
 * {@code ServerboundSignUpdatePacket} and closes the screen.
 *
 * <p>We deliberately avoid calling {@code KeyboardHandler.keyPressed} directly —
 * the mixin on that method fires KeyInputEvent and routes Zenith keybinds, which
 * would double-fire if we synthesised keys through it.</p>
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
    public void requestType(String text) {
        this.pendingText = text;
    }

    /** Called each tick by {@link GUIInteractionEngine}. Types pending text and clicks Done. */
    public void tick() {
        if (pendingText == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof SignEditScreen sign)) return;
        String text = pendingText;
        pendingText = null;
        try {
            setSignLine(sign, 0, text);
            // In 26.1, pressing Enter in a SignEditScreen triggers onDone() which sends the packet
            // and closes the screen. Call it via reflection for signature safety across 26.x.
            Method onDone = findMethod(sign.getClass(), "onDone");
            if (onDone != null) {
                onDone.setAccessible(true);
                // Schedule one tick later so the sign-text setter applies before the packet goes out.
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
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return m;
        }
        // Walk superclass (e.g. AbstractSignEditScreen in newer versions).
        if (c.getSuperclass() != null) return findMethod(c.getSuperclass(), name);
        return null;
    }

    private static void setSignLine(SignEditScreen sign, int line, String value) throws Exception {
        Field f = findField(sign.getClass(), "signText");
        if (f == null) throw new IllegalStateException("signText field not found on SignEditScreen");
        f.setAccessible(true);
        Object target = f.get(sign);
        if (target instanceof java.util.List<?> list) {
            @SuppressWarnings("unchecked")
            java.util.List<Component> lines = (java.util.List<Component>) list;
            lines.set(line, Component.literal(value));
            return;
        }
        if (target instanceof String[] arr) { arr[line] = value; return; }
        if (target instanceof Component[] arr) { arr[line] = Component.literal(value); return; }
        throw new IllegalStateException("Unrecognised sign-text field type: " + target.getClass());
    }

    private static Field findField(Class<?> c, String name) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return f;
        if (c.getSuperclass() != null) return findField(c.getSuperclass(), name);
        return null;
    }
}
