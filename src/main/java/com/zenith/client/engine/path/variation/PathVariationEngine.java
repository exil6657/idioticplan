package com.zenith.client.engine.path.variation;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.algorithm.PathNode;
import com.zenith.client.engine.path.postprocess.NaturalDeviator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Master variation controller. After A* produces a base path the engine:
 * <ol>
 *   <li>Optionally injects a variant (VariantPathGenerator).</li>
 *   <li>Smooths to waypoints (PathSmoother — called by ZenithPath).</li>
 *   <li>Adds Gaussian jitter to fine waypoints.</li>
 *   <li>Pulls from route history when available to prefer known-good shapes.</li>
 * </ol>
 */
public final class PathVariationEngine {

    private final PathVariationConfig config = new PathVariationConfig();
    private final VariantPathGenerator variant = new VariantPathGenerator();
    private final RouteHistoryTracker history = new RouteHistoryTracker();
    private final DeathRecoveryRouter deathRecovery = new DeathRecoveryRouter();
    private final NaturalDeviator deviator = new NaturalDeviator();

    public PathVariationConfig config() { return config; }
    public RouteHistoryTracker history() { return history; }
    public DeathRecoveryRouter death() { return deathRecovery; }
    public VariantPathGenerator variantGen() { return variant; }
    public NaturalDeviator deviator() { return deviator; }

    public List<PathNode> maybeVary(List<PathNode> base) {
        if (ThreadLocalRandom.current().nextFloat() < config.alternateRouteChance) {
            return variant.vary(base, config);
        }
        return base;
    }

    public List<NaturalDeviator.Waypoint> addJitter(List<NaturalDeviator.Waypoint> ws) {
        return variant.jitterWaypoints(ws, config.waypointJitterSigma);
    }

    public void rememberRoute(BlockPos from, BlockPos to, List<NaturalDeviator.Waypoint> path) {
        history.record(from, to, path);
    }
}
