package com.zenith.client.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder marker for chat-screen extensions. The real work for dot-command
 * completion happens in {@link MixinCommandSuggestions}; this mixin is kept so
 * future chat-screen hooks (coloured command prefixes, dot-command formatting)
 * have a single target.
 */
@Mixin(value = ChatScreen.class, priority = 900)
public abstract class MixinChatScreen {
}
