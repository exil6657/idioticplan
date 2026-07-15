package com.zenith.client.engine.path.speed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregates {@link SpeedSource}s to compute effective movement speed.
 *
 * <p>Phase 2: registers default sources; Phase 6 ties the sources to inventory
 * checks. We always apply BASE_WALK_BPS and the sprint multiplier when
 * sprinting.</p>
 */
public final class SpeedDetector {

    private static SpeedDetector instance;

    private final List<SpeedSource> sources = new ArrayList<>();
    private boolean sprinting;

    private SpeedDetector() {
        sources.add(new SpeedPotionDetector());
        sources.add(new ArmorSpeedDetector());
        sources.add(new RancherBootsDetector());
        sources.add(new FarmingSpeedCalculator());
        sources.sort(Comparator.comparingInt(SpeedSource::priority).reversed());
    }

    public static SpeedDetector getInstance() {
        if (instance == null) instance = new SpeedDetector();
        return instance;
    }

    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }

    /** Compute current effective speed in blocks per second. */
    public double currentSpeedBps() {
        double speed = SpeedConfig.BASE_WALK_BPS;
        double multiplier = 1d;
        for (SpeedSource s : sources) {
            double c = s.currentSpeedBps();
            if (c > 0 && c < 5) multiplier += c; // treat as add-multiplier (potion style)
            else if (c >= 5) speed = Math.max(speed, c); // absolute override
        }
        if (sprinting) multiplier *= SpeedConfig.SPRINT_MULT;
        return speed * multiplier;
    }

    public List<SpeedSource> getSources() { return sources; }
}
