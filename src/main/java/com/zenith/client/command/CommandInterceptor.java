package com.zenith.client.command;

import com.zenith.client.ZenithClient;
import com.zenith.client.config.ConfigManager;
import com.zenith.client.core.chat.ZenithChat;

/**
 * Intercepts outgoing chat/command packets and swallows dot-prefixed commands.
 *
 * <p>Phase 2: API only. The actual packet-hook is injected into the netty
 * pipeline by MixinClientConnection (Phase 6). Once wired, the mixin calls
 * {@link #onOutgoingChat(String)} which returns true when a Zenith dot command
 * was handled (so the mixin must cancel the packet).</p>
 */
public final class CommandInterceptor {

    private CommandInterceptor() {}

    /**
     * @param message raw message as typed by the user (not yet sent to server).
     * @return {@code true} if the message was intercepted and should NOT be sent to the server.
     */
    public static boolean onOutgoingChat(String message) {
        if (message == null) return false;
        String prefix = ConfigManager.getInstance().main().commandPrefix; // default ".z"
        if (!message.startsWith(prefix)) {
            // Also intercept ".z"-like "dot" messages from other mods if enabled
            return false;
        }
        CommandParser.Parsed parsed = CommandParser.parse(message, prefix);
        if (parsed == null) return false; // empty ".z" — show help
        if (parsed.name.isEmpty()) {
            CommandManager.getInstance().dispatch(new String[]{"help"});
            return true;
        }
        CommandResult res = CommandManager.getInstance().dispatch(prepend(parsed.name, parsed.args));
        if (res.getMessage() != null) {
            if (res.isSuccess()) ZenithChat.getInstance().info(res.getMessage());
            else ZenithChat.getInstance().error(res.getMessage());
        }
        return true; // always intercept — dot commands never hit the server
    }

    private static String[] prepend(String first, String[] rest) {
        String[] out = new String[rest.length + 1];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }
}
