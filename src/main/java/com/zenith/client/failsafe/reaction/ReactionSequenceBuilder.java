package com.zenith.client.failsafe.reaction;

import com.zenith.client.failsafe.FailsafeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Builder for an ordered list of {@link ReactionAction}s that together form a
 * mistake-simulation routine ("oops, I got distracted, looking around...").
 *
 * <p>Different {@link FailsafeType} triggers wire up different sequences — a
 * player-nearby trigger looks very different from a ban screen.</p>
 */
public final class ReactionSequenceBuilder {

    private final List<Supplier<ReactionAction>> steps = new ArrayList<>();

    public static ReactionSequenceBuilder create() { return new ReactionSequenceBuilder(); }

    public ReactionSequenceBuilder then(Supplier<ReactionAction> actionFactory) {
        steps.add(actionFactory);
        return this;
    }

    public List<ReactionAction> build() {
        List<ReactionAction> out = new ArrayList<>(steps.size());
        for (Supplier<ReactionAction> s : steps) out.add(s.get());
        return out;
    }

    public boolean isEmpty() { return steps.isEmpty(); }
}
