package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class WheatMacro extends AbstractFarmingMacro {

    public static final WheatMacro INSTANCE = new WheatMacro();
    private float startYaw;
    private WheatMacro() {}

    @Override public String id() { return "farming:wheat"; }
    @Override public String displayName() { return "Wheat Farming"; }
    @Override public String icon() { return "🌾"; }
    @Override public String skillFamily() { return "Farming"; }

    @Override protected void onStart() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        startYaw = p != null ? p.getYRot() : 0f;
        super.onStart();
    }

    @Override protected float targetYaw() {
        if (returning) {
            float y = startYaw + 180f;
            if (y > 180f) y -= 360f;
            if (y < -180f) y += 360f;
            return y;
        }
        return startYaw;
    }

    @Override protected boolean cropMatcher(BlockState state) {
        if (!state.is(Blocks.WHEAT)) return false;
        try {
            IntegerProperty age = (IntegerProperty) state.getProperties().stream()
                    .filter(pr -> pr.getName().equals("age")).findFirst().orElse(null);
            if (age != null) return state.getValue(age) >= 7;
        } catch (Throwable ignored) {}
        return true;
    }

    @Override protected float rowLength() { return 120f; }
    @Override protected String expectedToolId() { return "THEORETICAL_HOE"; }
}
