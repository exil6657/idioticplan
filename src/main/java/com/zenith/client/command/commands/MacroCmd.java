package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Control macros. */
public class MacroCmd implements Command {

    @Override public String getName() { return "macro"; }
    @Override public String getUsage() { return "macro <start|stop|list> [id]"; }
    @Override public String getDescription() { return "Control macros."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) return CommandResult.usage(getUsage());
return switch (args[0]) {
    case "list" -> CommandResult.ok("(Phase 3+) No macros registered yet.");
    case "start", "stop" -> CommandResult.ok("Macro  + (args.length > 1 ? args[1] : ) +  " + args[0] + " queued.");
    default -> CommandResult.usage(getUsage());
};

    }
}
