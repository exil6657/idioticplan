package com.zenith.client.core.util;

/**
 * Inventory utilities.
 *
 * <p>Flesh implementations added in Phase 6 once the world-interaction stack is
 * available (no hardcoded slots — all slots discovered by item id / name /
 * inventory layout).</p>
 */
public final class InventoryUtils {

    private InventoryUtils() {}

    /** Slot bounds in the player's main inventory (excludes hotbar quick-bar 0..8). */
    public static final int MAIN_INV_FIRST = 9;
    public static final int MAIN_INV_LAST  = 35;
    public static final int HOTBAR_FIRST   = 0;
    public static final int HOTBAR_LAST    = 8;
    public static final int HELMET_SLOT    = 5;   // offhand=45 etc. — these are screen-slot ids.
    public static final int OFFHAND_SLOT   = 45;

    /** @return true if the slot index is within the hotbar (0..8 in screen-slot numbering). */
    public static boolean isHotbar(int slot) {
        return slot >= HOTBAR_FIRST && slot <= HOTBAR_LAST;
    }

    public static boolean isMainStorage(int slot) {
        return slot >= MAIN_INV_FIRST && slot <= MAIN_INV_LAST;
    }
}
