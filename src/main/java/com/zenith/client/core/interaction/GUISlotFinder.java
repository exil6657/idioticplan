package com.zenith.client.core.interaction;

import com.zenith.client.core.interaction.GUIItemMatcher.GUIItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds slots in the current open GUI by matcher predicates — never by hardcoded
 * index (master rule §1).
 */
public final class GUISlotFinder {

    public int findFirst(GUIItemMatcher matcher, GUIState state) {
        if (state == null || state.stacks == null) return -1;
        for (int i = 0; i < state.stacks.size(); i++) {
            GUIItemStack s = state.stacks.get(i);
            if (s != null && matcher.matches(s)) return i;
        }
        return -1;
    }

    public List<Integer> findAll(GUIItemMatcher matcher, GUIState state) {
        List<Integer> out = new ArrayList<>();
        if (state == null || state.stacks == null) return out;
        for (int i = 0; i < state.stacks.size(); i++) {
            GUIItemStack s = state.stacks.get(i);
            if (s != null && matcher.matches(s)) out.add(i);
        }
        return out;
    }

    /**
     * Find a button/label by GUI title (text rendered on-screen). Buttons are stored
     * separately from item stacks in GUIState.
     */
    public int findButton(String label, GUIState state) {
        if (state == null || state.buttons == null) return -1;
        for (int i = 0; i < state.buttons.size(); i++) {
            if (label.equalsIgnoreCase(state.buttons.get(i))) return i;
        }
        return -1;
    }
}
