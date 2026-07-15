package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Toggle ESP rendering. */
public class ESPCmd implements Command {

    @Override public String getName() { return "esp"; }
    @Override public String getUsage() { return "esp <on|off|toggle|type>"; }
    @Override public String getDescription() { return "Toggle ESP rendering."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("ESP command acknowledged (Phase 12).");

    }
}
