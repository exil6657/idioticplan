package com.zenith.client.engine.path.postprocess;

import com.zenith.client.engine.path.algorithm.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits an A* node path into logical segments (STRAIGHT, TURN, JUMP, DROP) so
 * the MovementSimulator can apply appropriate humanization to each segment.
 */
public final class PathSegmenter {

    public enum SegmentType { STRAIGHT, TURN, JUMP, DROP, ETHERWARP }

    public record Segment(SegmentType type, List<PathNode> nodes) {}

    public List<Segment> segment(List<PathNode> nodes) {
        List<Segment> out = new ArrayList<>();
        if (nodes.isEmpty()) return out;
        List<PathNode> buf = new ArrayList<>();
        buf.add(nodes.get(0));
        SegmentType currentType = SegmentType.STRAIGHT;
        int prevDX = 0, prevDZ = 0;
        for (int i = 1; i < nodes.size(); i++) {
            PathNode prev = nodes.get(i-1);
            PathNode cur  = nodes.get(i);
            int dx = cur.pos.x - prev.pos.x;
            int dz = cur.pos.z - prev.pos.z;
            int dy = cur.pos.y - prev.pos.y;
            SegmentType nextType;
            if (cur.stepType == PathNode.StepType.ETHERWARP) nextType = SegmentType.ETHERWARP;
            else if (dy > 0) nextType = SegmentType.JUMP;
            else if (dy < 0) nextType = SegmentType.DROP;
            else if ((dx != prevDX || dz != prevDZ) && i > 1) nextType = SegmentType.TURN;
            else nextType = SegmentType.STRAIGHT;

            if (nextType != currentType && buf.size() > 1) {
                out.add(new Segment(currentType, new ArrayList<>(buf)));
                buf.clear();
                buf.add(prev);
            }
            buf.add(cur);
            currentType = nextType;
            prevDX = dx; prevDZ = dz;
        }
        if (!buf.isEmpty()) out.add(new Segment(currentType, buf));
        return out;
    }
}
