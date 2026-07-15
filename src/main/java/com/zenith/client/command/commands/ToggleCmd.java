package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.core.module.Module;

/** Toggle a module by id. */
public class ToggleCmd implements Command {
    @Override public String getName() { return "toggle"; }
    @Override public String getUsage() { return "toggle <module>"; }
    @Override public String getDescription() { return "Enable/disable a module by id."; }
    @Override
    public CommandResult execute(String[] args) {
        if (args.length == 0) return CommandResult.usage(getUsage());
        Module m = com.zenith.client.core.module.ModuleManager.getInstance().getById(args[0]);
        if (m == null) return CommandResult.err("No module with id '" + args[0] + "'");
        m.toggle();
        return CommandResult.ok((m.isEnabled() ? "Enabled " : "Disabled ") + m.getDisplayName());
    }
}
