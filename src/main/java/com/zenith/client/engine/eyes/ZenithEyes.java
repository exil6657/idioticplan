package com.zenith.client.engine.eyes;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.eyes.behavior.CameraWander;
import com.zenith.client.engine.eyes.behavior.EnvironmentAwareness;
import com.zenith.client.engine.eyes.behavior.IdleWander;
import com.zenith.client.engine.eyes.behavior.WalkingGlance;
import com.zenith.client.engine.eyes.humanizer.ContextualBehavior;

/**
 * Master controller for camera rotations.
 *
 * <p>All look/turn operations in Zenith flow through {@link #requestRotation(RotationRequest)}.
 * Direct mouse/movement calls are forbidden by master rule §2 ("no instant rotations").
 * The engine applies curves, acceleration, overshoots, jitter, hesitations,
 * tick quantisation, micro-corrections, and idle/walk behaviours before
 * producing final yaw/pitch values each frame.</p>
 *
 * <p>The final yaw/pitch are written via MixinCamera (Phase 3) directly into
 * {@code Camera#setRotation} before rendering — we don't call Minecraft's
 * {@code MouseHandler.turn()} directly because that doubles sensitivity. The
 * {@link MouseSensitivitySimulator} lets us work in meaningful units.</p>
 */
public final class ZenithEyes {

    private static ZenithEyes instance;

    private boolean enabled = true;
    private boolean freecam = false;

    private final RotationQueue queue = new RotationQueue();
    private final RotationExecutor executor = new RotationExecutor();
    private final ContextualBehavior ctx = new ContextualBehavior();

    // Behaviours
    private final CameraWander cameraWander = new CameraWander();
    private final IdleWander idleWander = new IdleWander();
    private final WalkingGlance walkingGlance = new WalkingGlance();
    private final EnvironmentAwareness env = new EnvironmentAwareness();

    // Current visible rotation (degrees).
    private float currentYaw;
    private float currentPitch;
    private float lastAppliedYaw;
    private float lastAppliedPitch;

    // Idle tracking.
    private long lastInputAt;

    // Debug
    private String lastTag = "";

    private ZenithEyes() {}

    public static ZenithEyes getInstance() {
        if (instance == null) instance = new ZenithEyes();
        return instance;
    }

    public void init() {
        RotationProfileRegistry.getInstance(); // ensure defaults registered
        ZenithClient.LOGGER.info("[ZenithEyes] Initialized.");
    }

    public void setEnabled(boolean v) { this.enabled = v; if (!v) queue.clear(); }
    public boolean isEnabled() { return enabled; }

    public void setFreecam(boolean v) { this.freecam = v; }
    public boolean isFreecam() { return freecam; }

    /** Submit a rotation request. @return the scheduled request. */
    public RotationRequest requestRotation(RotationRequest req) {
        if (req == null) return null;
        if (!enabled) {
            if (req.callback != null) req.callback.complete(false, "eyes disabled");
            return null;
        }
        if (freecam) {
            // Don't rotate the camera in freecam mode — freecam controls its own.
            if (req.callback != null) req.callback.complete(false, "freecam");
            return null;
        }
        return queue.submit(req);
    }

    /** Shorthand: fire-and-forget snap to a target. */
    public RotationRequest lookAt(float yaw, float pitch) {
        return requestRotation(RotationRequest.builder()
                .yaw(yaw).pitch(pitch)
                .priority(RotationRequest.Priority.MACRO)
                .tag("lookAt")
                .build());
    }

    /** Called from MixinCamera before the camera picks up rotation; returns the desired [yaw, pitch]. */
    public float[] onCameraRender(float partialTicks) {
        long nowMs = System.currentTimeMillis();
        float dtMs = 50f; // ~20 tps default; Phase 3 will pass real frame delta.

        // Promote a queued request if executor is idle.
        if (!executor.isRunning() && !queue.hasCurrent()) {
            RotationRequest next = queue.pollNext();
            if (next != null) {
                executor.start(next, currentYaw, currentPitch, ctx);
                queue.setCurrent(next);
                lastTag = next.tag != null ? next.tag : "";
            }
        }

        if (executor.isRunning()) {
            float[] out = executor.step(currentYaw, currentPitch, nowMs, dtMs);
            currentYaw = wrapYaw(out[0]);
            currentPitch = MathUtils.clampf(out[1], -90f, 90f);
            if (!executor.isRunning()) queue.completeCurrent();
        } else {
            // Behaviours when idle.
            applyBehaviours(nowMs);
        }

        return new float[]{currentYaw, currentPitch};
    }

    private void applyBehaviours(long nowMs) {
        // Behaviours only run when enabled and not in freecam/menu.
        if (!enabled || freecam || ctx.isInMenu) return;
        boolean idleForLong = (nowMs - lastInputAt) > 4_000;

        if (ctx.isMoving) {
            RotationRequest g = walkingGlance.maybeGlance(currentYaw, currentYaw, true, nowMs);
            if (g != null) requestRotation(g);
            return;
        }
        if (idleForLong) {
            RotationRequest g = idleWander.maybeRequest(currentYaw, currentPitch, nowMs - lastInputAt);
            if (g != null) requestRotation(g);
            return;
        }
        RotationRequest e = env.maybeLook(nowMs);
        if (e != null) { requestRotation(e); return; }
        // Default micro-wander (camera breathing).
        float[] o = cameraWander.sample(nowMs);
        currentYaw = wrapYaw(currentYaw + o[0] * 0.005f);
        currentPitch = MathUtils.clampf(currentPitch + o[1] * 0.005f, -90f, 90f);
    }

    /** Sync current rotation from the game (called each tick by MixinCamera). */
    public void syncFromGame(float yaw, float pitch) {
        this.currentYaw = wrapYaw(yaw);
        this.currentPitch = MathUtils.clampf(pitch, -90f, 90f);
        this.lastInputAt = System.currentTimeMillis();
    }

    public EnvironmentAwareness environment() { return env; }

    public RotationDebugData debugData() {
        RotationRequest cur = queue.peekCurrent();
        return new RotationDebugData(
                executor.isRunning() ? "EXECUTING" : (queue.queuedCount() > 0 ? "QUEUED" : "IDLE"),
                cur != null ? (cur.tag == null ? cur.priority.name() : cur.tag) : lastTag,
                cur != null && cur.profileId != null ? cur.profileId : "default",
                cur != null ? cur.targetYaw : currentYaw,
                cur != null ? cur.targetPitch : currentPitch,
                currentYaw, currentPitch,
                (float) executor.getAngularVelocity(),
                executor.getProgress(),
                queue.queuedCount(),
                executor.isOvershooting(), executor.isCorrecting(), executor.isHesitating(),
                0f,
                cur != null ? cur.priority.name() : "NONE"
        );
    }

    private static float wrapYaw(float yaw) {
        yaw %= 360f;
        if (yaw >= 180f) yaw -= 360f;
        if (yaw < -180f) yaw += 360f;
        return yaw;
    }
}
