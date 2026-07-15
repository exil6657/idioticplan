package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class SkillLevelUpEvent extends ZenithEvent {
    private final String skillName;
    private final int newLevel;
    public SkillLevelUpEvent(String skillName, int newLevel) { this.skillName = skillName; this.newLevel = newLevel; }
    public String getSkillName() { return skillName; }
    public int getNewLevel() { return newLevel; }
    @Override public boolean isCancellable() { return false; }
}
