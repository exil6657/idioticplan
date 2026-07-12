package com.zenith.client.engine.eyes;

import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.eyes.curve.BezierRotationCurve;
import com.zenith.client.engine.eyes.curve.ControlPointGenerator;
import com.zenith.client.engine.eyes.curve.CubicRotationCurve;
import com.zenith.client.engine.eyes.curve.CurveBlender;
import com.zenith.client.engine.eyes.humanizer.*;

/**
 * Stateful executor that drives a single {@link RotationRequest} to completion.
 *
 * <p>Holds curve state, humanizer state (acceleration, overshoot, corrections,
 * jitter, hesitation, quantisation), gaze history, and produces a yaw/pitch
 * delta each frame via {@link #step(float, float, long, float)}.</p>
 */
public class RotationExecutor {

    private enum Phase { IDLE, EXECUTING, CORRECTING, DONE }

    private final AccelerationModel accel = new AccelerationModel();
    private final OvershootCorrector overshoot = new OvershootCorrector();
    private final MicroCorrectionEngine corrector = new MicroCorrectionEngine();
    private final JitterInjector jitter = new JitterInjector();
    private final HesitationEngine hesitation = new HesitationEngine();
    private final TickQuantizer quantizer = new TickQuantizer();
    public final GazeHistory gazeHistory = new GazeHistory(10);
    public final MouseSensitivitySimulator sens = new MouseSensitivitySimulator();

    private RotationRequest request;
    private RotationProfile profile;
    private BezierRotationCurve yawCurve;
    private BezierRotationCurve pitchCurve;
    private float startYaw, startPitch;
    private float totalYawDelta, totalPitchDelta;
    private float yawDistance, pitchDistance;
    private long startedAt;
    private long adjustedDurationMs;
    private Phase phase = Phase.IDLE;
    private float lastYaw, lastPitch;
    private double angularVel; // deg/s (smoothed)
    private final ContextualBehavior ctx = new ContextualBehavior();
    private final FatigueModel fatigue = new FatigueModel();

    public void start(RotationRequest req, float currentYaw, float currentPitch, ContextualBehavior context) {
        this.request = req;
        this.profile = RotationProfileRegistry.getInstance().get(req.profileId);
        this.startYaw = currentYaw;
        this.startPitch = currentPitch;
        this.lastYaw = currentYaw;
        this.lastPitch = currentPitch;
        this.startedAt = System.currentTimeMillis();

        // Compute delta with wrap-around (shortest arc).
        this.totalYawDelta = MathUtils.angleDelta(req.targetYaw, currentYaw);
        this.totalPitchDelta = MathUtils.clamp(req.targetPitch - currentPitch, -90f, 90f);
        this.yawDistance = Math.abs(totalYawDelta);
        this.pitchDistance = Math.abs(totalPitchDelta);

        float angularDistance = (float) Math.hypot(yawDistance, pitchDistance);
        float duration = req.durationMs > 0 ? req.durationMs : profile.durationForAngle(angularDistance);
        duration *= context.durationMultiplier() * context.fatigueMultiplier(profile.fatigueMax);
        this.adjustedDurationMs = (long) Math.max(profile.minTurnMs, duration);

        // Generate per-axis control points & curves.
        var yawCP = ControlPointGenerator.generate(profile, yawDistance, true);
        var pitchCP = ControlPointGenerator.generate(profile, pitchDistance, false);
        this.yawCurve = new BezierRotationCurve(yawCP);
        this.pitchCurve = new BezierRotationCurve(pitchCP);

        accel.reset();
        overshoot.reset();
        corrector.reset();
        hesitation.reset();
        jitter.setSigma(profile.jitterSigma);
        hesitation.configure(profile.hesitationChance + 0.04f * fatigue.getFatigue(), profile.hesitationMaxMs);
        quantizer.setHz(profile.outputHz);
        quantizer.reset();
        gazeHistory.reset();

        // Roll for overshoot.
        overshoot.roll(angularDistance, profile.overshootChance, profile.overshootMax);

        this.phase = Phase.EXECUTING;
    }

    public boolean isRunning() { return phase != Phase.DONE && phase != Phase.IDLE; }
    public RotationRequest getRequest() { return request; }
    public float getProgress() {
        if (phase == Phase.DONE || phase == Phase.IDLE) return phase == Phase.DONE ? 1f : 0f;
        float t = (float) (System.currentTimeMillis() - startedAt) / adjustedDurationMs;
        return MathUtils.clamp(t, 0f, 1f);
    }

    /**
     * Advance the rotation by one frame.
     *
     * @param currentYaw   player's current actual yaw (may diverge from lastYaw due to external movement)
     * @param currentPitch player's current actual pitch
     * @param nowMs        current time millis
     * @param dtMs         frame delta in ms
     * @return desired new [yaw, pitch] to send to the game this frame
     */
    public float[] step(float currentYaw, float currentPitch, long nowMs, float dtMs) {
        if (phase == Phase.DONE || phase == Phase.IDLE) return new float[]{currentYaw, currentPitch};
        fatigue.update(ctx.sessionMinutes);
        ctx.sessionMinutes = ctx.sessionMinutes; // carried from external update

        if (hesitation.isPaused(nowMs)) {
            return new float[]{lastYaw, lastPitch};
        }

        if (phase == Phase.CORRECTING) {
            float[] micro = corrector.tick(nowMs, dtMs);
            lastYaw += micro[0];
            lastPitch += micro[1];
            if (!corrector.isActive()) phase = Phase.DONE;
            return quantized(lastYaw, lastPitch, nowMs);
        }

        float t = MathUtils.clamp((float) (nowMs - startedAt) / adjustedDurationMs, 0f, 1f);

        // Sample curves and blend.
        float yawBezier = yawCurve.sample(t);
        float pitchBezier = pitchCurve.sample(t);
        float cubic = CubicRotationCurve.smootherstep(t);
        float yT = CurveBlender.blend(yawBezier, cubic, profile.cubicBlend);
        float pT = CurveBlender.blend(pitchBezier, cubic, profile.cubicBlend);

        float desiredYaw = startYaw + totalYawDelta * yT;
        float desiredPitch = startPitch + totalPitchDelta * pT;

        // Apply overshoot offset (added on top of curve as it ramps out past 1.0 via the curve's cp).
        float[] os = overshoot.apply(t);
        desiredYaw += os[0];
        desiredPitch += os[1];

        // Reactive error correction: use actual player yaw (accounts for mouse outside our control).
        float errorYaw = MathUtils.angleDelta(request.targetYaw, currentYaw);
        float errorPitch = request.targetPitch - currentPitch;

        float dYaw = MathUtils.angleDelta(desiredYaw, lastYaw);
        float dPitch = desiredPitch - lastPitch;

        // Acceleration model limits instantaneous angular acceleration.
        float[] acc = accel.apply(dYaw, dPitch, errorYaw, errorPitch, profile.accelK, Math.max(1f, dtMs));
        float stepYaw = acc[0];
        float stepPitch = acc[1];

        // Jitter.
        float[] j = jitter.sample();
        stepYaw += j[0];
        stepPitch += j[1];

        lastYaw += stepYaw;
        lastPitch += stepPitch;

        // Clamp pitch.
        lastPitch = MathUtils.clampf(lastPitch, -90f, 90f);

        // Track gaze history for context.
        float dtSec = Math.max(0.001f, dtMs / 1000f);
        gazeHistory.push(stepYaw / dtSec, stepPitch / dtSec);
        angularVel = angularVel * 0.8f + (Math.abs(stepYaw) + Math.abs(stepPitch)) / dtSec * 0.2f;

        // End of main phase → transition to corrections or DONE.
        if (t >= 1f && !overshoot.isActive()) {
            float residualYaw = MathUtils.angleDelta(request.targetYaw, lastYaw);
            float residualPitch = request.targetPitch - lastPitch;
            if (Math.abs(residualYaw) < 0.08f && Math.abs(residualPitch) < 0.08f) {
                lastYaw = request.targetYaw;
                lastPitch = request.targetPitch;
                phase = Phase.DONE;
            } else {
                corrector.start(residualYaw, residualPitch, nowMs);
                phase = Phase.CORRECTING;
            }
        }
        return quantized(lastYaw, lastPitch, nowMs);
    }

    private float[] quantized(float yaw, float pitch, long nowMs) {
        float dYaw = MathUtils.angleDelta(yaw, currentQuantizedYaw);
        float dPitch = pitch - currentQuantizedPitch;
        long nowNs = nowMs * 1_000_000L;
        float[] q = quantizer.quantize(dYaw, dPitch, nowNs);
        currentQuantizedYaw += q[0];
        currentQuantizedPitch += q[1];
        return new float[]{currentQuantizedYaw, currentQuantizedPitch};
    }
    private float currentQuantizedYaw, currentQuantizedPitch;

    public void updateContext(ContextualBehavior c) {
        this.ctx.set(c.isMoving, c.isSprinting, c.isInCombat, c.isInMenu, c.isFalling, c.speed, c.yawVelocity, c.alertness, c.sessionMinutes);
    }

    public double getAngularVelocity() { return angularVel; }
    public boolean isOvershooting() { return overshoot.isActive(); }
    public boolean isCorrecting() { return phase == Phase.CORRECTING; }
    public boolean isHesitating() { return hesitation.isPaused(System.currentTimeMillis()); }

    public void cancel() { phase = Phase.DONE; }
}
