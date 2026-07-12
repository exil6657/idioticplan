package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Open the Zenith dashboard GUI. */
public class DashboardCmd implements Command {

    @Override public String getName() { return "dashboard"; }
    @Override public String getUsage() { return "dashboard"; }
    @Override public String getDescription() { return "Open the Zenith dashboard GUI."; }

    @Override
    public CommandResult execute(String[] args) {
        
// Phase 5 GUI — no-op for Phase 2.
return CommandResult.ok("Dashboard GUI will open in Phase 5.");

    }
}
