package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Detects when the tool/enchantment on the held item does not match what the
 * macro is expecting.
 *
 * <p>Macros register an "expected held item" string (SkyBlock ID or display-name
 * substring) via {@link #setExpected(String, long)}. While an expectation is
 * active, if the held item does not contain the expected substring we fire
 * {@link FailsafeType#WRONG_ITEM}.</p>
 */
public class WrongItemDetector extends AbstractDetector {

    private static String expected = null;
    private static long expectedUntil = 0;

    private long lastMismatchMs;

    /** Set the expected item for the current macro step; pass {@code null} to clear. */
    public static void setExpected(String nameSubstring, long holdForMs) {
        expected = nameSubstring;
        expectedUntil = nameSubstring == null ? 0 : System.currentTimeMillis() + holdForMs;
    }

    public static void clearExpected() { expected = null; expectedUntil = 0; }

    @Override
    public void tick(long nowMs) {
        var p = mc().player;
        if (p == null) { clear(FailsafeType.WRONG_ITEM); return; }
        if (expected == null || nowMs > expectedUntil) {
            clear(FailsafeType.WRONG_ITEM);
            return;
        }
        ItemStack held = p.getMainHandItem();
        String name = held.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
        if (!name.contains(expected.toLowerCase(java.util.Locale.ROOT))) {
            lastMismatchMs = nowMs;
            trigger(FailsafeType.WRONG_ITEM, "expected '" + expected + "' got '" + name + "'");
        } else {
            if (nowMs - lastMismatchMs > 1000) clear(FailsafeType.WRONG_ITEM);
        }
    }
}
