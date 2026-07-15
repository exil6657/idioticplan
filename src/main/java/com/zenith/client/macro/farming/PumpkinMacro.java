package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PumpkinMacro extends AbstractFarmingMacro {

    public static final PumpkinMacro INSTANCE = new PumpkinMacro();
    private float startYaw;

    private PumpkinMacro() {}

    @Override public String id() { return "farming:pumpkin"; }
    @Override public String displayName() { return "Pumpkin Farming"; }
    @Override public String icon() { return "🎃"; }
    @Override public String skillFamily() { return "Farming"; }

    @Override protected void onStart() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        startYaw = p != null ? p.getYRot() : 0f;
        super.onStart();
    }

    @Override protected float targetYaw() { return startYaw; }

    @Override protected boolean cropMatcher(BlockState state) {
        return state.is(Blocks.PUMPKIN);
    }

    @Override public double destX() { var p = net.minecraft.client.Minecraft.getInstance().player; return p == null ? 0 : p.getX(); }
    @Override public double destY() { var p = net.minecraft.client.Minecraft.getInstance().player; return p == null ? 0 : p.getY(); }
    @Override public double destZ() { var p = net.minecraft.client.Minecraft.getInstance().player; return p == null ? 0 : p.getZ(); }
}
