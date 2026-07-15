package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class VisitorArrivedEvent extends ZenithEvent {
    private final String visitorName;
    private final String offerSummary;
    public VisitorArrivedEvent(String visitorName, String offerSummary) {
        this.visitorName = visitorName; this.offerSummary = offerSummary;
    }
    public String getVisitorName() { return visitorName; }
    public String getOfferSummary() { return offerSummary; }
    @Override public boolean isCancellable() { return false; }
}
