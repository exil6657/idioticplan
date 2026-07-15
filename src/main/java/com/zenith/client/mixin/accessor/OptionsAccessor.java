package com.zenith.client.mixin.accessor;

/**
 * Accessor interface for MixinOptions. The concrete accessor methods live on
 * the MixinOptions class; this interface provides a type-safe way for other
 * code to get sensitivity/keybind values without referencing mixin internals.
 */
public interface OptionsAccessor {
    double zenith$getSensitivity();
}
