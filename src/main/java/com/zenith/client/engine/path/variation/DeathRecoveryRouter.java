package com.zenith.client.engine.path.variation;

import com.zenith.client.engine.path.BlockPos;

/**
 * After a death/respawn, returns the player to the last known farming/mining
 * position using a slightly different route each time (so repeated deaths don't
 * trace a perfect line back).
 */
public final class DeathRecoveryRouter {

    private BlockPos lastDeathPos;
    private BlockPos lastActivePos;

    public void recordDeath(BlockPos pos) { this.lastDeathPos = pos; }
    public void recordActive(BlockPos pos) { this.lastActivePos = pos; }

    public boolean hasRecoveryTarget() { return lastActivePos != null; }

    public BlockPos getRecoveryTarget() { return lastActivePos; }

    public void clear() { lastDeathPos = null; lastActivePos = null; }
}
