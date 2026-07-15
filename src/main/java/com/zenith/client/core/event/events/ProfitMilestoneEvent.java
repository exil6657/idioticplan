package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class ProfitMilestoneEvent extends ZenithEvent {
    private final long sessionProfit;
    private final long totalProfit;
    public ProfitMilestoneEvent(long sessionProfit, long totalProfit) { this.sessionProfit = sessionProfit; this.totalProfit = totalProfit; }
    public long getSessionProfit() { return sessionProfit; }
    public long getTotalProfit() { return totalProfit; }
    @Override public boolean isCancellable() { return false; }
}
