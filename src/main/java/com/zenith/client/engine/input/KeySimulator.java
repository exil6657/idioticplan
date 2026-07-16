package com.zenith.client.engine.input;

/**
 * Simulates key presses via Minecraft's keybinding system.
 *
 * <p>Writes directly to {@code Minecraft.options.key*}.setDown() — the
 * same path vanilla uses when a physical key is held. This is the only
 * place that touches MC key state (master rule §3, §9); all macro/path
 * code goes through this class.</p>
 *
 * <p>Reflection fallback is used for mapping drift in 26.1 — field names
 * may be keyUp/forward vs keyForward depending on mappings; we try both.</p>
 */
public final class KeySimulator {

    private boolean forward, back, left, right, jump, sneak, sprint, use, attack;

    public void setForward(boolean v)  { forward = v; }
    public void setBack(boolean v)     { back = v; }
    public void setLeft(boolean v)     { left = v; }
    public void setRight(boolean v)    { right = v; }
    public void setJump(boolean v)     { jump = v; }
    public void setSneak(boolean v)    { sneak = v; }
    public void setSprint(boolean v)   { sprint = v; }
    public void setUse(boolean v)      { use = v; }
    public void setAttack(boolean v)   { attack = v; }

    public void halt() { forward=back=left=right=jump=sneak=sprint=use=attack=false; }

    public boolean forward() { return forward; }
    public boolean back()    { return back; }
    public boolean left()    { return left; }
    public boolean right()   { return right; }
    public boolean jump()    { return jump; }
    public boolean sneak()   { return sneak; }
    public boolean sprint()  { return sprint; }
    public boolean use()     { return use; }
    public boolean attack()  { return attack; }

    /**
     * Push accumulated desired state into MC's Options KeyMappings.
     * Must run on the main thread each tick.
     */
    public void flush() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || mc.options == null) return;
            var opts = mc.options;
            // Mojang names in 26.1 unobfuscated: keyUp, keyDown, keyLeft, keyRight,
            // keyJump, keyShift (sneak), keySprint, keyAttack, keyUse.
            // Some versions name sneak as keyShift. Use reflection-tolerant setter.
            setKeyDown(opts, "keyUp", "keyForward", forward);
            setKeyDown(opts, "keyDown", "keyBack", back);
            setKeyDown(opts, "keyLeft", null, left);
            setKeyDown(opts, "keyRight", null, right);
            setKeyDown(opts, "keyJump", null, jump);
            setKeyDown(opts, "keyShift", "keySneak", sneak);
            setKeyDown(opts, "keySprint", null, sprint);
            setKeyDown(opts, "keyAttack", null, attack);
            setKeyDown(opts, "keyUse", null, use);
        } catch (Throwable t) {
            com.zenith.client.ZenithClient.LOGGER.debug("[KeySimulator] flush failed", t);
        }
    }

    private static void setKeyDown(Object options, String primary, String fallback, boolean down) {
        try {
            java.lang.reflect.Field f = null;
            try { f = options.getClass().getField(primary); }
            catch (NoSuchFieldException e) {
                if (fallback != null) {
                    try { f = options.getClass().getField(fallback); }
                    catch (NoSuchFieldException ex) { /* try declared */ }
                }
            }
            if (f == null) {
                try { f = options.getClass().getDeclaredField(primary); }
                catch (NoSuchFieldException e) {
                    if (fallback != null) f = options.getClass().getDeclaredField(fallback);
                }
            }
            if (f == null) return;
            f.setAccessible(true);
            Object km = f.get(options);
            if (km == null) return;
            // KeyMapping#setDown(boolean) in Mojang
            try {
                var m = km.getClass().getMethod("setDown", boolean.class);
                m.invoke(km, down);
            } catch (NoSuchMethodException nsme) {
                // Some versions use set(boolean)
                try {
                    var m2 = km.getClass().getMethod("set", boolean.class);
                    m2.invoke(km, down);
                } catch (Exception ignored) {
                    // final fallback: field 'isDown' or 'down'
                    try {
                        var ff = km.getClass().getField("down");
                        ff.setBoolean(km, down);
                    } catch (Exception ignored2) {
                        var df = km.getClass().getDeclaredField("isDown");
                        df.setAccessible(true);
                        df.setBoolean(km, down);
                    }
                }
            }
        } catch (Throwable ignored) {
            // silent - don't crash tick loop on keybind drift
        }
    }
}
