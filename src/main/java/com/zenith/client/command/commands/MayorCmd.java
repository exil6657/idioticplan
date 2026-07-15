package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Show the current SkyBlock mayor and perks. */
public class MayorCmd implements Command {

    @Override public String getName() { return "mayor"; }
    @Override public String getUsage() { return "mayor"; }
    @Override public String getDescription() { return "Show the current SkyBlock mayor and perks."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Mayor data unavailable (Phase 14 Mayor System).");

    }
}
