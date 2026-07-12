package com.zenith.client.failsafe.detection;

import com.zenith.client.failsafe.FailsafeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Triggers a NOTIFY (and escalates to PAUSE when configured) when the player's
 * inventory is completely full so the macro cannot collect more drops.
 *
 * <p>Checks the 36 main slots (excluding the 4 armor slots and the offhand).
 * Counts only non-{@link ItemStack#isEmpty} slots.</p>
 */
public class InventoryFullDetector extends AbstractDetector {

    private long lastTriggerMs;

    @Override
    public void tick(long nowMs) {
        Player p = mc().player;
        if (p == null) { clear(FailsafeType.INVENTORY_FULL); return; }
        Inventory inv = p.getInventory();
        int filled = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) filled++;
        }
        if (filled >= 36) {
            if (nowMs - lastTriggerMs > 5000) {
                lastTriggerMs = nowMs;
                trigger(FailsafeType.INVENTORY_FULL, "36/36 slots full");
            }
        } else {
            clear(FailsafeType.INVENTORY_FULL);
        }
    }
}
