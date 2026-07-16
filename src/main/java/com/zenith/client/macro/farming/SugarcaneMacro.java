package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Sugarcane is a column — break any block in the column. */
public final class SugarcaneMacro extends AbstractFarmingMacro {

    public static final SugarcaneMacro INSTANCE = new SugarcaneMacro();
    private float startYaw;
    private SugarcaneMacro() {}

    @Override public String id() { return "farming:sugarcane"; }
    @Override public String displayName() { return "Sugarcane Farming"; }
    @Override public String icon() { return "🎋"; }
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
        return state.is(Blocks.SUGAR_CANE);
    }

    @Override protected float rowLength() { return 120f; }
    @Override protected float rowSpacing() { return 3f; }
}
