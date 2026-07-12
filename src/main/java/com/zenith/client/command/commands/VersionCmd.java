package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Display Zenith version info. */
public class VersionCmd implements Command {

    @Override public String getName() { return "version"; }
    @Override public String getUsage() { return "version"; }
    @Override public String getDescription() { return "Display Zenith version info."; }

    @Override
    public CommandResult execute(String[] args) {
        
String v = com.zenith.client.ZenithClientInfo.VERSION;
String mc = com.zenith.client.ZenithClientInfo.MC_VERSION;
return CommandResult.ok("Zenith Client v" + v + " (MC " + mc + ") by " + com.zenith.client.ZenithClientInfo.DEVELOPER);

    }
}
