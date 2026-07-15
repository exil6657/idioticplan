package com.zenith.client.macro.test;

import com.zenith.client.macro.MacroModule;
import net.minecraft.client.Minecraft;

/**
 * Test macro — does nothing except register itself and look around occasionally.
 * Useful for smoke-testing the lifecycle (start/pause/resume/stop) and macro
 * framework before real farming/combat macros exist in Phase 13+.
 */
public final class IdleMacro extends MacroModule {

    public static final IdleMacro INSTANCE = new IdleMacro();

    private IdleMacro() {}

    @Override public String id() { return "test:idle"; }
    @Override public String displayName() { return "Idle (test)"; }
    @Override public String icon() { return "●"; }
    @Override public String skillFamily() { return "Test"; }

    @Override protected void onStart() {
        // Look at the player's own feet briefly to signal start.
    }

    @Override protected void onTick() {
        // Do nothing — verifies the macro tick loop is alive without moving.
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
    }

    @Override protected void onStop() { /* no-op */ }

    @Override public double destX() {
        var p = Minecraft.getInstance().player;
        return p == null ? 0 : p.getX();
    }
    @Override public double destY() {
        var p = Minecraft.getInstance().player;
        return p == null ? 0 : p.getY();
    }
    @Override public double destZ() {
        var p = Minecraft.getInstance().player;
        return p == null ? 0 : p.getZ();
    }
}
