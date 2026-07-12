package com.zenith.client.engine.path.speed;

/** Detects Rancher Boots (Garden farming boots). Phase 6 reads from equipped boots item. */
public final class RancherBootsDetector implements SpeedSource {
    private boolean equipped;
    public void setEquipped(boolean v) { this.equipped = v; }
    @Override public double currentSpeedBps() {
        return equipped ? SpeedConfig.BASE_WALK_BPS * SpeedConfig.RANCHER_MULT : 0d;
    }
    @Override public int priority() { return 10; }
}
