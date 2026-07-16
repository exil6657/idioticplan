package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class MushroomMacro extends AbstractFarmingMacro {

    public static final MushroomMacro INSTANCE = new MushroomMacro();
    private float startYaw;
    private MushroomMacro() {}

    @Override public String id() { return "farming:mushroom"; }
    @Override public String displayName() { return "Mushroom Farming"; }
    @Override public String icon() { return "🍄"; }
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
        return state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.MUSHROOM_STEM) || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK);
    }

    @Override protected float rowLength() { return 100f; }
}
