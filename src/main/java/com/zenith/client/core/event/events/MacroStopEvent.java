package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MacroStopEvent extends ZenithEvent {
    private final String macroId;
    private final String reason;
    public MacroStopEvent(String macroId, String reason) { this.macroId = macroId; this.reason = reason; }
    public String getMacroId() { return macroId; }
    public String getReason() { return reason; }
    @Override public boolean isCancellable() { return false; }
}
