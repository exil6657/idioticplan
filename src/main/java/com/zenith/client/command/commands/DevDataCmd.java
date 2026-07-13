package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.devdata.DevDataHarvester;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code .z devdata} — control the passive developer-data harvester.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code on} / {@code off} — enable/disable passive recording.</li>
 *   <li>{@code status} — current state, counts, output path.</li>
 *   <li>{@code flush} — immediately write {@code OBSERVATIONS.md} to disk.</li>
 *   <li>{@code snapshot} — take a one-shot GUI/scoreboard snapshot right now.</li>
 *   <li>{@code note <free text>} — append a timestamped developer note to the file.</li>
 *   <li>{@code path} — print the output .md file path.</li>
 * </ul>
 */
public class DevDataCmd implements Command {

    @Override public String getName() { return "devdata"; }
    @Override public String getUsage() { return "devdata <on|off|status|flush|snapshot|note|tour|stop-tour|path>"; }
    @Override public String getDescription() { return "Passive harvester + dev data tour macro → zenith/devdata/OBSERVATIONS.md."; }
    @Override public boolean requiresDebug() { return false; }

    @Override
    public CommandResult execute(String[] args) {
        DevDataHarvester h = DevDataHarvester.getInstance();
        String sub = args.length > 0 ? args[0].toLowerCase() : "status";
        switch (sub) {
            case "on", "enable", "start" -> {
                h.setEnabled(true);
                return CommandResult.ok("devdata harvester ENABLED. Writing to " + h.getOutputPath());
            }
            case "off", "disable", "stop" -> {
                h.setEnabled(false);
                return CommandResult.ok("devdata harvester DISABLED (file preserved at " + h.getOutputPath() + ").");
            }
            case "status" -> {
                ZenithChat.getInstance().info("DevData: enabled={}, guis={}, chat={}, uptime={}m",
                        h.isEnabled(),
                        h.getObservedGuiCount(),
                        h.getObservedChatCount(),
                        h.getSessionMs() / 60_000);
                ZenithChat.getInstance().info("Output: {}", h.getOutputPath());
                return CommandResult.ok("devdata status printed");
            }
            case "flush", "write", "save" -> {
                h.flush();
                return CommandResult.ok("devdata flushed → " + h.getOutputPath());
            }
            case "snapshot", "snap" -> {
                // Kick an immediate GUI + passive snapshot by cycling a note + flush.
                h.note("Manual snapshot requested via .z devdata snapshot.");
                h.flush();
                return CommandResult.ok("devdata snapshot taken and flushed.");
            }
            case "note", "annotate" -> {
                if (args.length < 2) return CommandResult.usage("usage: .z devdata note <text>");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) sb.append(' ');
                    sb.append(args[i]);
                }
                h.note("[MANUAL] " + sb);
                h.flush();
                return CommandResult.ok("note appended to devdata file.");
            }
            case "path", "file" -> {
                ZenithChat.getInstance().info("DevData file: {}", h.getOutputPath());
                return CommandResult.ok(h.getOutputPath().toString());
            }
            case "tour", "run", "start-tour" -> {
                // Stop any running macro first, then launch the dev data tour macro.
                com.zenith.client.macro.MacroManager.getInstance().stopAll("devdata tour");
                com.zenith.client.macro.MacroManager.getInstance().start("dev:devdata");
                return CommandResult.ok("DevData tour started — stopping at /is. Do not touch inputs.");
            }
            case "stop-tour" -> {
                com.zenith.client.macro.MacroManager.getInstance().stopAll("devdata tour stop");
                return CommandResult.ok("DevData tour stopped.");
            }
            default -> {
                return CommandResult.error("unknown devdata subcommand: " + sub);
            }
        }
    }

    @Override
    public List<String> suggest(String[] argv) {
        if (argv.length <= 2) {
            return List.of("on", "off", "status", "flush", "snapshot", "note", "tour", "stop-tour", "path");
        }
        return new ArrayList<>();
    }
}
