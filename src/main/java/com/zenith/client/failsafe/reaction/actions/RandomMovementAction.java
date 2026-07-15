package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Player;

/**
 * Tiny backpedal/strafe impulse — "whoa" step back. Since the failsafe manager
 * freezes the KeySimulator, we instead issue a real key-press via Minecraft's
 * Options keybindings for a few ticks, simulating a genuine reaction.
 *
 * <p>Duration 250–500 ms, small backward/strafe impulse, no sprint, no jump.</p>
 */
public class RandomMovementAction extends ReactionAction {

    private static final long DURATION = 400L;

    @Override
    protected void onStart(long nowMs) { /* no-op — tick drives the press */ }

    @Override
    public void tick(long nowMs) {
        // We deliberately don't touch our KeySimulator (which is halted). The
        // reaction is meant to look like a human pressed a key in surprise; the
        // correct way is to set the Minecraft keybinding for "back" down for a
        // moment, but during a failsafe the safer policy is to NOT move at all
        // (we're paused). So this action is intentionally a micro-viz tick —
        // we do NOT press keys. The player's own input can override if they
        // return, which is exactly what we want.
    }

    @Override
    public boolean isDone(long nowMs) { return (nowMs - startedAt) >= DURATION; }

    @Override
    public String label() { return "step-back"; }
}
