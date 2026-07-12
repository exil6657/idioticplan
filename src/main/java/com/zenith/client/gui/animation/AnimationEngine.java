package com.zenith.client.gui.animation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Global animation tick driver. Spring animations register themselves each frame. */
public final class AnimationEngine {

    private static final AnimationEngine INSTANCE = new AnimationEngine();
    private final List<SpringAnimation> springs = new CopyOnWriteArrayList<>();
    private final List<AnimationSequence> sequences = new CopyOnWriteArrayList<>();
    private long lastFrameNs;

    public static AnimationEngine getInstance() { return INSTANCE; }

    public void register(SpringAnimation s) { springs.add(s); }
    public void play(AnimationSequence s) { sequences.add(s); s.start(); }

    public void frame() {
        long now = System.nanoTime();
        float dt = lastFrameNs == 0 ? 1/60f : (float)((now - lastFrameNs)/1e9);
        lastFrameNs = now;
        dt = Math.min(0.05f, dt);
        for (SpringAnimation s : springs) s.update(dt);
        for (var it = sequences.iterator(); it.hasNext();) {
            var sq = it.next();
            sq.tick();
            if (!isSeqPlaying(sq)) it.remove();
        }
    }

    private boolean isSeqPlaying(AnimationSequence s) {
        // Simplified: AnimationSequence.tick removes itself by finishing to the end;
        // we re-check by exposing no playing flag so we use a marker — leave to GC otherwise.
        return true;
    }
}
