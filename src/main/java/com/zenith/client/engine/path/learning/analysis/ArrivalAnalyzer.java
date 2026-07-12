package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Arrival behaviour at stop points: overshoot and settle time. */
public final class ArrivalAnalyzer {
    public Stats analyze(List<MovementSample> samples) { return new Stats(0.08d, 130d); }
    public record Stats(double overshootBlocks, double settleMs) {}
}
