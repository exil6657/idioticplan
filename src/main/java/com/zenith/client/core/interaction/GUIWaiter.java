package com.zenith.client.core.interaction;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Waits for a GUI condition to be true (e.g. "slot X is now Y", "title changed to Z"),
 * returning true when the condition is met or false on timeout. Used by macro
 * code instead of Thread.sleep.
 *
 * <p>A static helper {@link #waitForTitle(String, long)} is provided for the
 * common "wait until a screen with this title substring opens" case; poll it
 * via {@link #checkAll()} from the tick loop.</p>
 */
public final class GUIWaiter {

    /** Static-helper waiter entry for title waits. */
    private static final List<TitleWait> pending = new ArrayList<>();

    private final com.zenith.client.core.timer.Timer timer = new com.zenith.client.core.timer.Timer();
    private BooleanSupplier condition;
    private long timeoutMs;
    private boolean started;

    public GUIWaiter waitFor(BooleanSupplier cond, long timeoutMs) {
        this.condition = cond; this.timeoutMs = timeoutMs;
        timer.reset(); started = true;
        return this;
    }

    /** @return true if condition met; false on timeout; null while waiting. */
    public Boolean check() {
        if (!started) return false;
        if (condition == null) return false;
        if (condition.getAsBoolean()) { started = false; return true; }
        if (timer.hasElapsed(timeoutMs)) { started = false; return false; }
        return null;
    }

    public void cancel() { started = false; condition = null; }

    /** Schedule a wait for a title substring; fires once. */
    public static void waitForTitle(String titleSubstr, long timeoutMs) {
        pending.add(new TitleWait(titleSubstr, System.currentTimeMillis() + timeoutMs));
    }

    /** Poll pending waits and clear expired/met ones. Called from GUIInteractionEngine. */
    public static void checkAll() {
        Minecraft mc = Minecraft.getInstance();
        String title = null;
        if (mc.screen != null && mc.screen.getTitle() != null) title = mc.screen.getTitle().getString();
        Iterator<TitleWait> it = pending.iterator();
        while (it.hasNext()) {
            TitleWait w = it.next();
            boolean met = title != null && title.contains(w.substring);
            boolean expired = System.currentTimeMillis() > w.deadlineMs;
            if (met || expired) it.remove();
        }
    }

    public static boolean hasAnyPending() { return !pending.isEmpty(); }

    private static final class TitleWait {
        final String substring;
        final long deadlineMs;
        TitleWait(String s, long d) { this.substring = s; this.deadlineMs = d; }
    }
}
