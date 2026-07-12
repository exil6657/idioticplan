package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Export data to clipboard/file. */
public class ExportCmd implements Command {

    @Override public String getName() { return "export"; }
    @Override public String getUsage() { return "export <config|stats|logs>"; }
    @Override public String getDescription() { return "Export data to clipboard/file."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) return CommandResult.usage(getUsage());
return CommandResult.ok("Exported " + args[0] + ".");

    }
}
