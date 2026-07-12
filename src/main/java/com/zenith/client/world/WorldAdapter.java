package com.zenith.client.world;

import com.zenith.client.engine.path.BlockPos;

/**
 * Abstraction over Minecraft's ClientLevel/BlockState/Entity access. All engine code
 * (pathfinding, raycasts, interact checks) reads world state via this interface, so
 * the engine never imports MC classes directly.
 *
 * <p>The default implementation ({@link MCWorldAdapter}) delegates to Minecraft's
 * {@code ClientLevel} and is installed once the player joins a world in Phase 6.
 * Tests use a fake implementation.</p>
 */
public interface WorldAdapter {

    /** @return true if the block at p is fully-solid (can stand on). */
    boolean isSolid(BlockPos p);

    /** @return true if the block is fully passable (air, plants, torches, etc.). */
    boolean isPassable(BlockPos p);

    /** @return true if the block is a climbable (ladder, vine, scaffolding). */
    boolean isClimbable(BlockPos p);

    /** @return true if the block is water. */
    boolean isWater(BlockPos p);

    /** @return true if the block is lava. */
    boolean isLava(BlockPos p);

    /** @return block id/name at p (SkyBlock id when known, MC identifier otherwise). */
    String getBlockId(BlockPos p);

    /**
     * Line-of-sight raycast from (x1,y1,z1) to (x2,y2,z2).
     * @return true if no solid block intersects the line.
     */
    boolean raycastClear(double x1, double y1, double z1, double x2, double y2, double z2);

    /** @return the player's current block position. */
    BlockPos playerBlockPos();

    /** @return player feet X. */
    double playerX();
    /** @return player feet Y. */
    double playerY();
    /** @return player feet Z. */
    double playerZ();

    /** @return player yaw (degrees). */
    float playerYaw();

    /** @return player pitch (degrees). */
    float playerPitch();

    /** @return true if player is on ground. */
    boolean playerOnGround();

    /** @return true if player is in a GUI/Screen. */
    boolean playerInScreen();

    /** @return open screen title, or null if no screen. */
    String openScreenTitle();

    /** Player health (0..20). */
    float playerHealth();

    /** Player food (0..20). */
    int playerFood();

    /** True if the player is alive and not in a loading screen. */
    boolean playerReady();

    /** @return true if in SkyBlock (scoreboard contains "SKYBLOCK"). */
    boolean isSkyBlock();
}
