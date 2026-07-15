package com.zenith.client.command;

import com.zenith.client.command.commands.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Tab-completer for dot-commands. Called from a Mixin on the ChatScreen.
 *
 * <p>Because we don't register with brigadier (that would send a brand packet to
 * the server and violate rule §4), we implement our own completion: the first
 * token is the command name (all registered commands), subsequent tokens come
 * from each command's {@link Command#suggest(String[])} method.</p>
 */
public final class CommandCompleter {

    private static final CommandCompleter INSTANCE = new CommandCompleter();
    public static CommandCompleter getInstance() { return INSTANCE; }

    private static final List<String> ROOT_SUBS = List.of(
            "help", "version", "status", "toggle", "stop", "break", "macro",
            "flip", "route", "esp", "clip", "waypoint", "hud", "debug",
            "theme", "profit", "mayor", "budget", "settings", "export",
            "dashboard", "lang", "failsafe"
    );

    private CommandCompleter() {}

    public boolean isDotCommand(String fullText) {
        if (fullText == null) return false;
        String t = fullText.trim();
        return t.equals(".z") || t.startsWith(".z ") || t.startsWith(".") && t.length() <= 2 && !t.equals("/");
    }

    public List<String> complete(String fullText) {
        String t = fullText == null ? "" : fullText;
        if (t.equals(".z") || t.equals(".z ")) return prependDot(ROOT_SUBS, ".z ");

        // Strip leading ".z "
        String body = t.startsWith(".z ") ? t.substring(3) : t;
        String[] parts = split(body);
        String last = parts.length == 0 ? "" : parts[parts.length - 1];
        String[] argv = new String[parts.length + 1];
        argv[0] = parts.length == 0 ? "" : parts[0];
        System.arraycopy(parts, 0, argv, 1, parts.length);

        if (parts.length == 0 || (parts.length == 1 && !find(parts[0]))) {
            // completing command name
            return filter(ROOT_SUBS, last == null ? "" : last, ".z ");
        }

        // completing arguments
        Command c = CommandManager.getInstance().get(parts[0]);
        if (c == null) return List.of();
        List<String> raw = c.suggest(argv);
        return filter(raw, last == null ? "" : last, "");
    }

    private boolean find(String name) {
        return CommandManager.getInstance().get(name) != null;
    }

    private static String[] split(String line) {
        if (line.isEmpty()) return new String[0];
        // Preserve trailing empty token for partial completions
        boolean trailingSpace = line.endsWith(" ");
        String[] a = line.trim().split(" ", -1);
        if (trailingSpace) {
            String[] out = Arrays.copyOf(a, a.length + 1);
            out[out.length - 1] = "";
            return out;
        }
        return a;
    }

    private static List<String> filter(List<String> options, String partial, String prefix) {
        String p = partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : options) {
            if (s.toLowerCase(Locale.ROOT).startsWith(p)) out.add(prefix + s);
        }
        return out;
    }

    private static List<String> prependDot(List<String> opts, String prefix) {
        List<String> out = new ArrayList<>(opts.size());
        for (String s : opts) out.add(prefix + s);
        return out;
    }
}
