package com.zenith.client.macro.farming;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.macro.MacroModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Base for all row-based farming macros (pumpkin, melon, wheat, carrot, potato,
 * sugarcane, cocoa, cactus, wart, mushroom). Provides:
 * - anchor capture for RepathReactionAction
 * - row-length + U-turn logic (was missing — caused wall crash)
 * - correct yaw→look vector math
 * - tool validation hook
 *
 * <p>Concrete subclasses supply {@link #targetYaw()} for row direction,
 * {@link #cropMatcher(BlockState)} for which blocks to break.</p>
 */
public abstract class AbstractFarmingMacro extends MacroModule {

    public enum FarmingState { FORWARD, U_TURN, STRAFE, RESYNC }

    protected int forwardTicks;
    protected boolean returning;
    protected FarmingState state = FarmingState.FORWARD;
    private long stateEnteredMs;
    private float rowLength = 100f; // blocks; override via rowLength()
    private double anchorX, anchorY, anchorZ;
    private int rowsFarmed = 0;

    /** Override to configure row length for your farm design. */
    protected float rowLength() { return 100f; }

    /** Override to configure row spacing (blocks to strafe during U-turn). */
    protected float rowSpacing() { return 2.5f; }

    /** Expected tool SB id — used by WrongToolDetector. */
    protected String expectedToolId() { return null; }

    @Override protected void onStart() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        anchorX = mc.player.getX();
        anchorY = mc.player.getY();
        anchorZ = mc.player.getZ();
        forwardTicks = 0;
        returning = false;
        rowsFarmed = 0;
        state = FarmingState.FORWARD;
        stateEnteredMs = System.currentTimeMillis();
        rowLength = rowLength();
        if (mc.player != null) {
            ZenithEyes.getInstance().setEnabled(true);
            ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                    .yaw(targetYaw()).pitch(-35f)
                    .priority(RotationRequest.Priority.MACRO)
                    .durationMs(350L)
                    .profile("farming-lookdown")
                    .tag("farming:start")
                    .build());
        }
        // register expected tool for failsafe (future wiring)
    }

    // ---- Repath anchor (MacroModule implements DestinationProvider) ----
    @Override public double destX() { return anchorX; }
    @Override public double destY() { return anchorY; }
    @Override public double destZ() { return anchorZ; }
    @Override public String destinationDescription() { return displayName() + \" anchor [\" + rowsFarmed + \" rows]\"; }

    @Override protected void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        var keys = InputEngine.getInstance().keys();
        if (keys == null) return;

        long now = System.currentTimeMillis();

        switch (state) {
            case FORWARD -> tickForward(mc, keys, now);
            case U_TURN -> tickUTurn(mc, keys, now);
            case STRAFE -> tickStrafe(mc, keys, now);
            case RESYNC -> tickResync(mc, keys, now);
        }
    }

    private void tickForward(Minecraft mc, com.zenith.client.engine.input.KeySimulator keys, long now) {
        keys.setForward(true);
        keys.setBack(false);
        keys.setLeft(false);
        keys.setRight(false);
        keys.setSprint(false);

        // Correct yaw → look vector: MC yaw 0=south, -90=east.
        // dir: x = -sin(yaw), z = cos(yaw)
        float yaw = targetYaw();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(35f); // 35° down, not 50
        double xLook = -Math.sin(yawRad) * Math.cos(pitchRad);
        double zLook = Math.cos(yawRad) * Math.cos(pitchRad);
        double yLook = -Math.sin(pitchRad);
        Vec3 eyes = mc.player.getEyePosition(1f);
        Vec3 look = new Vec3(xLook, yLook, zLook);
        // Check 0.5, 1.0, 1.5 ahead for crop
        boolean cropInFront = false;
        for (double d = 0.5d; d <= 1.6d; d += 0.5d) {
            BlockPos target = BlockPos.containing(eyes.add(look.scale(d)));
            BlockState st = mc.level.getBlockState(target);
            if (cropMatcher(st)) { cropInFront = true; break; }
        }
        keys.setAttack(cropInFront);

        // Keep yaw/pitch on target (smooth).
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(yaw).pitch(-35f)
                .priority(RotationRequest.Priority.MACRO_TICK)
                .durationMs(120L)
                .profile("farming-row")
                .tag("farming:row")
                .preemptible(true)
                .build());
        forwardTicks++;

        // Row end detection: after rowLength blocks *20 ticks/block approx 4.3 m/s walk = ~4.65 ticks per block -> rough
        // More reliable: track distance from anchor projected onto yaw axis.
        double dx = mc.player.getX() - anchorX;
        double dz = mc.player.getZ() - anchorZ;
        double proj = dx * (-Math.sin(yawRad)) + dz * Math.cos(yawRad);
        if (Math.abs(proj) >= rowLength || forwardTicks > rowLength * 8) {
            transition(FarmingState.U_TURN, now);
            keys.setForward(false);
            keys.setAttack(false);
        }
    }

    private void tickUTurn(Minecraft mc, com.zenith.client.engine.input.KeySimulator keys, long now) {
        keys.setForward(false);
        keys.setAttack(false);
        // Yaw 180° turn over ~600-800ms
        float newYaw = targetYaw() + 180f;
        // Wrap
        if (newYaw > 180f) newYaw -= 360f;
        if (newYaw < -180f) newYaw += 360f;
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(newYaw).pitch(-15f)
                .priority(RotationRequest.Priority.MACRO)
                .durationMs(650L)
                .profile("farming-uturn")
                .tag("farming:uturn")
                .preemptible(false)
                .build());
        if (now - stateEnteredMs > 750) {
            transition(FarmingState.STRAFE, now);
        }
    }

    private void tickStrafe(Minecraft mc, com.zenith.client.engine.input.KeySimulator keys, long now) {
        // Strafe sideways ~ rowSpacing blocks
        if (returning) keys.setLeft(true);
        else keys.setRight(true);
        keys.setForward(true);
        if (now - stateEnteredMs > (long)(rowSpacing() * 650)) {
            keys.setLeft(false);
            keys.setRight(false);
            keys.setForward(false);
            // flip target yaw for return pass
            returning = !returning;
            rowsFarmed++;
            // update anchor for new row start
            anchorX = mc.player.getX();
            anchorY = mc.player.getY();
            anchorZ = mc.player.getZ();
            forwardTicks = 0;
            transition(FarmingState.RESYNC, now);
        }
    }

    private void tickResync(Minecraft mc, com.zenith.client.engine.input.KeySimulator keys, long now) {
        keys.setForward(false);
        float yaw = targetYaw();
        if (returning) {
            yaw += 180f;
            if (yaw > 180f) yaw -= 360f;
            if (yaw < -180f) yaw += 360f;
        }
        ZenithEyes.getInstance().requestRotation(RotationRequest.builder()
                .yaw(yaw).pitch(-35f)
                .priority(RotationRequest.Priority.MACRO)
                .durationMs(400L)
                .profile("farming-resync")
                .tag("farming:resync")
                .build());
        if (now - stateEnteredMs > 500) {
            transition(FarmingState.FORWARD, now);
        }
    }

    private void transition(FarmingState next, long now) {
        this.state = next;
        this.stateEnteredMs = now;
    }

    @Override protected void onStop() {
        var keys = InputEngine.getInstance().keys();
        if (keys != null) keys.halt();
        ZenithEyes.getInstance().setEnabled(true); // don't disable entirely, just clear queue
        try { com.zenith.client.engine.path.ZenithPath.getInstance().stop(); } catch (Throwable ignored) {}
    }

    @Override protected void onFailsafePause() {
        var keys = InputEngine.getInstance().keys();
        if (keys != null) { keys.setForward(false); keys.setAttack(false); keys.setLeft(false); keys.setRight(false); }
    }

    /** Yaw (degrees) the player should face to walk along the crop row. */
    protected abstract float targetYaw();

    /** @return true when the given state is a crop this macro should break. */
    protected abstract boolean cropMatcher(BlockState state);
}
