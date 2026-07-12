package com.zenith.client.core.interaction;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.InventoryOpenEvent;
import com.zenith.client.core.event.events.InventoryCloseEvent;
import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the currently-open {@link net.minecraft.world.inventory.AbstractContainerMenu}
 * into a {@link GUIState} snapshot, and fires InventoryOpen/Close events when the
 * GUI changes. Uses Mojang names (26.1).
 */
public final class GUIParser {

    private String lastTitle;

    public GUIState read() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.containerMenu == null) return new GUIState();
        var menu = mc.player.containerMenu;
        var screen = mc.screen;
        GUIState s = new GUIState();
        s.title = screen != null && screen.getTitle() != null ? screen.getTitle().getString() : "";
        s.containerId = menu.containerId;
        s.stacks = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            ItemStack is = slot.getItem();
            s.stacks.add(toSnapshot(is));
        }
        s.slots = menu.slots.size();
        // Player inventory begins at first slot whose inventory is the player inventory.
        int pi = 0;
        for (int i = 0; i < menu.slots.size(); i++) {
            if (menu.slots.get(i).container == mc.player.getInventory()) { pi = i; break; }
        }
        s.playerInventoryStart = pi;
        s.rows = Math.max(0, (pi)/9);
        // Collect button labels
        s.buttons = new ArrayList<>();
        if (screen != null) {
            for (var child : screen.children()) {
                if (child instanceof net.minecraft.client.gui.components.Button b) {
                    Component msg = b.getMessage();
                    if (msg != null) s.buttons.add(msg.getString());
                }
            }
        }
        return s;
    }

    /** Called each tick from GUIInteractionEngine to fire open/close events. */
    public void tick() {
        GUIState s = read();
        String now = s.present() ? s.title : null;
        if (now != null && !now.equals(lastTitle)) {
            ZenithEventBus.getInstance().post(new InventoryOpenEvent(now, s.rows > 0 ? s.rows*9 : s.slots));
        } else if (now == null && lastTitle != null) {
            ZenithEventBus.getInstance().post(new InventoryCloseEvent(lastTitle));
        }
        lastTitle = now;
    }

    private static GUIItemStack toSnapshot(ItemStack is) {
        if (is == null || is.isEmpty()) return null;
        String name = "";
        if (is.getHoverName() != null) name = is.getHoverName().getString();
        List<String> lore = new ArrayList<>();
        try {
            var tag = is.getTag();
            if (tag != null && tag.contains("display", 10)) {
                var display = tag.getCompound("display");
                if (display.contains("Lore", 9)) {
                    var loreList = display.getList("Lore", 8);
                    for (int i = 0; i < loreList.size(); i++) {
                        String raw = loreList.getString(i);
                        lore.add(stripFormatting(raw));
                    }
                }
            }
        } catch (Exception e) { ZenithClient.LOGGER.debug("[GUIParser] lore read failed", e); }
        String sbId = "";
        try {
            var tag = is.getTag();
            if (tag != null && tag.contains("ExtraAttributes", 10)) {
                var ea = tag.getCompound("ExtraAttributes");
                if (ea.contains("id")) sbId = ea.getString("id");
            }
        } catch (Exception ignored) {}
        boolean ench = !is.getEnchantments().isEmpty();
        boolean glint = is.hasFoil();
        return new GUIItemStack(name, lore, is.getItem().toString(), sbId, is.getCount(), ench, glint);
    }

    private static String stripFormatting(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }
}
