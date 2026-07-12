package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.failsafe.FailsafeManager;

/**
 * {@code .z failsafe} — inspect/control the failsafe system from chat.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code .z failsafe status} — print current active trigger + severity.</li>
 *   <li>{@code .z failsafe resume} — clear all triggers and resume macros.</li>
 *   <li>{@code .z failsafe test &lt;type&gt;} — fire a synthetic trigger (debug).</li>
 *   <li>{@code .z failsafe list} — list detector enabled states.</li>
 * </ul>
 */
public class FailsafeCmd implements Command {

    @Override public String getName() { return "failsafe"; }
    @Override public String getUsage() { return "failsafe <status|resume|test|list>"; }
    @Override public String getDescription() { return "Inspect/control the failsafe system."; }

    @Override
    public CommandResult execute(String[] args) {
        FailsafeManager mgr = FailsafeManager.getInstance();
        String sub = args.length > 0 ? args[0].toLowerCase() : "status";
        switch (sub) {
            case "status" -> {
                var d = mgr.debugData();
                ZenithChat.getInstance().info("Failsafe: active={}, sev={}, type={}, reaction={}, triggers={}",
                        d.active, d.currentSeverity,
                        d.activeType != null ? d.activeType.displayName() : "—",
                        d.reactionState, d.triggersSinceStartup);
                if (d.active) ZenithChat.getInstance().warn("  reason: {}", d.activeReason);
                return CommandResult.ok("failsafe status printed");
            }
            case "resume", "clear" -> {
                mgr.resumeFromUser();
                return CommandResult.ok("failsafe cleared");
            }
            case "test" -> {
                String which = args.length > 1 ? args[1] : "CUSTOM";
                try {
                    com.zenith.client.failsafe.FailsafeType t = com.zenith.client.failsafe.FailsafeType.valueOf(which.toUpperCase());
                    mgr.trigger(t, "manual test", com.zenith.client.failsafe.FailsafeStrictness.NOTIFY);
                    return CommandResult.ok("test trigger fired: " + t.displayName());
                } catch (IllegalArgumentException e) {
                    return CommandResult.error("unknown failsafe type: " + which);
                }
            }
            case "list" -> {
                StringBuilder sb = new StringBuilder("detectors: ");
                for (var t : com.zenith.client.failsafe.FailsafeType.values()) {
                    sb.append(t.name()).append('=')
                            .append(mgr.config().isDetectorEnabled(t) ? '+' : '-')
                            .append(' ');
                }
                ZenithChat.getInstance().info(sb.toString());
                return CommandResult.ok("detector list printed");
            }
            default -> {
                return CommandResult.error("unknown subcommand: " + sub);
            }
        }
    }
}
