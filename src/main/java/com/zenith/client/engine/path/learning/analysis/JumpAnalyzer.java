package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Analyzes jump lead timing and air-strafe bias. */
public final class JumpAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats lead = new StreamingStats();
        StreamingStats air = new StreamingStats();
        // Stub — Phase 7 fills in jump-edge detection via PositionTracker.
        lead.add(85); air.add(0.12);
        return new Stats(lead.mean(), lead.stddev(), air.mean(), air.stddev());
    }
    public record Stats(double leadMs, double leadStdMs, double airStrafe, double airStrafeStd) {}
}
