package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.Random;

/**
 * Simulates "I almost typed something" — opens the chat GUI briefly, types a
 * couple of characters, then closes it without sending. The message is never
 * sent to the server (no ServerboundChatPacket). This helps make a pause look
 * like a confused human rather than a bot halt.
 */
public class AccidentalChatAction extends ReactionAction {

    private static final long HOLD_OPEN_MS = 600L;
    private final Random rng = new Random();
    private final boolean typeFragment;
    private boolean opened = false;

    public AccidentalChatAction(boolean typeFragment) {
        this.typeFragment = typeFragment;
    }

    @Override
    protected void onStart(long nowMs) {
        Minecraft mc = Minecraft.getInstance();
        // Only open chat if no screen is currently up.
        if (mc.screen == null) {
            mc.execute(() -> mc.setScreen(new ChatScreen("")));
            opened = true;
        }
    }

    @Override
    public void tick(long nowMs) {
        if (!opened) return;
        long elapsed = nowMs - startedAt;
        // At ~300ms, type a single garbage character (if enabled), then backspace.
        // We DO NOT send the message. This is a visual-only distraction.
        if (typeFragment && elapsed > 300L && elapsed < 550L) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ChatScreen cs) {
                // Add a single character to the input. We keep it as a no-op to
                // avoid injecting real keystrokes that could accidentally send.
            }
        }
    }

    @Override
    public boolean isDone(long nowMs) {
        if (!opened) return true;
        long elapsed = nowMs - startedAt;
        if (elapsed >= HOLD_OPEN_MS) {
            // Close chat screen.
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ChatScreen) {
                mc.execute(() -> mc.setScreen(null));
            }
            return true;
        }
        return false;
    }

    @Override
    public String label() { return typeFragment ? "oops-chat" : "blink"; }
}
