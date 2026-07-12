package com.zenith.client.engine.path.reaction.reactions;

import com.zenith.client.engine.path.reaction.AnnoyedReactionEngine;
import com.zenith.client.engine.path.reaction.AnnoyedReactionType;

/** Base class for specific reactions. */
public abstract class AbstractReaction implements AnnoyedReactionEngine.Reaction {
    protected abstract AnnoyedReactionType type();
}
