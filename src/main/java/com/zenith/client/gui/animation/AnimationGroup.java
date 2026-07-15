package com.zenith.client.gui.animation;

import java.util.ArrayList;
import java.util.List;

/** Groups a set of AnimatedValues/Springs so they can be opened/closed together. */
public final class AnimationGroup {
    public enum State { CLOSED, OPENING, OPEN, CLOSING }
    private final List<Runnable> openActions = new ArrayList<>();
    private final List<Runnable> closeActions = new ArrayList<>();
    private State state = State.CLOSED;
    private long toggleAt;

    public void onOpen(Runnable r) { openActions.add(r); }
    public void onClose(Runnable r) { closeActions.add(r); }

    public void open() {
        if (state == State.OPEN || state == State.OPENING) return;
        state = State.OPENING; toggleAt = System.currentTimeMillis();
        for (Runnable r : openActions) r.run();
    }

    public void close() {
        if (state == State.CLOSED || state == State.CLOSING) return;
        state = State.CLOSING; toggleAt = System.currentTimeMillis();
        for (Runnable r : closeActions) r.run();
    }

    public void toggle() { if (state == State.OPEN) close(); else open(); }
    public State getState() { return state; }
    public void markOpen() { state = State.OPEN; }
    public void markClosed() { state = State.CLOSED; }
}
