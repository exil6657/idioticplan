package com.zenith.client.failsafe;

import com.zenith.client.core.chat.ZenithChat;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * HUD/chat notification sink for failsafe triggers.
 *
 * <p>Triggers are queued and emitted over several ticks so multiple
 * simultaneous failsafes don't spam the player. The chat message is sent
 * immediately — this class additionally tracks the toast state read by the
 * Failsafe HUD panel (Phase 5 panel).</p>
 */
public final class PlayerNotifier {

    private static final int MAX_TOASTS = 5;
    private final Deque<Toast> toasts = new ArrayDeque<>();
    private long lastToastAt;

    public void onTrigger(FailsafeType type, String reason, FailsafeStrictness severity) {
        // Always post to chat (warn is already done in FailsafeManager.postFire).
        // Track for HUD toast.
        while (toasts.size() >= MAX_TOASTS) toasts.pollFirst();
        toasts.addLast(new Toast(type, reason, severity, System.currentTimeMillis()));
        lastToastAt = System.currentTimeMillis();
    }

    public void tick(long nowMs) {
        // Expire toasts older than 6 seconds.
        while (!toasts.isEmpty() && (nowMs - toasts.peekFirst().atMs) > 6_000L) {
            toasts.pollFirst();
        }
    }

    public Deque<Toast> activeToasts() { return toasts; }

    public boolean hasActiveToast() { return !toasts.isEmpty(); }

    public static final class Toast {
        public final FailsafeType type;
        public final String reason;
        public final FailsafeStrictness severity;
        public final long atMs;
        Toast(FailsafeType type, String reason, FailsafeStrictness severity, long atMs) {
            this.type = type; this.reason = reason; this.severity = severity; this.atMs = atMs;
        }
    }
}
