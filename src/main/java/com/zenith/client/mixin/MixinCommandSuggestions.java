package com.zenith.client.mixin;

import com.zenith.client.command.CommandCompleter;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Hooks into CommandSuggestions to replace tab completion for ".z"-prefixed
 * chat with our own client-side completer. We {@code require=0} to avoid
 * breakage if the method name shifts in a future 26.x build.
 */
@Mixin(value = CommandSuggestions.class, priority = 900)
public abstract class MixinCommandSuggestions {

    @Shadow @Final EditBox input;
    @Shadow private @Final List<String> commandUsage;
    @Shadow @Final private List<String> rawCommandUsage;
    @Shadow private CompletableFuture<List<String>> pendingSuggestions;

    @Shadow protected abstract void fillUsage(List<String> list, String currentPrefix, boolean bl);

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true, require = 0)
    private void zenith$onUpdateCommandInfo(CallbackInfo ci) {
        String text = input.getValue();
        if (CommandCompleter.getInstance().isDotCommand(text)) {
            // Consume the normal vanilla request and replace with our suggestions.
            commandUsage.clear();
            rawCommandUsage.clear();
            List<String> sug = CommandCompleter.getInstance().complete(text);
            if (sug == null) sug = new ArrayList<>();
            fillUsage(commandUsage, text, false);
            for (String s : sug) commandUsage.add(s);
            pendingSuggestions = CompletableFuture.completedFuture(sug);
            ci.cancel();
        }
    }
}
