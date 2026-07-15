package com.zenith.client.engine.path.learning;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.timer.Timer;
import com.zenith.client.engine.path.learning.profile.DefaultProfile;
import com.zenith.client.engine.path.learning.profile.ProfileBuilder;
import com.zenith.client.engine.path.learning.profile.ProfileExporter;
import com.zenith.client.engine.path.learning.profile.ProfileImporter;
import com.zenith.client.engine.path.learning.profile.ProfileMerger;
import com.zenith.client.engine.path.learning.profile.ProfileValidator;

import java.util.List;

/**
 * Master learning controller.
 *
 * <p>Every 30 seconds of non-macro movement, drains the recorder, builds a
 * fresh {@link MovementProfile} from the accumulated samples, and blends it
 * (low weight) into the active profile. The active profile drives all eyes/path
 * humanizer parameters, so over time the bot matches the player's own movement
 * style more and more closely.</p>
 *
 * <p>Critically: <b>out of the box the system uses the hand-tuned default
 * profile</b>, which is already brilliant and natural — learning only makes
 * it subtly more like the individual player. Even if the user never records
 * anything, movement looks human.</p>
 */
public final class MovementLearner {

    private static MovementLearner instance;

    private final MovementRecorder recorder = new MovementRecorder();
    private final ProfileBuilder builder = new ProfileBuilder();
    private final Timer retrainTimer = new Timer();
    private MovementProfile active;
    private boolean learningEnabled = true;
    private long samplesTrained;

    private MovementLearner() {
        active = ProfileImporter.load("learned");
        if (active == null) {
            active = DefaultProfile.build();
        } else {
            ProfileValidator.validate(active);
        }
        retrainTimer.reset();
    }

    public static MovementLearner getInstance() {
        if (instance == null) instance = new MovementLearner();
        return instance;
    }

    public void setLearningEnabled(boolean v) { this.learningEnabled = v; }
    public boolean isLearningEnabled() { return learningEnabled; }

    public MovementRecorder getRecorder() { return recorder; }

    public MovementProfile getActiveProfile() { return active; }

    public void recordSample(MovementSample s) {
        recorder.record(s);
    }

    /** Called periodically (each tick); retrains the profile when enough samples are collected. */
    public void tick() {
        if (!learningEnabled) return;
        if (retrainTimer.hasElapsed(30_000) && recorder.size() > 200) {
            retrainTimer.reset();
            List<MovementSample> batch = recorder.drain();
            MovementProfile learned = builder.build(batch, "learned");
            // Blend slowly: new data gets 25% weight so 4 retrains ~= full replacement.
            // Weight shrinks over time so early small batches don't dominate.
            double weight = 0.25 / (1.0 + samplesTrained / 4000.0);
            ProfileMerger.merge(active, learned, weight);
            samplesTrained += batch.size();
            // Persist every 4 retrains (~2 minutes).
            if ((samplesTrained / (batch.size() * 4)) % 2 == 0) {
                ProfileExporter.export(active);
            }
            ZenithClient.LOGGER.debug("[MovementLearner] Trained on {} samples (total {})", batch.size(), samplesTrained);
        }
    }

    public long samplesTrained() { return samplesTrained; }
}
