package com.zenith.client.world;

import com.zenith.client.engine.path.BlockPos;

/** No-op adapter used before the player joins a world. Every query returns safe defaults. */
final class EmptyWorldAdapter implements WorldAdapter {
    static final EmptyWorldAdapter INSTANCE = new EmptyWorldAdapter();

    @Override public boolean isSolid(BlockPos p) { return false; }
    @Override public boolean isPassable(BlockPos p) { return true; }
    @Override public boolean isClimbable(BlockPos p) { return false; }
    @Override public boolean isWater(BlockPos p) { return false; }
    @Override public boolean isLava(BlockPos p) { return false; }
    @Override public String getBlockId(BlockPos p) { return "air"; }
    @Override public boolean raycastClear(double x1,double y1,double z1,double x2,double y2,double z2){ return true; }
    @Override public BlockPos playerBlockPos() { return new BlockPos(0,0,0); }
    @Override public double playerX() { return 0; }
    @Override public double playerY() { return 0; }
    @Override public double playerZ() { return 0; }
    @Override public float playerYaw() { return 0; }
    @Override public float playerPitch() { return 0; }
    @Override public boolean playerOnGround() { return true; }
    @Override public boolean playerInScreen() { return false; }
    @Override public String openScreenTitle() { return null; }
    @Override public float playerHealth() { return 20; }
    @Override public int playerFood() { return 20; }
    @Override public boolean playerReady() { return false; }
    @Override public boolean isSkyBlock() { return false; }
}
