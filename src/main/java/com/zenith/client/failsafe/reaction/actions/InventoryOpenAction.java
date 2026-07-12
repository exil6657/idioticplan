package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.core.input.PlayerKeybindReader;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/** Opens the player's inventory for a second ("which item do I have?"). */
public class InventoryOpenAction extends ReactionAction {

    private static final long HOLD_MS = 800L;
    private boolean opened = false;

    @Override
    protected void onStart(long nowMs) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && mc.player != null) {
            mc.execute(() -> mc.setScreen(new InventoryScreen(mc.player)));
            opened = true;
        }
    }

    @Override
    public boolean isDone(long nowMs) {
        if (!opened) return true;
        if ((nowMs - startedAt) >= HOLD_MS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof InventoryScreen) {
                mc.execute(mc.player::closeContainer);
            }
            return true;
        }
        return false;
    }

    @Override
    public String label() { return "inv-open"; }
}
