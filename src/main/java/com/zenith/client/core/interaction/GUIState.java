package com.zenith.client.core.interaction;

import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of the currently open GUI: title, size, items, buttons, player inventory.
 * Built each tick by the GUIInteractionEngine from the MC Screen container.
 */
public final class GUIState {
    public String title;
    public int containerId = -1;
    public int rows = 0;       // number of chest rows (for container sizing)
    public int slots = 0;
    public List<GUIItemStack> stacks = new ArrayList<>();
    public List<String> buttons = new ArrayList<>();
    /** Player inventory starts at this slot index (usually 9 rows up). */
    public int playerInventoryStart;

    /** @return true if a GUI is currently open and the state is valid. */
    public boolean present() { return title != null && stacks != null && !stacks.isEmpty(); }

    public GUIItemStack stackAt(int slot) {
        if (slot < 0 || slot >= stacks.size()) return null;
        return stacks.get(slot);
    }
}
