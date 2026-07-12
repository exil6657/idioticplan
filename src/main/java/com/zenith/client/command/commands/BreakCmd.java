package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Start/end a macro break. */
public class BreakCmd implements Command {

    @Override public String getName() { return "break"; }
    @Override public String getUsage() { return "break [minutes]"; }
    @Override public String getDescription() { return "Start/end a macro break."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Break toggled.");

    }
}
