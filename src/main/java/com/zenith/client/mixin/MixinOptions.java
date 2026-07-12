package com.zenith.client.mixin;

import com.zenith.client.mixin.accessor.OptionsAccessor;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Accessors for sensitivity and keybinds are defined on the
 * {@link OptionsAccessor} interface (see accessor/ subpackage). This mixin
 * itself just needs to target Options so the accessor can apply.
 */
@Mixin(Options.class)
public abstract class MixinOptions implements OptionsAccessor {

    @Shadow public double sensitivity;

    @Override
    public double zenith$getSensitivity() { return sensitivity; }
}
