package com.zenith.client.engine.path.reaction;

/** Tuning for the annoyed reaction engine. */
public class AnnoyedReactionConfig {
    /** Probability (0..1) of performing a reaction on each error event. */
    public float baseChance = 0.45f;
    /** Minimum ms between reactions. */
    public long cooldownMs = 3500;
    /** How many errors in a row before reactions become more dramatic. */
    public int dramaThreshold = 3;
}
