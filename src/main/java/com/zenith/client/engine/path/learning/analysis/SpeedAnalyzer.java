package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;

import java.util.List;

/** Extracts walk/sprint speed distribution from samples. */
public final class SpeedAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats walk = new StreamingStats();
        StreamingStats sprint = new StreamingStats();
        for (MovementSample s : samples) {
            if (s.sprint) sprint.add(s.speed);
            else if (Math.abs(s.forward) > 0.05f) walk.add(s.speed);
        }
        return new Stats(walk.mean(), walk.stddev(), sprint.mean(), sprint.stddev());
    }
    public record Stats(double walkMean, double walkStd, double sprintMean, double sprintStd) {}
}
