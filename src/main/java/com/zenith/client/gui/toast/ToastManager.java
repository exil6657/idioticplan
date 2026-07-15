package com.zenith.client.gui.toast;

import java.util.ArrayList;
import java.util.List;

/** Client-side toast notifications (corner pop-ups) for macro events. */
public final class ToastManager {
    public record Toast(String message, int color, long expiresAt) {}
    private static final ToastManager INSTANCE = new ToastManager();
    private final List<Toast> toasts = new ArrayList<>();
    public static ToastManager getInstance() { return INSTANCE; }

    public void info(String msg)  { push(msg, 0xFFA0A0FF, 4000); }
    public void warn(String msg)  { push(msg, 0xFFFFB040, 5000); }
    public void error(String msg) { push(msg, 0xFFFF5060, 7000); }
    public void success(String msg){push(msg, 0xFF50E080, 3500); }

    private void push(String msg, int color, long duration) {
        toasts.add(new Toast(msg, color, System.currentTimeMillis() + duration));
    }

    public List<Toast> active() {
        long now = System.currentTimeMillis();
        toasts.removeIf(t -> now > t.expiresAt());
        return List.copyOf(toasts);
    }
}
