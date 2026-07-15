package com.zenith.client.mixin;

import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.RenderHudEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fires {@link RenderHudEvent} after the vanilla HUD so Zenith panels can draw on top. */
@Mixin(Gui.class)
public abstract class MixinGui {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("RETURN"), require = 0)
    private void zenith$postRenderHud(GuiGraphics g, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        ZenithEventBus.getInstance().post(new RenderHudEvent(g, partialTick, w, h));
    }
}
