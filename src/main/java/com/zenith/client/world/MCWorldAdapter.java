package com.zenith.client.world;

import com.zenith.client.engine.path.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;

/**
 * Live WorldAdapter reading from Minecraft.getInstance() ClientLevel. Installed
 * once the player joins a world; swapped out on disconnect.
 *
 * <p>26.1 Mojang names: BlockState.isSolid(), Block.isLava()/isWater()/isLadder()
 * are used directly. Raycast via level.clip().</p>
 */
public final class MCWorldAdapter implements WorldAdapter {

    public static final MCWorldAdapter INSTANCE = new MCWorldAdapter();
    private MCWorldAdapter() {}

    private Minecraft mc() { return Minecraft.getInstance(); }

    @Override
    public boolean isSolid(BlockPos p) {
        var level = mc().level;
        if (level == null) return false;
        return level.getBlockState(toMc(p)).isSolid();
    }

    @Override
    public boolean isPassable(BlockPos p) {
        var level = mc().level;
        if (level == null) return true;
        BlockState s = level.getBlockState(toMc(p));
        return s.isAir() || !s.canOcclude() && !s.blocksMotion();
    }

    @Override
    public boolean isClimbable(BlockPos p) {
        var level = mc().level;
        if (level == null) return false;
        return level.getBlockState(toMc(p)).isLadder(level, toMc(p), mc().player);
    }

    @Override public boolean isWater(BlockPos p) {
        var level = mc().level; if (level == null) return false;
        return level.getFluidState(toMc(p)).isSource() && level.getFluidState(toMc(p)).is(net.minecraft.tags.FluidTags.WATER);
    }
    @Override public boolean isLava(BlockPos p) {
        var level = mc().level; if (level == null) return false;
        return level.getFluidState(toMc(p)).is(net.minecraft.tags.FluidTags.LAVA);
    }

    @Override public String getBlockId(BlockPos p) {
        var level = mc().level;
        if (level == null) return "air";
        return level.getBlockState(toMc(p)).getBlock().toString();
    }

    @Override
    public boolean raycastClear(double x1, double y1, double z1, double x2, double y2, double z2) {
        var level = mc().level;
        if (level == null) return true;
        Vec3 start = new Vec3(x1, y1, z1);
        Vec3 end   = new Vec3(x2, y2, z2);
        var ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc().player);
        BlockHitResult res = level.clip(ctx);
        return res == null || res.getType() == BlockHitResult.Type.MISS;
    }

    @Override public BlockPos playerBlockPos() {
        var p = mc().player; return p == null ? new BlockPos(0,0,0) : BlockPos.of(p.getX(), p.getY(), p.getZ());
    }
    @Override public double playerX() { var p = mc().player; return p == null ? 0 : p.getX(); }
    @Override public double playerY() { var p = mc().player; return p == null ? 0 : p.getY(); }
    @Override public double playerZ() { var p = mc().player; return p == null ? 0 : p.getZ(); }
    @Override public float playerYaw() { var p = mc().player; return p == null ? 0 : p.getYRot(); }
    @Override public float playerPitch() { var p = mc().player; return p == null ? 0 : p.getXRot(); }
    @Override public boolean playerOnGround() { var p = mc().player; return p != null && p.onGround(); }
    @Override public boolean playerInScreen() { return mc().screen != null; }
    @Override public String openScreenTitle() {
        var s = mc().screen;
        if (s == null || s.getTitle() == null) return null;
        return s.getTitle().getString();
    }
    @Override public float playerHealth() { var p = mc().player; return p == null ? 20 : p.getHealth(); }
    @Override public int playerFood() { var p = mc().player; return p == null ? 20 : p.getFoodData().getFoodLevel(); }

    @Override public boolean playerReady() {
        var p = mc().player;
        return mc().level != null && p != null && p.isAlive();
    }

    @Override
    public boolean isSkyBlock() {
        // Scoreboard title check — Phase 6 will add the TabList + scoreboard parser.
        if (mc().level == null) return false;
        var sb = mc().level.getScoreboard();
        if (sb == null) return false;
        var obj = sb.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
        if (obj == null) return false;
        return obj.getDisplayName().getString().toUpperCase().contains("SKYBLOCK")
                || obj.getName().toUpperCase().contains("SB");
    }

    private static net.minecraft.core.BlockPos toMc(BlockPos p) {
        return new net.minecraft.core.BlockPos(p.x, p.y, p.z);
    }
}
