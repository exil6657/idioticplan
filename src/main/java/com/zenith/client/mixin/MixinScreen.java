package com.zenith.client.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Prevents cursor reset on GUI close (NoCursorReset QoL — the vanilla snap-to-centre
 * is a detectable fingerprint when macros rapidly open/close menus). Phase 18 adds
 * the actual onClose() injection.
 */
@Mixin(Screen.class)
public abstract class MixinScreen {
}
