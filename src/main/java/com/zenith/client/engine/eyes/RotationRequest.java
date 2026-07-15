package com.zenith.client.engine.eyes;

/**
 * A request to smoothly rotate the camera to look at a target.
 *
 * <p>Priority determines queue ordering: {@link Priority#FAILSAFE} (failsafe warp
 * out) interrupts everything; {@link Priority#MACRO} (macro path look-at) is
 * queued after COMBAT/MACRO_TICK; {@link Priority#BACKGROUND} (idle eye wander)
 * is lowest and may be evicted.</p>
 */
public final class RotationRequest {

    public enum Priority {
        BACKGROUND(0),
        WANDER(10),
        MACRO(20),
        COMBAT(30),
        MACRO_TICK(40),
        FAILSAFE(100);

        public final int value;
        Priority(int v) { this.value = v; }
    }

    /** Target yaw (degrees, -180..180). */
    public final float targetYaw;
    /** Target pitch (degrees, -90..90). */
    public final float targetPitch;

    public final Priority priority;

    /** Desired time to complete the rotation in milliseconds (soft target; curves may over/undershoot). */
    public final long durationMs;

    /** Rotation profile id to use; null = registry default. */
    public final String profileId;

    /** Optional tag for debug/Brain View. */
    public final String tag;

    /** If true, follow moving target by re-querying each tick via a supplier (set externally). */
    public final boolean tracking;

    /** Whether this request may be preempted by higher priority. */
    public final boolean preemptible;

    /** Callback invoked when rotation completes, or if cancelled with reason=null/error. */
    public volatile RotationCallback callback;

    public long submittedAt;
    public long startedAt;
    public volatile boolean cancelled;
    public volatile String cancelReason;

    public RotationRequest(float targetYaw, float targetPitch, Priority priority,
                           long durationMs, String profileId, String tag,
                           boolean tracking, boolean preemptible,
                           RotationCallback callback) {
        this.targetYaw = targetYaw;
        this.targetPitch = targetPitch;
        this.priority = priority;
        this.durationMs = durationMs;
        this.profileId = profileId;
        this.tag = tag;
        this.tracking = tracking;
        this.preemptible = preemptible;
        this.callback = callback;
    }

    public static Builder builder() { return new Builder(); }

    @FunctionalInterface
    public interface RotationCallback {
        void complete(boolean success, String reason);
    }

    public static final class Builder {
        private float yaw, pitch;
        private Priority priority = Priority.MACRO;
        private long durationMs = 180;
        private String profileId;
        private String tag;
        private boolean tracking;
        private boolean preemptible = true;
        private RotationCallback callback;

        public Builder yaw(float v)    { this.yaw = v; return this; }
        public Builder pitch(float v)  { this.pitch = v; return this; }
        public Builder priority(Priority p) { this.priority = p; return this; }
        public Builder durationMs(long d)  { this.durationMs = d; return this; }
        public Builder profile(String id)  { this.profileId = id; return this; }
        public Builder tag(String t)       { this.tag = t; return this; }
        public Builder tracking(boolean v) { this.tracking = v; return this; }
        public Builder preemptible(boolean v) { this.preemptible = v; return this; }
        public Builder onComplete(RotationCallback cb) { this.callback = cb; return this; }

        public RotationRequest build() {
            return new RotationRequest(yaw, pitch, priority, durationMs, profileId, tag, tracking, preemptible, callback);
        }
    }
}
