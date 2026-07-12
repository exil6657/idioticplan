package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.algorithm.PathNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deviates straight-line path segments slightly (perpendicular) so the player
 * doesn't walk mathematically perfect lines. Deviation is Gaussian and
 * amplitude is small (± 0.2 blocks) so it looks organic but not suspicious.
 */
public final class NaturalDeviator {

    private float sigma = 0.12f;

    public void setSigma(float s) { this.sigma = s; }

    public List<Waypoint> deviate(List<PathNode> nodes, int subdivisionsPerSegment) {
        List<Waypoint> out = new ArrayList<>();
        if (nodes.isEmpty()) return out;
        for (int i = 0; i < nodes.size(); i++) {
            PathNode cur = nodes.get(i);
            if (i == nodes.size() - 1) {
                out.add(new Waypoint(cur.pos.x + 0.5, cur.pos.y, cur.pos.z + 0.5, false));
                break;
            }
            PathNode next = nodes.get(i+1);
            for (int s = 0; s < subdivisionsPerSegment; s++) {
                double t = (double) s / subdivisionsPerSegment;
                double x = lerp(cur.pos.x + 0.5, next.pos.x + 0.5, t);
                double y = lerp(cur.pos.y, next.pos.y, t);
                double z = lerp(cur.pos.z + 0.5, next.pos.z + 0.5, t);
                // perpendicular offset in XZ
                double dx = next.pos.x - cur.pos.x;
                double dz = next.pos.z - cur.pos.z;
                double len = Math.hypot(dx, dz);
                if (len > 0.001) {
                    double nx = -dz / len;
                    double nz =  dx / len;
                    double off = ThreadLocalRandom.current().nextGaussian() * sigma;
                    x += nx * off;
                    z += nz * off;
                }
                boolean isJump = next.pos.y > cur.pos.y && s == subdivisionsPerSegment - 1;
                out.add(new Waypoint(x, y, z, isJump));
            }
        }
        return out;
    }

    private static double lerp(double a, double b, double t) { return a + (b-a)*t; }

    /** Fine-grained execution waypoint (entity position coordinates). */
    public record Waypoint(double x, double y, double z, boolean jumpHere) {}
}
