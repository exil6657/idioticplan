package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class CropMilestoneEvent extends ZenithEvent {
    private final String cropId;
    private final int newMilestone;
    public CropMilestoneEvent(String cropId, int newMilestone) { this.cropId = cropId; this.newMilestone = newMilestone; }
    public String getCropId() { return cropId; }
    public int getNewMilestone() { return newMilestone; }
    @Override public boolean isCancellable() { return false; }
}
