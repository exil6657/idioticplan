package com.zenith.client.gui.animation;

import java.util.ArrayList;
import java.util.List;

/** Plays a sequence of time-based keyframes in order. */
public final class AnimationSequence {
    public record Keyframe(long offsetMs, Runnable action) {}
    private final List<Keyframe> frames = new ArrayList<>();
    private long startMs;
    private int next = 0;
    private boolean playing;

    public AnimationSequence add(long offsetMs, Runnable action) {
        frames.add(new Keyframe(offsetMs, action));
        frames.sort((a,b) -> Long.compare(a.offsetMs(), b.offsetMs()));
        return this;
    }

    public void start() { startMs = System.currentTimeMillis(); next = 0; playing = true; }
    public void stop() { playing = false; }

    public void tick() {
        if (!playing) return;
        long elapsed = System.currentTimeMillis() - startMs;
        while (next < frames.size() && frames.get(next).offsetMs() <= elapsed) {
            frames.get(next).action().run();
            next++;
        }
        if (next >= frames.size()) playing = false;
    }
}
