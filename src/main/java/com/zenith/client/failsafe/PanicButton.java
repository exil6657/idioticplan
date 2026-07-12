package com.zenith.client.failsafe;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.input.KeySimulator;
import com.zenith.client.engine.path.ZenithPath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Immediate manual kill-switch bound to the PANIC_BUTTON keybind.
 *
 * <p>Unlike regular failsafe escalation, the panic button takes instant action
 * on the very next tick: halts input, cancels pathing, disables eye rotation,
 * blocks bits spending, and (if the player holds the bind for >400 ms)
 * disconnects.</p>
 */
public final class PanicButton {

    private final FailsafeManager mgr;
    private long pressedAtMs;
    private boolean held = false;

    PanicButton(FailsafeManager mgr) { this.mgr = mgr; }

    public void onPress() {
        pressedAtMs = System.currentTimeMillis();
        held = true;
        engage(false);
        ZenithChat.getInstance().error("PANIC — all macros & input HALTED. Hold 400 ms to disconnect.");
    }

    public void onRelease() {
        held = false;
    }

    public void tick(long nowMs) {
        if (held && (nowMs - pressedAtMs) > 400) {
            engage(true);
            held = false;
        }
    }

    private void engage(boolean disconnect) {
        // Freeze everything.
        ZenithPath.getInstance().stop();
        ZenithEyes.getInstance().setEnabled(false);
        KeySimulator keys = InputEngine.getInstance().keys();
        if (keys != null) keys.halt();
        mgr.trigger(FailsafeType.CUSTOM, "manual panic", disconnect ? FailsafeStrictness.DISCONNECT : FailsafeStrictness.PAUSE);
        if (disconnect) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                mc.execute(() -> {
                    try {
                        mc.getConnection().getConnection().disconnect(
                                Component.literal("Zenith — manual panic disconnect"));
                    } catch (Throwable t) {
                        ZenithClient.LOGGER.error("[Panic] disconnect failed", t);
                    }
                });
            }
        }
    }

    public boolean isHeld() { return held; }
}
