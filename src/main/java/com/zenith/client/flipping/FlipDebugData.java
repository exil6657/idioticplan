package com.zenith.client.flipping;

/** Immutable snapshot rendered by Brain View / Flipper HUD panel. */
public final class FlipDebugData {

    public final boolean running;
    public final int activeOrders;
    public final int listedOrders;
    public final int holdingOrders;
    public final long sessionProfit;
    public final long sessionFlips;
    public final int candidateQueueDepth;
    public final long scans;
    public final int binItems;
    public final int bazaarItems;
    public final boolean onBreak;
    public final boolean ahBusy;
    public final long budget;
    public final double successRate;

    public FlipDebugData(boolean running, int activeOrders, int listedOrders, int holdingOrders,
                         long sessionProfit, long sessionFlips, int candidateQueueDepth,
                         long scans, int binItems, int bazaarItems, boolean onBreak,
                         boolean ahBusy, long budget, double successRate) {
        this.running = running;
        this.activeOrders = activeOrders;
        this.listedOrders = listedOrders;
        this.holdingOrders = holdingOrders;
        this.sessionProfit = sessionProfit;
        this.sessionFlips = sessionFlips;
        this.candidateQueueDepth = candidateQueueDepth;
        this.scans = scans;
        this.binItems = binItems;
        this.bazaarItems = bazaarItems;
        this.onBreak = onBreak;
        this.ahBusy = ahBusy;
        this.budget = budget;
        this.successRate = successRate;
    }
}
