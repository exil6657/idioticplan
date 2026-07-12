package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Select a GUI theme. */
public class ThemeCmd implements Command {

    @Override public String getName() { return "theme"; }
    @Override public String getUsage() { return "theme <dark|light|custom>"; }
    @Override public String getDescription() { return "Select a GUI theme."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) return CommandResult.usage(getUsage());
com.zenith.client.config.ConfigManager.getInstance().main().theme = args[0];
com.zenith.client.config.ConfigManager.getInstance().requestSave();
return CommandResult.ok("Theme set to " + args[0]);

    }
}
