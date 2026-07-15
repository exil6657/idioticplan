package com.zenith.client.core.chat;

import com.zenith.client.ZenithClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side chat manager.
 *
 * <p>All Zenith-visible HUD messages flow through this class. <b>Messages are
 * never sent to the server</b> (master rule §4). During early init (before
 * Minecraft is fully loaded) messages are logged to SLF4J; once the in-game
 * GUI is available they are added to the client HUD chat HUD via reflection-
 * safe accessor shims added in Phase 5 (GUI integration).</p>
 */
public final class ZenithChat {

    private static final Logger LOG = LoggerFactory.getLogger("ZenithChat");
    private static ZenithChat instance;

    private final ChatConfig config = new ChatConfig();
    /** When true, a Minecraft player is in-game and we can render HUD chat. */
    private boolean hudAvailable = false;
    /** Runtime sink set once the GUI system is wired (Phase 5). */
    private ChatSink hudSink;

    private ZenithChat() {}

    public static ZenithChat getInstance() {
        if (instance == null) instance = new ZenithChat();
        return instance;
    }

    public ChatConfig getConfig() { return config; }

    /** Called by Phase 5 GUI init when the in-game chat HUD is usable. */
    public void setHudSink(ChatSink sink) {
        this.hudSink = sink;
        this.hudAvailable = sink != null;
    }

    // ---- public send API -------------------------------------------------

    public void info(String msg, Object... args)  { send(ChatLevel.INFO, msg, args); }
    public void warn(String msg, Object... args)  { send(ChatLevel.WARN, msg, args); }
    public void error(String msg, Object... args) { send(ChatLevel.ERROR, msg, args); }
    public void debug(String msg, Object... args) { if (config.showDebug) send(ChatLevel.DEBUG, msg, args); }
    public void success(String msg, Object... args) { send(ChatLevel.SUCCESS, msg, args); }

    /** Plain raw send with a given level — args are {} formatted like SLF4J. */
    public void send(ChatLevel level, String message, Object... args) {
        if (!config.enabled && level != ChatLevel.ERROR) return;
        String formatted = formatArgs(message, args);
        String prefixed = prefixFor(level).getLiteral() + formatted;

        switch (level) {
            case WARN  -> LOG.warn("[Zenith] {}", formatted);
            case ERROR -> LOG.error("[Zenith] {}", formatted);
            case DEBUG -> LOG.debug("[Zenith] {}", formatted);
            default    -> LOG.info("[Zenith] {}", formatted);
        }

        if (hudAvailable && hudSink != null) {
            try {
                hudSink.accept(prefixed);
            } catch (Throwable t) {
                ZenithClient.LOGGER.error("[Chat] HUD sink failed", t);
            }
        }
    }

    private String prefixFor(ChatLevel level) {
        return switch (level) {
            case WARN    -> ChatPrefix.WARN;
            case ERROR   -> ChatPrefix.ERROR;
            case DEBUG   -> ChatPrefix.DEBUG;
            case SUCCESS -> ChatPrefix.SUCCESS;
            case INFO    -> config.style;
        };
    }

    private static String formatArgs(String msg, Object[] args) {
        if (args == null || args.length == 0) return msg;
        StringBuilder out = new StringBuilder(msg.length() + 32);
        int argIdx = 0;
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '{' && i + 1 < msg.length() && msg.charAt(i + 1) == '}' && argIdx < args.length) {
                out.append(String.valueOf(args[argIdx++]));
                i++;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Functional bridge to the in-game chat HUD (Phase 5). */
    @FunctionalInterface
    public interface ChatSink {
        void accept(String formattedMessage);
    }
}
