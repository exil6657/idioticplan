package com.zenith.client.engine.input;

import com.zenith.client.ZenithClient;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.engine.path.movement.MovementSimulator;

/**
 * Central input controller.
 *
 * <p>Each tick the engine:
 * <ol>
 *   <li>Queries ZenithPath for desired movement inputs (forward/strafe/jump/sprint).</li>
 *   <li>Queries ZenithEyes for desired yaw/pitch and writes them to the MouseSimulator.</li>
 *   <li>Applies the resulting key and mouse states to KeySimulator/MouseSimulator.</li>
 *   <li>Flushes key states to Minecraft (Phase 4: key bindings).</li>
 * </ol>
 * <b>Master rules §2, §3, §9:</b> no direct movement, no instant rotations, no
 * hardcoded keybinds — everything goes through the eyes/path engines, which in
 * turn respect the player's configured keybinds via PlayerKeybindReader.</p>
 */
public final class InputEngine {

    private static InputEngine instance;

    private final KeySimulator keys = new KeySimulator();
    private final MouseSimulator mouse = new MouseSimulator();
    private final ClickSimulator clicks = new ClickSimulator(mouse);
    private final InputConfig config = new InputConfig();

    private double playerX, playerY, playerZ;
    private float playerYaw, playerPitch;
    private boolean onGround = true;

    private InputEngine() {}

    public static InputEngine getInstance() {
        if (instance == null) instance = new InputEngine();
        return instance;
    }

    public void init() {
        ZenithClient.LOGGER.info("[InputEngine] Initialized.");
    }

    public void updatePlayerState(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.playerX = x; this.playerY = y; this.playerZ = z;
        this.playerYaw = yaw; this.playerPitch = pitch; this.onGround = onGround;
    }

    /** Called once per client tick. */
    public void tick() {
        if (!config.inputOverride) { keys.flush(); return; }

        // 1. Ask the path system for movement.
        long now = System.currentTimeMillis();
        MovementSimulator.MovementInput mi = ZenithPath.getInstance().tick(
                playerX, playerY, playerZ, playerYaw, onGround, now);
        if (mi == null) mi = new MovementSimulator.MovementInput(0f,0f,false,false);

        // 2. Apply movement to keys.
        applyMovement(mi);

        // 3. Apply rotation from eyes → mouse deltas.
        float[] rot = ZenithEyes.getInstance().onCameraRender(1f);
        float dyaw   = rot[0] - playerYaw;
        float dpitch = rot[1] - playerPitch;
        // Convert to MC mouse-delta units (sensitivity-aware via ZenithEyes's own simulator).
        mouse.move(dyaw * 0.15f, dpitch * 0.15f);

        keys.flush();
    }

    private void applyMovement(MovementSimulator.MovementInput mi) {
        // Don't halt attack/use here — those are managed by macros/combat.
        // Only clear movement keys; macros explicitly set attack/use.
        boolean keepAttack = keys.attack();
        boolean keepUse = keys.use();
        keys.setForward(false);
        keys.setBack(false);
        keys.setLeft(false);
        keys.setRight(false);
        keys.setJump(false);
        keys.setSprint(false);
        // restore attack/use if macro holds them
        if (keepAttack) keys.setAttack(true);
        if (keepUse) keys.setUse(true);

        float f = mi.forward();
        float s = mi.strafe();
        if (f > 0.05f) keys.setForward(true);
        else if (f < -0.05f) keys.setBack(true);
        if (s > 0.05f) keys.setRight(true); // strafe right = D
        else if (s < -0.05f) keys.setLeft(true); // strafe left = A
        // Jump is edge-triggered; only press if requested and on ground
        if (mi.jump() && onGround) keys.setJump(true);
        keys.setSprint(mi.sprint());
    }

    public KeySimulator keys()   { return keys; }
    public MouseSimulator mouse(){ return mouse; }
    public ClickSimulator clicks(){ return clicks; }
    public InputConfig config()  { return config; }
}
