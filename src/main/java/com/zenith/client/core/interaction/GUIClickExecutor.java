package com.zenith.client.core.interaction;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.protection.BitsSpendBlocker;
import com.zenith.client.core.timer.DelayManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;

/**
 * Executes clicks on the currently-open container. All clicks route through
 * here so we can apply:
 * <ul>
 *   <li>Bits-spend blocking (BitsSpendBlocker checks item lore for bit cost)</li>
 *   <li>Humanized inter-click delays (DelayManager)</li>
 *   <li>Event cancellation (Failsafe/anti-detection)</li>
 * </ul>
 */
public final class GUIClickExecutor {

    public void clickSlot(int slotId, int mouseButton, ClickType action) {
        if (slotId < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.containerMenu == null) return;
        if (!DelayManager.getInstance().isReady("gui_click")) return;
        DelayManager.getInstance().resetHumanised("gui_click", 60, 140);

        // Bits protection: read the item and block if it's a bits-cost item.
        var menu = mc.player.containerMenu;
        if (slotId < menu.slots.size()) {
            var stack = menu.slots.get(slotId).getItem();
            if (costsBits(stack)) {
                boolean shift = (action == ClickType.QUICK_MOVE);
                boolean feature = isBitsFeature(stack);
                long cost = estimateCost(stack);
                if (BitsSpendBlocker.preClick(feature == null ? "unknown_bits_item" : feature, cost, false)) {
                    return; // blocked
                }
            }
        }

        try {
            mc.gameMode.handleInventoryMouseClick(menu.containerId, slotId, mouseButton, action, mc.player);
        } catch (Throwable t) {
            ZenithClient.LOGGER.error("[GUIClickExecutor] click failed slot={} action={}", slotId, action, t);
        }
    }

    public void leftClick(int slot) { clickSlot(slot, 0, ClickType.PICKUP); }
    public void rightClick(int slot) { clickSlot(slot, 1, ClickType.PICKUP); }
    public void shiftClick(int slot) { clickSlot(slot, 0, ClickType.QUICK_MOVE); }
    public void dropOne() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.containerMenu == null) return;
        int slot = mc.player.inventory.selected;
        clickSlot(slot, 0, ClickType.THROW);
    }

    private boolean costsBits(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Simple heuristic: display name/lore contains "Bits" with a number in front.
        var tag = stack.getTag();
        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            var ea = tag.getCompound("ExtraAttributes");
            if (ea.contains("bits")) return true;
        }
        if (stack.getHoverName() != null && stack.getHoverName().getString().contains("Bits")) return true;
        return false;
    }
    private long estimateCost(net.minecraft.world.item.ItemStack stack) { return 0; /* Phase 7 improves */ }
    private String isBitsFeature(net.minecraft.world.item.ItemStack stack) { return null; }
}
