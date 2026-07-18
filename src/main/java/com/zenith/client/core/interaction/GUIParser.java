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

    private static final GUIParser INSTANCE = new GUIParser();
    public static GUIParser getInstance() { return INSTANCE; }

    private String lastTitle;

    private GUIParser() {}

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
        try { if (is.getHoverName() != null) name = is.getHoverName().getString(); } catch (Throwable ignored) {}
        List<String> lore = new ArrayList<>();
        String sbId = "";
        boolean ench = false;
        boolean glint = false;
        try { glint = is.hasFoil(); } catch (Throwable ignored) {}
        try { ench = !is.getEnchantments().isEmpty(); } catch (Throwable ignored) {}

        // ---- Modern 26.1 path: DataComponents API ----
        try {
            // Try to read lore via DataComponents.LORE
            // reflections avoid compile-time dependency drift
            Class<?> dcClass = Class.forName("net.minecraft.core.component.DataComponents");
            Object loreCompType = dcClass.getField("LORE").get(null);
            var getMethod = is.getClass().getMethod("get", Class.forName("net.minecraft.core.component.DataComponentType"));
            Object loreComp = getMethod.invoke(is, loreCompType);
            if (loreComp != null) {
                var linesM = loreComp.getClass().getMethod("lines");
                Object lines = linesM.invoke(loreComp);
                if (lines instanceof List<?> lst) {
                    for (Object lineObj : lst) {
                        if (lineObj instanceof Component c) lore.add(stripFormatting(c.getString()));
                        else lore.add(stripFormatting(lineObj.toString()));
                    }
                }
            }
        } catch (Throwable ignored) {
            // fallback to legacy display tag
            try {
                var getTagM = is.getClass().getMethod("getTag");
                Object tag = getTagM.invoke(is);
                if (tag != null) {
                    var containsM = tag.getClass().getMethod("contains", String.class, int.class);
                    var getCompoundM = tag.getClass().getMethod("getCompound", String.class);
                    if ((boolean)containsM.invoke(tag, "display", 10)) {
                        Object display = getCompoundM.invoke(tag, "display");
                        var containsM2 = display.getClass().getMethod("contains", String.class, int.class);
                        if ((boolean)containsM2.invoke(display, "Lore", 9)) {
                            var getListM = display.getClass().getMethod("getList", String.class, int.class);
                            Object loreList = getListM.invoke(display, "Lore", 8);
                            var sizeM = loreList.getClass().getMethod("size");
                            var getStringM = loreList.getClass().getMethod("getString", int.class);
                            int sz = (int)sizeM.invoke(loreList);
                            for (int i=0;i<sz;i++) lore.add(stripFormatting((String)getStringM.invoke(loreList, i)));
                        }
                    }
                }
            } catch (Throwable e) { ZenithClient.LOGGER.debug("[GUIParser] lore read failed", e); }
        }

        // ---- ExtraAttributes.id ----
        try {
            Class<?> dcClass = Class.forName("net.minecraft.core.component.DataComponents");
            Object customDataType = dcClass.getField("CUSTOM_DATA").get(null);
            var getMethod = is.getClass().getMethod("get", Class.forName("net.minecraft.core.component.DataComponentType"));
            Object customData = getMethod.invoke(is, customDataType);
            if (customData != null) {
                // CustomData → copyTag() → CompoundTag
                var copyTagM = customData.getClass().getMethod("copyTag");
                Object tag = copyTagM.invoke(customData);
                if (tag != null) {
                    var containsEa = tag.getClass().getMethod("contains", String.class);
                    if ((boolean)containsEa.invoke(tag, "ExtraAttributes") || (boolean)tag.getClass().getMethod("contains", String.class, int.class).invoke(tag, "ExtraAttributes", 10)) {
                        var getCompoundM = tag.getClass().getMethod("getCompound", String.class);
                        Object ea = getCompoundM.invoke(tag, "ExtraAttributes");
                        var containsId = ea.getClass().getMethod("contains", String.class);
                        if ((boolean)containsId.invoke(ea, "id")) {
                            var getStringM = ea.getClass().getMethod("getString", String.class);
                            sbId = (String)getStringM.invoke(ea, "id");
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            try {
                var getTagM = is.getClass().getMethod("getTag");
                Object tag = getTagM.invoke(is);
                if (tag != null) {
                    var containsM = tag.getClass().getMethod("contains", String.class, int.class);
                    if ((boolean)containsM.invoke(tag, "ExtraAttributes", 10)) {
                        var getCompoundM = tag.getClass().getMethod("getCompound", String.class);
                        Object ea = getCompoundM.invoke(tag, "ExtraAttributes");
                        var containsM2 = ea.getClass().getMethod("contains", String.class);
                        if ((boolean)containsM2.invoke(ea, "id")) {
                            var getStringM = ea.getClass().getMethod("getString", String.class);
                            sbId = (String)getStringM.invoke(ea, "id");
                        }
                    }
                }
            } catch (Throwable ignored2) {}
        }

        String itemId = "";
        try { itemId = is.getItem().toString(); } catch (Throwable ignored) {}
        return new GUIItemStack(name, lore, itemId, sbId, is.getCount(), ench, glint);
    }

    private static String stripFormatting(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }
}
