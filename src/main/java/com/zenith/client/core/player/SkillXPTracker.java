package com.zenith.client.core.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks skill XP levels by skill name — populated by SkillLevelUpEvent chat parser. */
public final class SkillXPTracker {
    private static final SkillXPTracker INSTANCE = new SkillXPTracker();
    public static SkillXPTracker getInstance() { return INSTANCE; }
    private final Map<String, Integer> levels = new ConcurrentHashMap<>();
    public void setLevel(String skill, int level) { levels.put(skill.toLowerCase(), level); }
    public int getLevel(String skill) { return levels.getOrDefault(skill.toLowerCase(), 0); }
}
