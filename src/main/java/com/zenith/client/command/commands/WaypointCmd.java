package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Manage local waypoints. */
public class WaypointCmd implements Command {

    @Override public String getName() { return "waypoint"; }
    @Override public String getUsage() { return "waypoint <add|remove|list>"; }
    @Override public String getDescription() { return "Manage local waypoints."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Waypoints managed in Phase 5.");

    }
}
