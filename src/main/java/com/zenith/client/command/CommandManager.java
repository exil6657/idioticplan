package com.zenith.client.command;

import com.zenith.client.ZenithClient;
import com.zenith.client.command.commands.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for all dot-prefixed Zenith commands. Commands are invoked via the
 * {@code .z <name> [args...]} pattern and are intercepted client-side before
 * reaching the server (master rule §4 — no custom packets, no brand leakage).
 */
public final class CommandManager {

    private static CommandManager instance;

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private CommandManager() {}

    public static CommandManager getInstance() {
        if (instance == null) instance = new CommandManager();
        return instance;
    }

    public void registerAll() {
        register(new HelpCmd());
        register(new VersionCmd());
        register(new StatusCmd());
        register(new ToggleCmd());  // convenience: .z toggle <module>
        register(new StopCmd());
        register(new BreakCmd());
        register(new MacroCmd());
        register(new FlipCmd());
        register(new RouteCmd());
        register(new ESPCmd());
        register(new ClipCmd());
        register(new WaypointCmd());
        register(new HudCmd());
        register(new DebugCmd());
        register(new ThemeCmd());
        register(new ProfitCmd());
        register(new MayorCmd());
        register(new BudgetCmd());
        register(new SettingsCmd());
        register(new ExportCmd());
        register(new DashboardCmd());
        register(new LangCmd());
        register(new FailsafeCmd());
        ZenithClient.LOGGER.info("[CommandManager] {} commands registered", commands.size());
    }

    public void register(Command c) { commands.put(c.getName().toLowerCase(), c); }

    public Command get(String name) { return commands.get(name.toLowerCase()); }
    public Collection<Command> getAll() { return Collections.unmodifiableCollection(commands.values()); }

    /** Invoke a command given an argv array (argv[0] = command name). */
    public CommandResult dispatch(String[] argv) {
        if (argv == null || argv.length == 0) {
            return dispatch(new String[]{"help"});
        }
        Command c = get(argv[0]);
        if (c == null) {
            return CommandResult.err("Unknown command: '" + argv[0] + "'. Try .z help.");
        }
        if (c.requiresDebug() && !debugEnabled()) {
            return CommandResult.NO_PERMISSION;
        }
        String[] args = new String[argv.length - 1];
        System.arraycopy(argv, 1, args, 0, args.length);
        try { return c.execute(args); }
        catch (Throwable t) {
            ZenithClient.LOGGER.error("[Command] {} failed", c.getName(), t);
            return CommandResult.err("Command error: " + t.getMessage());
        }
    }

    private boolean debugEnabled() {
        // Later phases tie to DebugConfigFile.debugMode
        return Boolean.getBoolean("zenith.debug");
    }
}
