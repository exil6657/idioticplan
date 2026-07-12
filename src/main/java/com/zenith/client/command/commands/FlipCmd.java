package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Control the bazaar/ah flipper. */
public class FlipCmd implements Command {

    @Override public String getName() { return "flip"; }
    @Override public String getUsage() { return "flip <on|off|status|list>"; }
    @Override public String getDescription() { return "Control the bazaar/ah flipper."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) return CommandResult.usage(getUsage());
return CommandResult.ok("Flipper command  + args[0] +  acknowledged (Phase 9).");

    }
}
