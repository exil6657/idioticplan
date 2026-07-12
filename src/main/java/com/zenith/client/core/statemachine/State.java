package com.zenith.client.core.statemachine;

import java.util.function.Consumer;

/**
 * A single state in a {@link StateMachine}.
 *
 * <p>Construct using {@link #builder(String)} to attach enter/tick/exit handlers.</p>
 */
public class State {

    private final String name;
    private final Consumer<StateContext> onEnter;
    private final Consumer<StateContext> onTick;
    private final Consumer<StateContext> onExit;

    public State(String name,
                 Consumer<StateContext> onEnter,
                 Consumer<StateContext> onTick,
                 Consumer<StateContext> onExit) {
        this.name = name;
        this.onEnter = onEnter;
        this.onTick = onTick;
        this.onExit = onExit;
    }

    public String getName() { return name; }

    public void enter(StateContext ctx) {
        if (onEnter != null) onEnter.accept(ctx);
    }
    public void tick(StateContext ctx) {
        if (onTick != null) onTick.accept(ctx);
    }
    public void exit(StateContext ctx) {
        if (onExit != null) onExit.accept(ctx);
    }

    public static Builder builder(String name) { return new Builder(name); }

    public static final class Builder {
        private final String name;
        private Consumer<StateContext> onEnter, onTick, onExit;
        private Builder(String name) { this.name = name; }

        public Builder onEnter(Consumer<StateContext> c) { this.onEnter = c; return this; }
        public Builder onTick(Consumer<StateContext> c)  { this.onTick = c;  return this; }
        public Builder onExit(Consumer<StateContext> c)  { this.onExit = c;  return this; }

        public State build() { return new State(name, onEnter, onTick, onExit); }
    }
}
