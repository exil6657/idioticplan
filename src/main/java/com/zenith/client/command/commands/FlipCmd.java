package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.flipping.FlipEngine;
import com.zenith.client.flipping.budget.BudgetManager;
import com.zenith.client.flipping.profit.ProfitTracker;

/** {@code .z flip} — control the flipping engine from chat. */
public class FlipCmd implements Command {

    @Override public String getName() { return "flip"; }
    @Override public String getUsage() { return "flip <start|stop|status|profit|reset>"; }
    @Override public String getDescription() { return "Control the AH/Bazaar flipper."; }

    @Override
    public CommandResult execute(String[] args) {
        FlipEngine e = FlipEngine.getInstance();
        String sub = args.length > 0 ? args[0].toLowerCase() : "status";
        switch (sub) {
            case "start", "on" -> {
                e.init(); e.start();
                return CommandResult.ok("flipper started");
            }
            case "stop", "off" -> { e.stop(); return CommandResult.ok("flipper stopped"); }
            case "profit", "stats" -> {
                ProfitTracker p = ProfitTracker.getInstance();
                var a = e.orders().activeOrders();
                ZenithChat.getInstance().info("Flipper: session profit={}, flips={}, active orders={}, queued={}",
                        p.sessionProfit(), p.sessionFlips(), a.size(), e.scanner().queueDepth());
                return CommandResult.ok("flipper stats printed");
            }
            case "reset" -> {
                ProfitTracker.getInstance().resetSession();
                BudgetManager.getInstance().config().resetSession();
                return CommandResult.ok("flip session reset");
            }
            case "status" -> {
                var d = e.debugData();
                ZenithChat.getInstance().info("Flipper running={} activeOrders={} listed={} holding={} budget={} scans={} cands={}",
                        d.running, d.activeOrders, d.listedOrders, d.holdingOrders,
                        d.budget, d.scans, d.candidateQueueDepth);
                return CommandResult.ok("flipper status");
            }
            default -> { return CommandResult.error("unknown subcommand: " + sub); }
        }
    }

    @Override
    public java.util.List<String> suggest(String[] argv) {
        if (argv.length <= 2) return java.util.List.of("start", "stop", "status", "profit", "stats", "reset");
        return java.util.List.of();
    }
}
