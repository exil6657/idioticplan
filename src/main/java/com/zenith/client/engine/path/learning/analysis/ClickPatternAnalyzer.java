package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** CPS distribution while attacking / farming. Stub until input engine records clicks. */
public final class ClickPatternAnalyzer {
    public Stats analyze(List<MovementSample> samples) { return new Stats(8d, 1.2d); }
    public record Stats(double cpsMean, double cpsStd) {}
}
