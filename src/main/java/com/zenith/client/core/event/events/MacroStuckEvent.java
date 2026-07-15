package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MacroStuckEvent extends ZenithEvent {
    private final String macroId;
    private final long stuckMs;
    public MacroStuckEvent(String macroId, long stuckMs) { this.macroId = macroId; this.stuckMs = stuckMs; }
    public String getMacroId() { return macroId; }
    public long getStuckMs() { return stuckMs; }
    @Override public boolean isCancellable() { return false; }
}
