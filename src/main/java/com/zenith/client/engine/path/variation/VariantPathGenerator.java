package com.zenith.client.engine.path.variation;

import com.zenith.client.engine.path.algorithm.PathNode;
import com.zenith.client.engine.path.postprocess.NaturalDeviator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Produces variant paths from a base A* result by adding small extra waypoints
 * and biased offsets, used when we want to avoid travelling the exact same
 * node sequence twice.
 */
public final class VariantPathGenerator {

    public List<PathNode> vary(List<PathNode> base, PathVariationConfig cfg) {
        if (base.size() < 4) return new ArrayList<>(base);
        List<PathNode> out = new ArrayList<>(base.size() + 4);
        for (int i = 0; i < base.size() - 1; i++) {
            PathNode cur = base.get(i);
            out.add(cur);
            // Randomly inject a mid-point node with a 0.05-0.2 block sideways offset.
            if (i > 0 && i < base.size() - 2 && ThreadLocalRandom.current().nextFloat() < cfg.alternateRouteChance * 0.3f) {
                PathNode next = base.get(i+1);
                int mx = (cur.pos.x + next.pos.x) / 2;
                int my = cur.pos.y;
                int mz = (cur.pos.z + next.pos.z) / 2;
                int off = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
                if (Math.abs(next.pos.x - cur.pos.x) > Math.abs(next.pos.z - cur.pos.z)) mz += off;
                else mx += off;
                PathNode mid = new PathNode(new com.zenith.client.engine.path.BlockPos(mx, my, mz));
                mid.parent = cur;
                out.add(mid);
            }
        }
        out.add(base.get(base.size()-1));
        return out;
    }

    public List<NaturalDeviator.Waypoint> jitterWaypoints(List<NaturalDeviator.Waypoint> in, float sigma) {
        List<NaturalDeviator.Waypoint> out = new ArrayList<>(in.size());
        for (NaturalDeviator.Waypoint w : in) {
            double jx = ThreadLocalRandom.current().nextGaussian() * sigma;
            double jz = ThreadLocalRandom.current().nextGaussian() * sigma;
            out.add(new NaturalDeviator.Waypoint(w.x() + jx, w.y(), w.z() + jz, w.jumpHere()));
        }
        return out;
    }
}
