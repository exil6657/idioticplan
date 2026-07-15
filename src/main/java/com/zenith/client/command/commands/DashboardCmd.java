package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.gui.ZenithScreenWrapper;
import com.zenith.client.gui.dashboard.DashboardScreen;

/** Open the Zenith dashboard GUI. This is the primary control surface. */
public class DashboardCmd implements Command {

    @Override public String getName() { return "dashboard"; }
    @Override public String getUsage() { return "dashboard"; }
    @Override public String getDescription() { return "Open the Zenith dashboard GUI."; }

    @Override
    public CommandResult execute(String[] args) {
        ZenithScreenWrapper.open(new DashboardScreen());
        return CommandResult.ok("Dashboard opened.");
    }
}
