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

    @Override protected float targetYaw() {
        if (returning) {
            float yaw = startYaw + 180f;
            if (yaw > 180f) yaw -= 360f;
            if (yaw < -180f) yaw += 360f;
            return yaw;
        }
        return startYaw;
    }

    @Override protected boolean cropMatcher(BlockState state) {
        return state.is(Blocks.PUMPKIN);
    }

    @Override protected float rowLength() { return 120f; }
    @Override protected float rowSpacing() { return 2.5f; }
    @Override protected String expectedToolId() { return "PUMPKIN_DICER"; }
}
