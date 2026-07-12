package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Open module settings GUI. */
public class SettingsCmd implements Command {

    @Override public String getName() { return "settings"; }
    @Override public String getUsage() { return "settings [module]"; }
    @Override public String getDescription() { return "Open module settings GUI."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Settings GUI opens in Phase 5.");

    }
}
