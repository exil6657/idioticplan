package com.zenith.client.engine.path.movement;

/**
 * Computes Minecraft-style forward/strafe values from desired motion.
 *
 * <p>Minecraft movement inputs are (forward, strafe) ∈ [-1,1]. This controller
 * converts a world-space direction vector and player yaw into per-tick inputs,
 * applying diagonal normalisation so forward+strafe doesn't move at √2 speed.</p>
 */
public final class StrafeController {

    private float forward, strafe;

    /**
     * @param dirX world-space desired X velocity (normalised)
     * @param dirZ world-space desired Z velocity (normalised)
     * @param playerYaw player's current yaw (degrees)
     */
    public void face(double dirX, double dirZ, float playerYaw) {
        double yawRad = Math.toRadians(playerYaw);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        // In MC, +Z is forward, -X is left, +X is right. Convert world dir to local.
        double localForward =  dirX * -sin + dirZ * cos;
        double localStrafe  =  dirX *  cos + dirZ * sin;
        double len = Math.hypot(localForward, localStrafe);
        if (len > 1.0d) { localForward /= len; localStrafe /= len; }
        this.forward = (float) localForward;
        this.strafe  = (float) localStrafe;
    }

    /** Stop all movement. */
    public void halt() { forward = 0f; strafe = 0f; }

    public float getForward() { return forward; }
    public float getStrafe()  { return strafe; }
}
