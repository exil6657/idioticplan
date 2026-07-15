package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.macro.MacroManager;
import com.zenith.client.macro.MacroModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Control macros. */
public class MacroCmd implements Command {

    @Override public String getName() { return "macro"; }
    @Override public String getUsage() { return "macro <start|stop|list|pause|resume> [id]"; }
    @Override public String getDescription() { return "Start/stop/list macros or pause/resume the active one."; }

    @Override
    public CommandResult execute(String[] args) {
        if (args.length == 0) return CommandResult.usage(getUsage());
        MacroManager mgr = MacroManager.getInstance();
        return switch (args[0]) {
            case "list" -> {
                StringBuilder sb = new StringBuilder("Macros registered:\n");
                for (MacroModule m : mgr.all()) {
                    sb.append("  ").append(m.id()).append(" — ").append(m.displayName())
                            .append(" [").append(m.state()).append("]\n");
                }
                if (mgr.all().isEmpty()) sb.append("  (none)");
                yield CommandResult.ok(sb.toString().trim());
            }
            case "start" -> {
                if (args.length < 2) yield CommandResult.usage(getUsage());
                boolean ok = mgr.start(args[1]);
                yield ok ? CommandResult.ok("Started " + args[1])
                         : CommandResult.err("Failed to start " + args[1]);
            }
            case "stop" -> {
                mgr.stopAll("manual");
                yield CommandResult.ok("All macros stopped.");
            }
            case "pause" -> {
                mgr.pauseAll("manual");
                yield CommandResult.ok("Active macro paused.");
            }
            case "resume" -> {
                mgr.resumeActive();
                yield CommandResult.ok("Active macro resumed.");
            }
            default -> CommandResult.usage(getUsage());
        };
    }

    @Override
    public List<String> suggest(String[] argv) {
        // argv[0] = "macro"
        if (argv.length <= 2) return List.of("start", "stop", "list", "pause", "resume");
        if ("start".equals(argv[1])) {
            String partial = argv[argv.length - 1].toLowerCase();
            List<String> out = new ArrayList<>();
            for (MacroModule m : MacroManager.getInstance().all()) {
                if (m.id().toLowerCase().startsWith(partial)) out.add(m.id());
            }
            return out;
        }
        return Collections.emptyList();
    }
}
