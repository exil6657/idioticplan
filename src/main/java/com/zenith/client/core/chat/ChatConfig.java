package com.zenith.client.core.chat;

/**
 * Configuration values for the chat system.
 *
 * <p>Loaded by {@code ConfigManager} (Phase 2) from {@code config/zenith.json}.
 * Kept mutable in-memory; changes are persisted via Gson.</p>
 */
public class ChatConfig {

    /** Master chat toggle — if false, suppress all Zenith chat output. */
    public boolean enabled = true;

    /** If true, show DEBUG-level messages. */
    public boolean showDebug = false;

    /** If true, prefix timestamps are appended to messages. */
    public boolean showTimestamps = true;

    /** If true, play a click sound on WARN/ERROR. */
    public boolean soundOnAlert = true;

    /** Chat prefix style (default colourized). */
    public ChatPrefix style = ChatPrefix.DEFAULT;

    public boolean isEnabled() { return enabled; }
    public boolean isShowDebug() { return showDebug; }
    public boolean isShowTimestamps() { return showTimestamps; }
    public boolean isSoundOnAlert() { return soundOnAlert; }
}
