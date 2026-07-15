package com.zenith.client.world.navigation;

import com.zenith.client.engine.path.BlockPos;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathResult;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.engine.path.algorithm.WalkabilityChecker;
import com.zenith.client.world.World;

/**
 * Bridges ZenithPath to the live WorldAdapter. Installs a WalkabilityChecker.WorldView
 * that reads from {@link World#get()} so A* operates on real MC block data.
 *
 * <p>All path requests from macros, autopilot, and failsafes should go through
 * this class rather than calling ZenithPath directly, so the walkability view is
 * always installed correctly.</p>
 */
public final class PathNavigator {

    private static final PathNavigator INSTANCE = new PathNavigator();
    public static PathNavigator getInstance() { return INSTANCE; }

    private boolean installed;

    public void install() {
        if (installed) return;
        // Hook ZenithPath's A* walkability view to our WorldAdapter.
        var aStar = getAStar();
        if (aStar != null) {
            aStar.getWalkability().setWorldView(new WalkabilityChecker.WorldView() {
                @Override public boolean isSolid(BlockPos p) { return World.get().isSolid(p); }
                @Override public boolean isPassable(BlockPos p) { return World.get().isPassable(p); }
                @Override public boolean isClimbable(BlockPos p) { return World.get().isClimbable(p); }
                @Override public boolean isWater(BlockPos p) { return World.get().isWater(p); }
                @Override public boolean isLava(BlockPos p) { return World.get().isLava(p); }
            });
        }
        installed = true;
    }

    public PathResult navigateTo(double tx, double ty, double tz) {
        if (!installed) install();
        double px = World.get().playerX(), py = World.get().playerY(), pz = World.get().playerZ();
        PathRequest req = PathRequest.builder()
                .from(px, py, pz).to(tx, ty, tz)
                .allowEtherwarp(true).allowSprint(true)
                .stopDistance(0.5).maxComputeMs(30)
                .tag("nav")
                .build();
        return runCompute(req);
    }

    private PathResult runCompute(PathRequest req) {
        // Phase 6: compute and submit to executor. In this phase we return the result directly.
        return com.zenith.client.engine.path.algorithm.AStarAccess.compute(req);
    }

    private static com.zenith.client.engine.path.algorithm.AStarPathfinder getAStar() {
        return AStarAccess.astar();
    }
}
