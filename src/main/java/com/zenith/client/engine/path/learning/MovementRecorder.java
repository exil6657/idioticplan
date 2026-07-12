package com.zenith.client.engine.path.learning;

import java.util.ArrayList;
import java.util.List;

/**
 * Circular buffer of {@link MovementSample}s recorded during non-macro play.
 *
 * <p>When recording is enabled (default: whenever no macro is active and the
 * player is moving) the recorder keeps the last {@code WINDOW_SEC} seconds of
 * tick samples. The {@link ProfileBuilder} periodically drains samples into a
 * new profile version.</p>
 */
public final class MovementRecorder {

    public static final int WINDOW_SEC = 600; // 10 minutes
    public static final int SAMPLE_HZ = 20;   // tick rate
    private static final int CAPACITY = WINDOW_SEC * SAMPLE_HZ;

    private final MovementSample[] samples = new MovementSample[CAPACITY];
    private int head;
    private int size;
    private boolean enabled = true;
    private boolean macroActive;

    public void setEnabled(boolean v) { this.enabled = v; }
    public void setMacroActive(boolean v) { this.macroActive = v; }

    /** Push a sample; oldest is overwritten when full. */
    public synchronized void record(MovementSample s) {
        if (!enabled || macroActive) return;
        samples[head] = s;
        head = (head + 1) % CAPACITY;
        if (size < CAPACITY) size++;
    }

    public synchronized List<MovementSample> drain() {
        List<MovementSample> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            MovementSample s = samples[(head - size + i + CAPACITY) % CAPACITY];
            if (s != null) out.add(s);
        }
        size = 0; head = 0; // reset for next window
        return out;
    }

    public synchronized int size() { return size; }
}
