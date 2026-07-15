package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;

/** Fired each frame after the world is rendered (for world-space ESP/path visualisation). */
public class RenderWorldEvent extends ZenithEvent {
    public final float tickDelta;
    public final PoseStack poseStack;
    public final float partialTick;
    public RenderWorldEvent(PoseStack poseStack, float tickDelta) {
        this.poseStack = poseStack; this.tickDelta = tickDelta; this.partialTick = tickDelta;
    }
    @Override public boolean isCancellable() { return false; }
}
