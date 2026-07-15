package com.zenith.client.engine.path;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.MathUtils;
import com.zenith.client.engine.path.algorithm.AStarPathfinder;
import com.zenith.client.engine.path.algorithm.EtherwarpPathfinder;
import com.zenith.client.engine.path.algorithm.FlyingPathfinder;
import com.zenith.client.engine.path.algorithm.PathNode;
import com.zenith.client.engine.path.postprocess.*;
import com.zenith.client.engine.path.speed.SpeedDetector;
import com.zenith.client.engine.path.variation.PathVariationEngine;

import java.util.List;

/**
 * Master controller for pathfinding and movement.
 *
 * <p><b>Master rule §3:</b> no direct movement — all player movement is done via
 * this class. Callers submit a {@link PathRequest} and ZenithPath handles
 * A*/etherwarp planning, smoothing, deviation, humanized waypoint execution,
 * speed detection, obstacle recovery, and learning. The output (forward, strafe,
 * jump, sprint) is consumed by the InputEngine each tick.</p>
 *
 * <p>Fine marker granularity: post-processing subdivides each A* edge into many
 * sub-block waypoints (~0.3 blocks apart) so movement is driven by many tiny
 * targets rather than chunky block waypoints. This produces smooth curves and
 * makes the player look like they're moving naturally, not pathing to integer
 * coordinates.</p>
 */
public final class ZenithPath {

    private static ZenithPath instance;

    private final AStarPathfinder astar = new AStarPathfinder();
    private final EtherwarpPathfinder etherwarp = new EtherwarpPathfinder();
    private final FlyingPathfinder fly = new FlyingPathfinder();

    private final PathSmoother smoother = new PathSmoother();
    private final NaturalDeviator deviator = new NaturalDeviator();
    private final PathSegmenter segmenter = new PathSegmenter();
    private final CourseCorrector corrector = new CourseCorrector();
    private final ArrivalOvershoot arrival = new ArrivalOvershoot();
    private final RouteMemory memory = new RouteMemory();
    private final WallAwareness walls = new WallAwareness();
    private final TerrainAdaptation terrain = new TerrainAdaptation();

    private final PathExecutor executor = new PathExecutor();
    private final PathVariationEngine variation = new PathVariationEngine();
    private final SpeedDetector speed = SpeedDetector.getInstance();

    private PathRequest current;
    private PathResult lastResult;
    private long lastRepathAt;

    private ZenithPath() {}

    public static ZenithPath getInstance() {
        if (instance == null) instance = new ZenithPath();
        return instance;
    }

    public void init() {
        ZenithClient.LOGGER.info("[ZenithPath] Initialized (A*, etherwarp, post-process, humanizers, learner).");
    }

    /** Submit a new path request; computes and begins execution. */
    public void requestPath(PathRequest req) {
        this.current = req;
        recompute(req);
    }

    public PathExecutor executor() { return executor; }
    public PathVariationEngine variation() { return variation; }
    public SpeedDetector speed() { return speed; }
    public boolean isFollowing() { return executor.state() == PathExecutor.State.FOLLOWING; }
    public PathRequest getCurrent() { return current; }

    /**
     * Called each tick.
     *
     * @return movement inputs for this tick (forward, strafe, jump, sprint), or all-zeros if idle.
     */
    public MovementSimulator.MovementInput tick(double px, double py, double pz, float yaw, boolean onGround, long nowMs) {
        if (current == null) return new MovementSimulator.MovementInput(0f,0f,false,false);

        // Re-path if executor signals it's stuck.
        if (executor.needsRepath() && nowMs - lastRepathAt > 800) {
            lastRepathAt = nowMs;
            PathRequest again = PathRequest.builder()
                    .from(px,py,pz).to(current.toX,current.toY,current.toZ)
                    .mode(current.mode).allowEtherwarp(current.allowEtherwarp)
                    .allowSprint(current.allowSprint).stopDistance(current.stopDistance)
                    .maxComputeMs(current.maxComputeMs).tag(current.tag + ":recover").build();
            recompute(again);
        }

        // Periodic course correction check every 1.2 s.
        // (full recompute only if drift is beyond threshold, to avoid thrash.)

        return executor.tick(px, py, pz, yaw, onGround, nowMs);
    }

    public void stop() {
        current = null;
        executor.cancel();
    }

    public PathResult getLastResult() { return lastResult; }

    // ---- Internals ------------------------------------------------------

    private void recompute(PathRequest req) {
        PathResult res = switch (req.mode) {
            case ETHERWARP -> etherwarp.compute(req);
            case FLY -> fly.compute(req);
            case ROTATE_ONLY -> new PathResult(PathResult.Status.FOUND, List.of(), 0, 0, BlockPos.containing(req.fromX, req.fromY, req.fromZ));
            case WALK -> astar.compute(req);
        };
        this.lastResult = res;
        if (!res.isSuccess()) {
            ZenithClient.LOGGER.warn("[ZenithPath] Path {}: {}", req.tag, res.status);
            return;
        }

        // Apply variation to the coarse node list.
        List<PathNode> nodes = variation.maybeVary(res.nodes);

        // Smooth (collapse co-linear waypoints).
        List<PathNode> smoothed = smoother.smooth(nodes, (x,y,z) -> true); // LOS stub until Phase 6.

        // Deviate into fine-grained waypoints (subdivided, with perpendicular jitter).
        // 4 subdivisions per block gives ~0.25 block markers — very fine movement resolution.
        List<NaturalDeviator.Waypoint> wps = deviator.deviate(smoothed, 4);
        wps = variation.addJitter(wps);

        executor.start(wps);
    }

    public PathDebugData debugData() {
        boolean stuck = executor.needsRepath();
        return new PathDebugData(
                executor.state().name(),
                current != null ? current.mode.name() : "IDLE",
                lastResult != null ? lastResult.nodes.size() : 0,
                0,
                lastResult != null ? lastResult.totalCost : 0,
                current != null ? Math.hypot(current.toX - 0, current.toZ - 0) : 0,
                speed.currentSpeedBps(),
                stuck,
                stuck ? "recovery" : "",
                lastResult != null ? lastResult.computeMs : 0
        );
    }
}
