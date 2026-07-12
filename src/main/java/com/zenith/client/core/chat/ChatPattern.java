package com.zenith.client.core.chat;

import java.util.regex.Pattern;

/**
 * A compiled regex pattern with a handler that fires when the pattern matches a
 * chat line. Each pattern corresponds to one SkyBlock message format (rare drop,
 * skill up, coin change, etc.).
 */
public final class ChatPattern {
    public final Pattern regex;
    public final ChatHandler handler;
    public ChatPattern(String regex, ChatHandler handler) {
        this.regex = Pattern.compile(regex);
        this.handler = handler;
    }

    @FunctionalInterface
    public interface ChatHandler {
        void handle(String[] groups, String raw);
    }
}
