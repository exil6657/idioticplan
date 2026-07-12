package com.zenith.client.mixin;

import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.MouseDeltaEvent;
import com.zenith.client.core.event.events.MouseInputEvent;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures raw mouse motion and button events. Delta event allows injecting
 * synthetic movement (used by ZenithEyes / MovementRecorder). Button events
 * feed the click humanizer and manual-input detection.
 */
@Mixin(MouseHandler.class)
public abstract class MixinMouse {

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onMouseMove(long window, double dx, double dy, CallbackInfo ci) {
        MouseDeltaEvent event = new MouseDeltaEvent(dx, dy);
        ZenithEventBus.getInstance().post(event);
        // If dx/dy were modified by subscribers, we'd need to write back; for Phase 4
        // we let the original values through so nothing breaks. Synthetic injection
        // is handled by writing accumulatedDX/accumulatedDY via MouseAccessor (Phase 4).
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        MouseInputEvent event = new MouseInputEvent(button, action, mods);
        ZenithEventBus.getInstance().post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
