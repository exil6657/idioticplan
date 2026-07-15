package com.zenith.client.command;

import java.util.Collections;
import java.util.List;

/** Interface for all dot-prefixed Zenith commands. */
public interface Command {

    /** The command name without prefix, e.g. "help" for ".z help". */
    String getName();

    /** Short usage line, e.g. "flip [item]". */
    String getUsage();

    /** Help description. */
    String getDescription();

    /** Execute the command with the pre-split argument list. */
    CommandResult execute(String[] args);

    /**
     * Tab-completion for the given argument list (argv[0] is the command name itself).
     * Returns a list of possible completions for the last partial argument.
     * Default returns empty list (no completions).
     */
    default List<String> suggest(String[] argv) { return Collections.emptyList(); }

    /** @return true if this command requires debug mode to be enabled. */
    default boolean requiresDebug() { return false; }
}
