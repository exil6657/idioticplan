package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;

/**
 * Opens chat and pastes a pre-scripted auto-response (e.g. "afk a sec") that
 * the player can manually send by pressing enter. WE NEVER SEND AUTOMATICALLY
 * — that would be a server-visible automation indicator. We simply pre-fill
 * the chat box; the player sends when they return.
 */
public class ChatResponseAction extends ReactionAction {

    private final String response;
    private static final long DURATION = 400L;
    private boolean opened = false;

    public ChatResponseAction() { this("brb"); }
    public ChatResponseAction(String response) { this.response = response; }

    @Override
    protected void onStart(long nowMs) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            mc.execute(() -> mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen(response)));
            opened = true;
        }
    }

    @Override
    public boolean isDone(long nowMs) {
        if (!opened) return true;
        return (nowMs - startedAt) >= DURATION; // leave chat open for the player to review
    }

    @Override
    public String label() { return "chat-prefill"; }
}
