package com.zenith.client.command.commands;

import com.zenith.client.command.Command;
import com.zenith.client.command.CommandResult;
import com.zenith.client.gui.hud.editor.HudEditor;

public class HudCmd implements Command {
    @Override public String getName() { return "hud"; }
    @Override public String getUsage() { return "hud <on|off|edit|reset>"; }
    @Override public String getDescription() { return "Control HUD rendering and HUD editor."; }
    @Override
    public CommandResult execute(String[] args) {
        if (args.length == 0) return CommandResult.usage(getUsage());
        return switch (args[0].toLowerCase()) {
            case "edit" -> { HudEditor.getInstance().toggle();
                yield CommandResult.ok("HUD editor " + (HudEditor.getInstance().isActive() ? "enabled" : "disabled")); }
            case "on" -> CommandResult.ok("HUD on");
            case "off" -> CommandResult.ok("HUD off");
            case "reset" -> CommandResult.ok("HUD layout reset to defaults");
            default -> CommandResult.usage(getUsage());
        };
    }
}
