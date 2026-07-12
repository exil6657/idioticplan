package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Show profit stats. */
public class ProfitCmd implements Command {

    @Override public String getName() { return "profit"; }
    @Override public String getUsage() { return "profit [session|total]"; }
    @Override public String getDescription() { return "Show profit stats."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Profit: 0 (session), 0 (total). Stats engine lands in Phase 16.");

    }
}
