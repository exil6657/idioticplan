package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.gui.GuiEngine;

/** Debug utilities (brain view, thread dump, state dump). */
public class DebugCmd implements Command {
    @Override public String getName() { return "debug"; }
    @Override public String getUsage() { return "debug <brain|threads|state>"; }
    @Override public String getDescription() { return "Debug tools (requires debug mode)."; }
    @Override public boolean requiresDebug() { return false; } // accessible for brain view always

    @Override
    public CommandResult execute(String[] args) {
        if (args.length == 0) return CommandResult.usage(getUsage());
        return switch (args[0].toLowerCase()) {
            case "brain" -> {
                GuiEngine.getInstance().toggleBrainView();
                yield CommandResult.ok("Brain view " + (GuiEngine.getInstance().isBrainViewShown() ? "shown" : "hidden"));
            }
            case "threads" -> CommandResult.ok("Thread count: " + Thread.activeCount());
            case "state" -> CommandResult.ok("State dump to logs/zenith_debug.log (Phase 8).");
            default -> CommandResult.usage(getUsage());
        };
    }
}
