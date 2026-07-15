package com.zenith.client.core.statemachine;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.event.events.MacroErrorEvent;
import com.zenith.client.core.event.ZenithEventBus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic reusable state machine (master rule §6: state machines for everything).
 *
 * <p>Every macro, subsystem, and autopilot step is driven by one of these. The
 * standard lifecycle states are present as constants; consumers can add their
 * own via {@link #registerState(State)}.</p>
 *
 * <p>Mandatory standard states (always present):
 * IDLE, STARTING, RUNNING, PAUSED, RECOVERING, BREAK, STOPPING, ERROR.</p>
 */
public class StateMachine {

    // ---- Mandatory state ids --------------------------------------------
    public static final String IDLE       = "IDLE";
    public static final String STARTING   = "STARTING";
    public static final String RUNNING    = "RUNNING";
    public static final String PAUSED     = "PAUSED";
    public static final String RECOVERING = "RECOVERING";
    public static final String BREAK      = "BREAK";
    public static final String STOPPING   = "STOPPING";
    public static final String ERROR      = "ERROR";

    private final String name;
    private final Map<String, State> states = new HashMap<>();
    private final List<StateTransition> transitions = new ArrayList<>();
    private final StateContext context = new StateContext();

    private String currentState = IDLE;
    private String previousState = null;
    private boolean initialised = false;

    public StateMachine(String name) {
        this.name = name;
        // Register mandatory states as pass-throughs.
        registerState(State.builder(IDLE).build());
        registerState(State.builder(STARTING).build());
        registerState(State.builder(RUNNING).build());
        registerState(State.builder(PAUSED).build());
        registerState(State.builder(RECOVERING).build());
        registerState(State.builder(BREAK).build());
        registerState(State.builder(STOPPING).build());
        registerState(State.builder(ERROR).build());
        initialised = true;
    }

    public String getName() { return name; }
    public String getCurrentState() { return currentState; }
    public String getPreviousState() { return previousState; }
    public StateContext getContext() { return context; }

    public void registerState(State s) {
        states.put(s.getName(), s);
    }

    public void addTransition(StateTransition t) {
        transitions.add(t);
    }

    /** Force a transition to a specific state regardless of predicates. */
    public void transitionTo(String target) {
        State next = states.get(target);
        if (next == null) {
            ZenithClient.LOGGER.warn("[{}] Unknown state {}, going to ERROR", name, target);
            transitionTo(ERROR);
            return;
        }
        State cur = states.get(currentState);
        if (cur != null) {
            try { cur.exit(context); } catch (Throwable t) {
                ZenithClient.LOGGER.error("[{}] exit({}) failed", name, currentState, t);
            }
        }
        previousState = currentState;
        currentState = target;
        context.setStateEnteredAtMs(System.currentTimeMillis());
        try { next.enter(context); } catch (Throwable t) {
            ZenithClient.LOGGER.error("[{}] enter({}) failed", name, target, t);
            error(t.getMessage());
        }
    }

    /** Tick the state machine (called from the owning macro's tick loop). */
    public void tick() {
        State cur = states.get(currentState);
        if (cur == null) {
            error("No such state: " + currentState);
            return;
        }
        context.setLastTickAtMs(System.currentTimeMillis());
        try { cur.tick(context); } catch (Throwable t) {
            ZenithClient.LOGGER.error("[{}] tick({}) failed", name, currentState, t);
            error(t.getMessage());
            return;
        }
        // Evaluate transitions for the current state, highest priority first.
        List<StateTransition> candidates = new ArrayList<>();
        for (StateTransition t : transitions) {
            if (t.getSource().equals(currentState)) candidates.add(t);
        }
        candidates.sort(Comparator.comparingInt(StateTransition::getPriority).reversed());
        for (StateTransition t : candidates) {
            try {
                if (t.test(context)) {
                    transitionTo(t.getTarget());
                    return;
                }
            } catch (Throwable ex) {
                ZenithClient.LOGGER.warn("[{}] transition {}->{} predicate threw", name, currentState, t.getTarget(), ex);
            }
        }
    }

    public void start() { transitionTo(STARTING); }
    public void stop()  { transitionTo(STOPPING); }
    public void pause() { transitionTo(PAUSED); }
    public void resume() { transitionTo(RUNNING); }
    public void requestBreak() { transitionTo(BREAK); }
    public void recover() { transitionTo(RECOVERING); }

    public void error(String reason) {
        ZenithEventBus.getInstance().post(new MacroErrorEvent(name, reason, null));
        if (!ERROR.equals(currentState)) transitionTo(ERROR);
    }

    public boolean isRunning() { return RUNNING.equals(currentState); }
    public boolean isIdle()    { return IDLE.equals(currentState); }
    public boolean isPaused()  { return PAUSED.equals(currentState); }
    public boolean isErrored() { return ERROR.equals(currentState); }
}
