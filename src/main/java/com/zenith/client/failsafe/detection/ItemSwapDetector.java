package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Detects when the player's held item changes while a macro is running and no
 * macro module initiated the swap. The macro is expected to call
 * {@link #expectSwap(long)} before each legitimate item change, passing the
 * expected new item's identity hash or a short-lived window (1.5 s) during
 * which swaps are permitted.</p>
 */
public class ItemSwapDetector extends AbstractDetector {

    private static long expectSwapUntil = 0;
    private static String expectedHint = "";

    private ItemStack lastHeld = ItemStack.EMPTY;
    private long lastChangeMs;

    /** Call from macro code just before an intentional swap. */
    public static void expectSwap(long windowMs) {
        expectSwapUntil = System.currentTimeMillis() + windowMs;
    }

    /** Call from macro code with an expected display name/substring for logging. */
    public static void expectSwap(String expectedName) {
        expectSwapUntil = System.currentTimeMillis() + 1500L;
        expectedHint = expectedName == null ? "" : expectedName;
    }

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { clear(FailsafeType.ITEM_DESELECT); lastHeld = ItemStack.EMPTY; return; }
        ItemStack held = p.getMainHandItem();
        boolean changed = !ItemStack.matches(lastHeld, held);
        if (changed && !ItemStack.matches(lastHeld, ItemStack.EMPTY)) {
            if (nowMs <= expectSwapUntil) {
                // Expected swap — reset window.
                expectSwapUntil = 0;
            } else {
                lastChangeMs = nowMs;
                String oldName = lastHeld.getHoverName().getString();
                String newName = held.getHoverName().getString();
                trigger(FailsafeType.ITEM_DESELECT, oldName + " → " + newName);
            }
        }
        lastHeld = held.copy();
        if ((nowMs - lastChangeMs) > 2000) clear(FailsafeType.ITEM_DESELECT);
    }
}
