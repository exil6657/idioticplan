package com.zenith.client.engine.path.movement;

import com.zenith.client.engine.path.postprocess.NaturalDeviator;

/**
 * Per-tick movement planner: given the next target waypoint, player position,
 * and player yaw, outputs (forward, strafe, jump, sprint) inputs that the
 * input engine will translate to key presses.
 *
 * <p>Movement is NOT done via teleport/velocity packets (master rule §3); we
 * only ever press keys and look at the target via ZenithEyes.</p>
 */
public final class MovementSimulator {

    private final StrafeController strafe = new StrafeController();
    private final SprintController sprintCtl = new SprintController();

    private float lastForward, lastStrafe;

    /**
     * @param px,pz           player position XZ
     * @param py              player Y (feet)
     * @param yaw             player yaw (degrees)
     * @param target          next waypoint
     * @param onGround        whether the player is currently on ground
     * @param jumpAt          true if we should jump now (jump pad, step up, etc.)
     * @param dtMs            tick delta ms
     * @return movement inputs to apply this tick
     */
    public MovementInput tick(double px, double py, double pz, float yaw,
                              NaturalDeviator.Waypoint target,
                              boolean onGround, boolean jumpAt, float dtMs) {
        double dx = target.x() - px;
        double dz = target.z() - pz;
        double dist = Math.hypot(dx, dz);

        if (dist < 0.05d && !target.jumpHere()) {
            strafe.halt();
            return new MovementInput(0f, 0f, false, false);
        }

        double dirX = dx / dist;
        double dirZ = dz / dist;
        strafe.face(dirX, dirZ, yaw);

        // Angle between look direction and movement direction (for sprint gating and look).
        float fwd = strafe.getForward();
        float str = strafe.getStrafe();

        // Smooth inputs — human key presses don't go 0→1 instantly.
        float smoothing = 0.35f;
        fwd = lastForward + (fwd - lastForward) * smoothing;
        str = lastStrafe  + (str - lastStrafe)  * smoothing;
        lastForward = fwd; lastStrafe = str;

        // Sprint logic: sprint on long straight segments.
        boolean sprint = sprintCtl.shouldSprint(dist, (float) Math.toDegrees(Math.atan2(-dirX, dirZ)) - yaw,
                jumpAt, System.currentTimeMillis());

        boolean jump = jumpAt && onGround;

        return new MovementInput(fwd, str, jump, sprint);
    }

    public StrafeController getStrafe() { return strafe; }
    public SprintController getSprint() { return sprintCtl; }

    public void halt() {
        strafe.halt();
        sprintCtl.stop();
        lastForward = 0f; lastStrafe = 0f;
    }

    /** Movement input package consumed by the InputEngine. */
    public record MovementInput(float forward, float strafe, boolean jump, boolean sprint) {}
}
