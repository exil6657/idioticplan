package com.zenith.client.core.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Matches items in open GUI containers by display name, lore, item id, stack size, etc.
 *
 * <p><b>Master rule §1:</b> never hard-code slot indices. All GUI interactions find
 * their target slot by using one of these matchers.</p>
 */
public final class GUIItemMatcher {

    public String displayNameContains;
    public String displayNameEquals;
    public List<String> loreContains;
    public String itemId;          // e.g. "minecraft: Netherite Hoe"
    public Integer minStackSize;
    public Integer maxStackSize;
    public Boolean enchanted;
    public String skyblockId;      // SkyBlock item id from lore/extra attributes
    public Boolean glint;

    public static GUIItemMatcher byNameContains(String contains) {
        GUIItemMatcher m = new GUIItemMatcher(); m.displayNameContains = contains; return m;
    }
    public static GUIItemMatcher byName(String exact) {
        GUIItemMatcher m = new GUIItemMatcher(); m.displayNameEquals = exact; return m;
    }
    public static GUIItemMatcher bySkyblockId(String id) {
        GUIItemMatcher m = new GUIItemMatcher(); m.skyblockId = id; return m;
    }
    public static GUIItemMatcher nameContains(String contains) { return byNameContains(contains); }
    public static GUIItemMatcher loreContains(String text) {
        GUIItemMatcher m = new GUIItemMatcher();
        m.loreContains = new ArrayList<>();
        m.loreContains.add(text);
        return m;
    }

    /**
     * Test a slot's stack against this matcher. Accepts a {@link GUIItemStack} snapshot
     * (decoupled from MC ItemStack so parsers/tests can run without MC classes).
     */
    public boolean matches(GUIItemStack stack) {
        if (stack == null) return false;
        if (displayNameContains != null && (stack.displayName == null || !stack.displayName.toLowerCase().contains(displayNameContains.toLowerCase()))) return false;
        if (displayNameEquals != null && !displayNameEquals.equalsIgnoreCase(stack.displayName)) return false;
        if (skyblockId != null && !skyblockId.equalsIgnoreCase(stack.skyblockId)) return false;
        if (itemId != null && !itemId.equals(stack.itemId)) return false;
        if (enchanted != null && enchanted != stack.enchanted) return false;
        if (glint != null && glint != stack.glint) return false;
        if (minStackSize != null && stack.stackSize < minStackSize) return false;
        if (maxStackSize != null && stack.stackSize > maxStackSize) return false;
        if (loreContains != null) {
            if (stack.lore == null) return false;
            String joined = String.join("\n", stack.lore).toLowerCase();
            for (String l : loreContains) if (!joined.contains(l.toLowerCase())) return false;
        }
        return true;
    }

    /** Lightweight ItemStack snapshot decoupled from MC. */
    public record GUIItemStack(String displayName, List<String> lore, String itemId, String skyblockId,
                               int stackSize, boolean enchanted, boolean glint) {}
}
