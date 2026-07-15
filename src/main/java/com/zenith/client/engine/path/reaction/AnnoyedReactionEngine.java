package com.zenith.client.engine.path.reaction;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * When the macro encounters a small error (GUI didn't open, item is wrong), this
 * engine occasionally plays a visible human-like reaction (head shake, mouse wiggle,
 * brief pause) to prevent the "perfect robot" signature.
 *
 * <p>Reactions are lightweight and never block the macro for long; the macro
 * proceeds with its retry immediately after scheduling a reaction.</p>
 */
public final class AnnoyedReactionEngine {

    public interface Reaction {
        void fire();
        default long durationMs() { return 200; }
    }

    private final AnnoyedReactionConfig config = new AnnoyedReactionConfig();
    private final Map<AnnoyedReactionType, Reaction> handlers = new EnumMap<>(AnnoyedReactionType.class);
    private long lastReactionAt;
    private int errorStreak;

    public AnnoyedReactionConfig config() { return config; }

    public void register(AnnoyedReactionType type, Reaction r) { handlers.put(type, r); }

    public void onError() {
        errorStreak++;
        long now = System.currentTimeMillis();
        if (now - lastReactionAt < config.cooldownMs) return;
        float chance = config.baseChance;
        if (errorStreak >= config.dramaThreshold) chance = Math.min(0.9f, chance + 0.2f);
        if (ThreadLocalRandom.current().nextFloat() > chance) return;
        AnnoyedReactionType t = AnnoyedReactionType.values()[ThreadLocalRandom.current().nextInt(AnnoyedReactionType.values().length)];
        Reaction r = handlers.get(t);
        if (r != null) {
            try { r.fire(); } catch (Throwable ignored) {}
            lastReactionAt = now;
        }
    }

    public void resetStreak() { errorStreak = 0; }
}
