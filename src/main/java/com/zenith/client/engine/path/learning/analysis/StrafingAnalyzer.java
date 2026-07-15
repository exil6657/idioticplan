package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Strafing bias / distribution while moving. */
public final class StrafingAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats s = new StreamingStats();
        for (MovementSample sm : samples) s.add(sm.strafe);
        return new Stats(s.std(), s.mean());
    }
    public record Stats(double strafeStd, double strafeMean) {}
}
