package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class CommunityUpgradeReadyEvent extends ZenithEvent {
    private final String upgradeName;
    public CommunityUpgradeReadyEvent(String upgradeName) { this.upgradeName = upgradeName; }
    public String getUpgradeName() { return upgradeName; }
    @Override public boolean isCancellable() { return false; }
}
