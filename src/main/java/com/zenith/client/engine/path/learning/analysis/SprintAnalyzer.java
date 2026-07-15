package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Length of each contiguous sprint burst. */
public final class SprintAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats burst = new StreamingStats();
        long start = -1;
        MovementSample prev = null;
        for (MovementSample s : samples) {
            if (s.sprint && start == -1) start = s.t;
            else if (!s.sprint && start != -1 && prev != null) {
                burst.add(s.t - start);
                start = -1;
            }
            prev = s;
        }
        if (burst.count() == 0) { burst.add(750); }
        return new Stats(burst.mean(), burst.stddev());
    }
    public record Stats(double burstMs, double burstStdMs) {}
}
