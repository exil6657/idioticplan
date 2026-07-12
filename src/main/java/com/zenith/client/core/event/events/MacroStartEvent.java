package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MacroStartEvent extends ZenithEvent {
    private final String macroId;
    public MacroStartEvent(String macroId) { this.macroId = macroId; }
    public String getMacroId() { return macroId; }
    @Override public boolean isCancellable() { return false; }
}
