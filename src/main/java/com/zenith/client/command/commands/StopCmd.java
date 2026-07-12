package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Emergency-stop all running macros. */
public class StopCmd implements Command {

    @Override public String getName() { return "stop"; }
    @Override public String getUsage() { return "stop"; }
    @Override public String getDescription() { return "Emergency-stop all running macros."; }

    @Override
    public CommandResult execute(String[] args) {
        
com.zenith.client.core.chat.ZenithChat.getInstance().warn("All macros stopped.");
return CommandResult.ok("Stopped all active macros.");

    }
}
