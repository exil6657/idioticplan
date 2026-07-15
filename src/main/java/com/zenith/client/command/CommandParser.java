package com.zenith.client.command;

/**
 * Parses a raw chat string (with or without the prefix) into a command name
 * and argument list. Thread-safe, no allocations beyond the returned record.
 */
public final class CommandParser {

    private CommandParser() {}

    public static Parsed parse(String raw, String prefix) {
        if (raw == null) return null;
        String trim = raw.trim();
        if (trim.isEmpty()) return null;
        if (prefix != null && trim.startsWith(prefix)) {
            trim = trim.substring(prefix.length()).trim();
        }
        String[] parts = trim.split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return null;
        String name = parts[0].toLowerCase(java.util.Locale.ROOT);
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return new Parsed(name, args);
    }

    public static final class Parsed {
        public final String name;
        public final String[] args;
        Parsed(String name, String[] args) { this.name = name; this.args = args; }
    }
}
