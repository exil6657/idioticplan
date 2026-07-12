package com.zenith.client.engine.path.reaction;

/**
 * A set of "annoyed" reactions the bot takes when something small goes wrong
 * (menu didn't open, block is in the way, wrong item, lag spike).
 *
 * <p>Reactions are <em>visible</em> little human-like behaviours: wiggling the
 * mouse briefly, pressing backspace (if in chat), shaking the head, etc. The
 * goal is to avoid the robotic feel of a macro that just silently retries.</p>
 */
public enum AnnoyedReactionType {
    HEAD_SHAKE,
    SMALL_WIGGLE,
    BRIEF_PAUSE,
    CAMERA_JITTER,
    CURSOR_NUDGE,
    HAND_SWING_CANCEL
}
