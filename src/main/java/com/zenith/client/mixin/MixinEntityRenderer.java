package com.zenith.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Entity render filter (ESP / ghost entity hide / goblin form hider).
 *
 * <p>Phase 4: no-op. Phase 18 (ESP/Render modules) injects the shouldRender()
 * equivalent for 26.1. Method names differ pre/post-26.1 so this is left as
 * a stub mixin so the mixin config continues to resolve cleanly.</p>
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderer {
}
