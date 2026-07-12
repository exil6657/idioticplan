package com.zenith.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

/** Block-render control (Xray / thyst/goblin/mineshaft hiders / NoRender). Phase 18 fills in. */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
}
