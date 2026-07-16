package com.zenith.client.macro.farming;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Melon farming macro — walks a straight row breaking melon blocks.
 * Now has proper U-turn (AbstractFarmingMacro state machine), anchor capture
 * for REPATH, and correct yaw math.
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

    @Override protected float targetYaw() {
        // When returning, inherited logic already flips, but we can also add manual.
        // AbstractFarmingMacro handles flip via 'returning' flag in RESYNC/FORWARD.
        if (returning) {
            float yaw = startYaw + 180f;
            if (yaw > 180f) yaw -= 360f;
            if (yaw < -180f) yaw += 360f;
            return yaw;
        }
        return startYaw;
    }

    @Override protected boolean cropMatcher(BlockState state) {
        return state.is(Blocks.MELON);
    }

    @Override protected float rowLength() { return 120f; } // user-configurable later
    @Override protected float rowSpacing() { return 2.5f; }
    @Override protected String expectedToolId() { return "MELON_DICER"; }
}
