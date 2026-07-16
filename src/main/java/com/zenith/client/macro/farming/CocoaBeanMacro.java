package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class CocoaBeanMacro extends AbstractFarmingMacro {

    public static final CocoaBeanMacro INSTANCE = new CocoaBeanMacro();
    private float startYaw;
    private CocoaBeanMacro() {}

    @Override public String id() { return "farming:cocoa"; }
    @Override public String displayName() { return "Cocoa Bean Farming"; }
    @Override public String icon() { return "🍫"; }
    @Override public String skillFamily() { return "Farming"; }

    @Override protected void onStart() {
        var p = net.minecraft.client.Minecraft.getInstance().player;
        startYaw = p != null ? p.getYRot() : 0f;
        super.onStart();
    }

    @Override protected float targetYaw() {
        // Cocoa is farmed on side of jungle logs — yaw offset by ~60° depending on farm design
        if (returning) {
            float y = startYaw + 180f;
            if (y > 180f) y -= 360f;
            if (y < -180f) y += 360f;
            return y;
        }
        return startYaw;
    }

    @Override protected boolean cropMatcher(BlockState state) {
        if (!state.is(Blocks.COCOA)) return false;
        try {
            IntegerProperty age = (IntegerProperty) state.getProperties().stream()
                    .filter(pr -> pr.getName().equals("age")).findFirst().orElse(null);
            if (age != null) return state.getValue(age) >= 2;
        } catch (Throwable ignored) {}
        return true;
    }

    @Override protected float rowLength() { return 100f; }
    @Override protected String expectedToolId() { return "COCOA_CHOPPER"; }
}
