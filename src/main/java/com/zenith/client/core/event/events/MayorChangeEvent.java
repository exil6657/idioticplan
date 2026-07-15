package com.zenith.client.core.event.events;

import com.zenith.client.core.event.ZenithEvent;

public class MayorChangeEvent extends ZenithEvent {
    private final String newMayorName;
    private final String perksCsv;
    private final boolean election;
    public MayorChangeEvent(String newMayorName, String perksCsv, boolean election) {
        this.newMayorName = newMayorName; this.perksCsv = perksCsv; this.election = election;
    }
    public String getNewMayorName() { return newMayorName; }
    public String getPerksCsv() { return perksCsv; }
    public boolean isElection() { return election; }
    @Override public boolean isCancellable() { return false; }
}
