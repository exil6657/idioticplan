package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MacroErrorEvent extends ZenithEvent {
    private final String macroId;
    private final String errorMessage;
    private final Throwable throwable;
    public MacroErrorEvent(String macroId, String errorMessage, Throwable throwable) {
        this.macroId = macroId; this.errorMessage = errorMessage; this.throwable = throwable;
    }
    public String getMacroId() { return macroId; }
    public String getErrorMessage() { return errorMessage; }
    public Throwable getThrowable() { return throwable; }
    @Override public boolean isCancellable() { return false; }
}
