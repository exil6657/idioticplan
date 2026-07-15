package com.zenith.client.mixin;

import com.zenith.client.core.ClientTickDispatcher;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.ClientTickEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the main client tick loop to dispatch {@link ClientTickEvent} and spoofs
 * the client brand to "vanilla" to stay invisible to server-side mod detection.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow private long clientTickCount;

    /** Start of tick — dispatch the ClientTickEvent (subscribers do the real work). */
    @Inject(method = "tick", at = @At("HEAD"))
    private void zenith$onTickHead(CallbackInfo ci) {
        ZenithEventBus.getInstance().post(new ClientTickEvent(clientTickCount));
    }

    /** Spoof client brand string — master rule §4: no server-visible branding. */
    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
    private void zenith$spoofBrand(CallbackInfoReturnable<String> ci) {
        // Return "vanilla" instead of "fabric" so servers/plugins cannot see the Fabric client brand.
        ci.setReturnValue("vanilla");
    }
}
