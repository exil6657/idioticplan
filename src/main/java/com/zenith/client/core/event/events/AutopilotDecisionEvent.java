package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

/** Fired each tick by the autopilot planner so external modules can observe/override decisions. */
public class AutopilotDecisionEvent extends ZenithEvent {
    private final String decision;
    public AutopilotDecisionEvent(String decision) { this.decision = decision; }
    public String getDecision() { return decision; }
}
