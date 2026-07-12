package com.zenith.client.engine.path.learning.analysis;

import com.zenith.client.engine.path.learning.MovementSample;
import java.util.List;

/** Analyzes angular velocity and overshoot during turns. */
public final class TurnAnalyzer {
    public Stats analyze(List<MovementSample> samples) {
        StreamingStats vel = new StreamingStats();
        double peakOvershoot = 0;
        int overshootEvents = 0;
        double lastYaw = 0;
        boolean tracking = false;
        for (MovementSample s : samples) {
            float av = Math.abs(s.yawVel);
            if (av > 15) {
                vel.add(av);
                tracking = true;
            } else if (tracking && av < 5) {
                // capture overshoot
                peakOvershoot += Math.abs(s.yaw - lastYaw);
                overshootEvents++;
                tracking = false;
            }
            lastYaw = s.yaw;
        }
        return new Stats(vel.mean(), vel.stddev(),
                overshootEvents == 0 ? 0 : peakOvershoot / overshootEvents,
                vel.count() == 0 ? 0 : overshootEvents / (double) vel.count());
    }
    public record Stats(double meanDegPerSec, double stdDegPerSec, double overshootMeanDeg, double overshootChance) {}
}
