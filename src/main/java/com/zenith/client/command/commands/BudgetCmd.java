package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;

/** Configure flipper budget. */
public class BudgetCmd implements Command {

    @Override public String getName() { return "budget"; }
    @Override public String getUsage() { return "budget <set|show|reset>"; }
    @Override public String getDescription() { return "Configure flipper budget."; }

    @Override
    public CommandResult execute(String[] args) {
        
return CommandResult.ok("Budget manager lands in Phase 10.");

    }
}
