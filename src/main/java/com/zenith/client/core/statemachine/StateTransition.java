package com.zenith.client.core.statemachine;

import java.util.function.Predicate;

/**
 * A transition rule from one state to another.
 *
 * <p>Evaluated each tick while the source state is active. When the predicate
 * returns {@code true} the state machine moves to {@code target}.</p>
 */
public class StateTransition {

    private final String source;
    private final String target;
    private final Predicate<StateContext> condition;
    private final int priority;

    public StateTransition(String source, String target, Predicate<StateContext> condition, int priority) {
        this.source = source;
        this.target = target;
        this.condition = condition;
        this.priority = priority;
    }

    public StateTransition(String source, String target, Predicate<StateContext> condition) {
        this(source, target, condition, 0);
    }

    public String getSource() { return source; }
    public String getTarget() { return target; }
    public int getPriority() { return priority; }

    public boolean test(StateContext ctx) {
        try { return condition.test(ctx); }
        catch (Throwable t) { return false; }
    }
}
