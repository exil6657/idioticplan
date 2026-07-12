package com.zenith.client.failsafe;

/**
 * Categorises each failsafe trigger so the UI and reaction engine can treat
 * them differently (e.g. a {@code PLAYER_NEARBY} gets a mistake-sim "oops"
 * while {@code BAN} goes straight to disconnect).
 */
public enum FailsafeType {

    PLAYER_NEARBY     ("Player Nearby",      FailsafeStrictness.PAUSE,      true),
    BAN_DETECTED      ("Ban Screen",         FailsafeStrictness.DISCONNECT, true),
    LIMBO             ("Limbo",              FailsafeStrictness.PAUSE,      true),
    TELEPORT          ("Unexpected Teleport",FailsafeStrictness.PAUSE,      true),
    VELOCITY_KICK     ("Velocity Kick",      FailsafeStrictness.WARP_HOME,  true),
    ITEM_DESELECT     ("Wrong Item Held",    FailsafeStrictness.PAUSE,      false),
    ROTATION_RESET    ("Rotation Snapback",  FailsafeStrictness.NOTIFY,     false),
    WORLD_CHANGE      ("World Change",       FailsafeStrictness.PAUSE,      true),
    GUI_CLOSE         ("GUI Unexpectedly Closed", FailsafeStrictness.NOTIFY, false),
    WRONG_ITEM        ("Wrong Tool",         FailsafeStrictness.PAUSE,      false),
    YAW_FLIP          ("Yaw 180 Flip",       FailsafeStrictness.NOTIFY,     false),
    INVENTORY_FULL    ("Inventory Full",     FailsafeStrictness.NOTIFY,     false),
    LAGBACK           ("Lagback",            FailsafeStrictness.PAUSE,      true),
    DISMOUNT          ("Dismounted Entity",  FailsafeStrictness.NOTIFY,     false),
    DEATH             ("Player Death",       FailsafeStrictness.PAUSE,      true),
    LOW_HEALTH        ("Low Health",         FailsafeStrictness.WARP_HOME,  false),
    NO_HUNGER         ("No Hunger",          FailsafeStrictness.WARP_HOME,  false),
    CUSTOM            ("Custom",             FailsafeStrictness.NOTIFY,     true);

    private final String displayName;
    private final FailsafeStrictness defaultSeverity;
    private final boolean autoEscalates;

    FailsafeType(String displayName, FailsafeStrictness defaultSeverity, boolean autoEscalates) {
        this.displayName = displayName;
        this.defaultSeverity = defaultSeverity;
        this.autoEscalates = autoEscalates;
    }

    public String displayName() { return displayName; }
    public FailsafeStrictness defaultSeverity() { return defaultSeverity; }
    public boolean autoEscalates() { return autoEscalates; }
}
