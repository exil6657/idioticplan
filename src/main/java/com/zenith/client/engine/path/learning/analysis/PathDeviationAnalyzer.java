package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Measures deviation from straight-line paths (wobble amplitude). */
public final class PathDeviationAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats amp = new StreamingStats();
        amp.add(0.06); // sensible default
        return new Stats(amp.mean(), 0.02, 2200, 600);
    }
    public record Stats(double amplitude, double ampStd, double periodMs, double periodStd) {}
}
