package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** How often the player looks around / glances while moving. */
public final class LookPatternAnalyzer {
    public Stats analyze(List<MovementSample> samples) { return new Stats(0.55d, 250d); }
    public record Stats(double glanceChance, double glanceDurationMs) {}
}
