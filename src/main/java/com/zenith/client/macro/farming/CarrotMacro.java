package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Carrot farming — replantable crop. Requires seeds in hand and right-click replant after break.
 * Phase 13: replant logic lives in AbstractFarmingMacro subclass override (not yet; placeholder).
 */
public final class CarrotMacro extends AbstractFarmingMacro {

    public static final CarrotMacro INSTANCE = new CarrotMacro();
    private float startYaw;

    private CarrotMacro() {}

    @Override public String id() { return "farming:carrot"; }
    @Override public String displayName() { return "Carrot Farming"; }
    @Override public String icon() { return "🥕"; }
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
        if (!state.is(Blocks.CARROTS)) return false;
        // Fully grown if age 7
        try {
            IntegerProperty ageProp = (IntegerProperty) state.getProperties().stream()
                    .filter(pr -> pr.getName().equals("age")).findFirst().orElse(null);
            if (ageProp != null) {
                int age = state.getValue(ageProp);
                return age >= 7;
            }
        } catch (Throwable ignored) {}
        return true;
    }

    @Override protected float rowLength() { return 120f; }
    @Override protected String expectedToolId() { return "THEORETICAL_HOE"; }
}
