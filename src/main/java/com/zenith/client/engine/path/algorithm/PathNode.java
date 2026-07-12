package com.zenith.client.engine.path.algorithm;

import com.zenith.client.engine.path.BlockPos;

/** Node in the A* open/closed set. */
public final class PathNode {
    public final BlockPos pos;
    public PathNode parent;
    public double g; // cost so far
    public double h; // heuristic
    public double f; // g + h
    /** Type of step that arrived here (walk, jump, drop, etherwarp). */
    public StepType stepType = StepType.WALK;
    /** Whether we arrived via a jump start or fall. */
    public boolean onGround;

    public PathNode(BlockPos pos) { this.pos = pos; }

    public enum StepType { WALK, JUMP, DROP, CLIMB, ETHERWARP, SPRINT }

    @Override public boolean equals(Object o) {
        return o instanceof PathNode n && n.pos.equals(pos);
    }
    @Override public int hashCode() { return pos.hashCode(); }
}
