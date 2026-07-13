package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Melon farming macro — walks a straight row breaking melon blocks. The user
 * should start facing the row; targetYaw() uses the player's current yaw on
 * start. Real row-reversal / turn logic comes in Phase 13.
 */
public final class MelonMacro extends AbstractFarmingMacro {

    public static final MelonMacro INSTANCE = new MelonMacro();
    private float startYaw;

    private MelonMacro() {}

    @Override public String id() { return "farming:melon"; }
    @Override public String displayName() { return "Melon Farming"; }
    @Override public String icon() { return "🍉"; }
    @Override public String skillFamily() { return "Farming"; }

    @Override protected void onStart() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        startYaw = p != null ? p.getYRot() : 0f;
        super.onStart();
    }

    @Override protected float targetYaw() { return startYaw; }

    @Override protected boolean cropMatcher(BlockState state) {
        return state.is(Blocks.MELON);
    }

    @Override public double destX() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p == null ? 0 : p.getX();
    }
    @Override public double destY() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p == null ? 0 : p.getY();
    }
    @Override public double destZ() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        return p == null ? 0 : p.getZ();
    }
}
