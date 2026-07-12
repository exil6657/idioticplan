package com.zenith.client.failsafe.reaction;

import com.zenith.client.failsafe.FailsafeType;

/**
 * A single step in a mistake-simulation sequence.
 *
 * <p>Actions are ticked once per client tick until {@link #isDone(long)} returns
 * {@code true}. They operate <em>after</em> input is frozen, so any movement
 * they produce must look like a surprised human reacting to the situation —
 * not like a bot snapping into place.</p>
 */
public abstract class ReactionAction {

    protected long startedAt;
    protected boolean started = false;

    public final void start(long nowMs) {
        this.startedAt = nowMs;
        this.started = true;
        onStart(nowMs);
    }

    /** Called once on first tick. */
    protected void onStart(long nowMs) {}

    /** Called every tick until {@link #isDone(long)} returns true. */
    public void tick(long nowMs) {}

    /** @return true once this action is complete and the sequence should advance. */
    public abstract boolean isDone(long nowMs);

    /** Human-readable label for Brain View. */
    public abstract String label();

    /** Called to cancel early if the failsafe is cleared. */
    public void cancel() {}
}
