package com.zenith.client.flipping.npc;

/** Placeholder for NPC route planning (Phase 11 autopilot/farming macros include NPC waypoints). */
public final class NPCNavigator {
    private static final NPCNavigator INSTANCE = new NPCNavigator();
    public static NPCNavigator getInstance() { return INSTANCE; }
    private NPCNavigator() {}
}
