package com.zenith.client.flipping.recovery;

import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeType;

/**
 * NavigationRecovery — placeholder recovery step for the flipper.
 *
 * <p>When the flipper detects an unusual state (limbo, lobby, world change, GUI
 * not opening), the recovery chain pauses new orders, fires a failsafe trigger,
 * and — in later phases — executes /locraw + navigation back to the hub.
 * Phase 9 just installs the stub and hooks it to the failsafe bus.</p>
 */
public final class NavigationRecovery {
    private static final NavigationRecovery INSTANCE = new NavigationRecovery();
    public static NavigationRecovery getInstance() { return INSTANCE; }
    private NavigationRecovery() {}
    public void tick() { /* filled in when nav/macros are wired */ }
}
