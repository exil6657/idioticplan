package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** List all commands or show help for a specific command. */
public class HelpCmd implements Command {

    @Override public String getName() { return "help"; }
    @Override public String getUsage() { return "help [command]"; }
    @Override public String getDescription() { return "List all commands or show help for a specific command."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) {
    StringBuilder sb = new StringBuilder("Commands:\n");
    for (var c : com.zenith.client.command.CommandManager.getInstance().getAll()) {
        sb.append("  .z ").append(c.getName()).append(" ").append(c.getUsage()).append(" — ").append(c.getDescription()).append("\n");
    }
    return CommandResult.ok(sb.toString().trim());
}
var c = com.zenith.client.command.CommandManager.getInstance().get(args[0]);
if (c == null) return CommandResult.err("Unknown command: " + args[0]);
return CommandResult.ok(".z " + c.getName() + " " + c.getUsage() + " — " + c.getDescription());

    }
}
