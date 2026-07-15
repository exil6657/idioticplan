package com.zenith.client.failsafe;

/**
 * Categorises each failsafe trigger so the UI and reaction engine can treat
 * them differently. Default severities follow the revised SkyBlock-aware logic:
 * <ul>
 *   <li>Rotation snaps / yaw flips → WIGGLE_REACT (camera wiggle + maybe "?" in chat).</li>
 *   <li>Low health → COMBAT (fight back / heal / eat) — NOT warp.</li>
 *   <li>Player nearby → WIGGLE_REACT (glance around, look legit) — no flee.</li>
 *   <li>Blocks placed in front of macro (obstruction) → REMOVE_OBSTRUCTION
 *       (look at block, swing, maybe "?", continue).</li>
 *   <li>Death / teleport / world-change / limbo → REPATH (recompute route, resume).</li>
 *   <li>Velocity kick / ban screen / staff pull → WARP_ISLAND (/is), escalating to DISCONNECT.</li>
 * </ul>
 */
public enum FailsafeType {

    PLAYER_NEARBY     ("Player Nearby",       FailsafeStrictness.WIGGLE_REACT,       true),
    BAN_DETECTED      ("Ban Screen",          FailsafeStrictness.DISCONNECT,         true),
    LIMBO             ("Limbo",               FailsafeStrictness.REPATH,             true),
    TELEPORT          ("Unexpected Teleport", FailsafeStrictness.REPATH,             true),
    VELOCITY_KICK     ("Velocity Kick",       FailsafeStrictness.WARP_ISLAND,        true),
    STAFF_PULL        ("Staff Pull",          FailsafeStrictness.WARP_ISLAND,        true),
    ITEM_DESELECT     ("Wrong Item Held",     FailsafeStrictness.WIGGLE_REACT,       false),
    ROTATION_RESET    ("Rotation Snapback",   FailsafeStrictness.WIGGLE_REACT,       false),
    WORLD_CHANGE      ("World Change",        FailsafeStrictness.REPATH,             true),
    GUI_CLOSE         ("GUI Unexpectedly Closed", FailsafeStrictness.NOTIFY,         false),
    WRONG_TOOL        ("Wrong Tool",          FailsafeStrictness.WIGGLE_REACT,       false),
    YAW_FLIP          ("Yaw 180 Flip",        FailsafeStrictness.WIGGLE_REACT,       false),
    INVENTORY_FULL    ("Inventory Full",      FailsafeStrictness.NOTIFY,             false),
    LAGBACK           ("Lagback",             FailsafeStrictness.REPATH,             true),
    DISMOUNT          ("Dismounted Entity",   FailsafeStrictness.WIGGLE_REACT,       false),
    DEATH             ("Player Death",        FailsafeStrictness.INSTANT_RESPAWN,    true),
    LOW_HEALTH        ("Low Health",          FailsafeStrictness.COMBAT,             false),
    LOW_HUNGER        ("Low Hunger",          FailsafeStrictness.COMBAT,             false),
    OBSTRUCTION       ("Path Obstructed",     FailsafeStrictness.REMOVE_OBSTRUCTION, true),
    CUSTOM            ("Custom",              FailsafeStrictness.NOTIFY,             true);

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
