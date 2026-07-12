package com.zenith.client.mixin;

import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.event.events.KeyInputEvent;
import com.zenith.client.keybind.KeybindManager;
import com.zenith.client.keybind.ZenithKeybinds;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts key presses so we can (a) fire {@link KeyInputEvent}, (b) fire our
 * own keybinds from {@link KeybindManager}, (c) allow subscribers (manual-input
 * detector, etc.) to suppress keys.
 */
@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onKeyPress(long window, int key, int scanCode, int action, int mods, CallbackInfo ci) {
        KeyInputEvent event = new KeyInputEvent(key, scanCode, action, mods);
        ZenithEventBus.getInstance().post(event);
        if (event.isCancelled()) { ci.cancel(); return; }
        // Route to KeybindManager for Zenith global binds.
        if (action == 1 || action == 2) { // PRESS or REPEAT
            for (ZenithKeybinds zb : ZenithKeybinds.values()) {
                if (matchesKey(zb, key)) KeybindManager.getInstance().onKeyPress(zb);
            }
        }
    }

    private static boolean matchesKey(ZenithKeybinds bind, int glfwKey) {
        // Phase 4: placeholder. Real mapping from GLFW name → code added when KeybindManager is fully wired.
        return false;
    }
}
