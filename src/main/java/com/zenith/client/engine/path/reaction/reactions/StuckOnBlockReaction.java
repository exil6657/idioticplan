package com.zenith.client.engine.path.reaction.reactions;

import com.zenith.client.ZenithClient;
import com.zenith.client.engine.path.reaction.AnnoyedReactionEngine;
import com.zenith.client.engine.path.reaction.AnnoyedReactionType;

/** StuckOnBlockReaction — triggered when the corresponding error occurs. */
public class StuckOnBlockReaction extends AbstractReaction {

    @Override protected AnnoyedReactionType type() { return AnnoyedReactionType.SMALL_WIGGLE; }

    @Override public void fire() {
        // The reaction implementation fires (a) a brief input wiggle (e.g. left+right strafe),
        // (b) a short camera shake via ZenithEyes, or (c) a tick of delay.
        // Phase 6 wires concrete input/camera calls; Phase 2 just logs so the engine is exercised.
        ZenithClient.LOGGER.debug("[Annoyed] Fired reaction: {}", getClass().getSimpleName());
    }

    @Override public long durationMs() { return 220; }
}
