package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Show current client status (session, active macro, profit). */
public class StatusCmd implements Command {

    @Override public String getName() { return "status"; }
    @Override public String getUsage() { return "status"; }
    @Override public String getDescription() { return "Show current client status (session, active macro, profit)."; }

    @Override
    public CommandResult execute(String[] args) {
        
var cfg = com.zenith.client.config.ConfigManager.getInstance().main();
return CommandResult.ok("Theme=" + cfg.theme + " lang=" + cfg.language + " activeModules=" + cfg.activeModules);

    }
}
