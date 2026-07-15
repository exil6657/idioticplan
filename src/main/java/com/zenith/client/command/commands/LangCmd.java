package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Set language (en_gb, etc.). */
public class LangCmd implements Command {

    @Override public String getName() { return "lang"; }
    @Override public String getUsage() { return "lang <code>"; }
    @Override public String getDescription() { return "Set language (en_gb, etc.)."; }

    @Override
    public CommandResult execute(String[] args) {
        
if (args.length == 0) return CommandResult.ok("Current language: " + com.zenith.client.config.ConfigManager.getInstance().main().language);
com.zenith.client.config.ConfigManager.getInstance().main().language = args[0];
com.zenith.client.config.ConfigManager.getInstance().requestSave();
return CommandResult.ok("Language set to " + args[0]);

    }
}
