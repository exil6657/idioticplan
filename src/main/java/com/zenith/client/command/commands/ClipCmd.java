package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Save the last N seconds of gameplay as a clip. */
public class ClipCmd implements Command {

    @Override public String getName() { return "clip"; }
    @Override public String getUsage() { return "clip"; }
    @Override public String getDescription() { return "Save the last N seconds of gameplay as a clip."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Clip queued (Phase 17).");

    }
}
