package com.zenith.client.engine.path;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.path.algorithm.PathNode;
import com.zenith.client.engine.path.humanizer.*;
import com.zenith.client.engine.path.movement.MovementSimulator;
import com.zenith.client.engine.path.postprocess.*;

import java.util.List;

/**
 * Runtime executor: follows a post-processed waypoint list, producing per-tick
 * movement inputs via the {@link MovementSimulator} while applying humanizers
 * (sprint variation, jump variation, direction noise, thinking pauses, stutter
 * step, obstacle recovery) and course re-computation when drift is detected.
 */
public final class PathExecutor {

    public enum State { IDLE, FOLLOWING, REPATHING, RECOVERING, ARRIVED, CANCELLED }

    private final MovementSimulator move = new MovementSimulator();
    private final SprintVariation sprintVar = new SprintVariation();
    private final JumpVariation jumpVar = new JumpVariation();
    private final DirectionNoise dirNoise = new DirectionNoise();
    private final ThinkingPauses pauses = new ThinkingPauses();
    private final StutterStep stutter = new StutterStep();
    private final ObstacleRecovery recovery = new ObstacleRecovery();
    private final CourseCorrector corrector = new CourseCorrector();
    private final ArrivalOvershoot overshoot = new ArrivalOvershoot();

    private List<NaturalDeviator.Waypoint> waypoints;
    private int currentIndex;
    private State state = State.IDLE;
    private double px, py, pz;
    private float yaw;
    private boolean onGround;

    public void start(List<NaturalDeviator.Waypoint> wps) {
        this.waypoints = wps;
        this.currentIndex = 0;
        this.state = State.FOLLOWING;
        sprintVar.reset(); jumpVar.reset(); pauses.reset(); stutter.reset(); recovery.clear();
    }

    public void cancel() { state = State.CANCELLED; }
    public State state() { return state; }

    /**
     * Tick the executor.
     * @return movement inputs for this tick, or null if idle
     */
    public MovementSimulator.MovementInput tick(double px, double py, double pz, float yaw, boolean onGround, long nowMs) {
        this.px = px; this.py = py; this.pz = pz; this.yaw = yaw; this.onGround = onGround;
        if (state != State.FOLLOWING || waypoints == null) return null;
        if (currentIndex >= waypoints.size()) { state = State.ARRIVED; return null; }
        NaturalDeviator.Waypoint wp = waypoints.get(currentIndex);

        // Distance check to advance.
        double dist = Math.hypot(wp.x() - px, wp.z() - pz);
        if (dist < 0.2d && py <= wp.y() + 0.5d) {
            currentIndex++;
            if (currentIndex >= waypoints.size()) { state = State.ARRIVED; return null; }
            wp = waypoints.get(currentIndex);
        }

        // Thinking pause.
        if (pauses.isPaused(nowMs)) return new MovementSimulator.MovementInput(0f,0f,false,false);

        // Obstacle recovery.
        ObstacleRecovery.RecoveryStage stage = recovery.tick(nowMs);
        if (recovery.active()) {
            return switch (stage) {
                case BACKOFF -> new MovementSimulator.MovementInput(-0.4f, 0f, false, false);
                case STRAFE  -> new MovementSimulator.MovementInput(0f, (nowMs % 200 < 100) ? 0.4f : -0.4f, false, false);
                case JUMP    -> new MovementSimulator.MovementInput(0.2f, 0f, true, false);
                case REPATH -> { state = State.REPATHING; yield null; }
                case NONE -> new MovementSimulator.MovementInput(0f,0f,false,false);
            };
        }

        boolean jumpQueued = wp.jumpHere() && dist < 1.0d;
        MovementSimulator.MovementInput mi = move.tick(px, py, pz, yaw, wp, onGround, jumpQueued, 50f);

        // Apply humanizer layers.
        boolean sprint = sprintVar.update(mi.sprint(), dist);
        float fwd = mi.forward();
        float str = mi.strafe() + dirNoise.sample(nowMs);
        fwd = stutter.applyForward(fwd, nowMs);
        if (Math.abs(fwd) < 0.02f && Math.abs(str) < 0.02f) sprint = false;

        return new MovementSimulator.MovementInput(fwd, MathUtils.clampf(str, -1f, 1f), mi.jump(), sprint);
    }

    public boolean needsRepath() { return state == State.REPATHING; }
    public void markRepathed() { state = State.FOLLOWING; }

    public MovementSimulator getMove() { return move; }
    public ObstacleRecovery recovery() { return recovery; }
    public CourseCorrector corrector() { return corrector; }
}
