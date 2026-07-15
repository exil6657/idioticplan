package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Manage routes / waypoint paths. */
public class RouteCmd implements Command {

    @Override public String getName() { return "route"; }
    @Override public String getUsage() { return "route <start|stop|save|load>"; }
    @Override public String getDescription() { return "Manage routes / waypoint paths."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Route command acknowledged (Phase 7/11).");

    }
}
