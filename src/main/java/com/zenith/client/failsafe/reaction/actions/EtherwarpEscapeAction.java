package com.zenith.client.failsafe.reaction.actions;

import com.zenith.client.engine.eyes.RotationRequest;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.engine.path.PathRequest;
import com.zenith.client.engine.path.PathMode;
import com.zenith.client.failsafe.reaction.ReactionAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Emergency etherwarp escape — tries to teleport the player 20-30 blocks in
 * the direction they're facing, if possible. Used only as a last-resort
 * reaction (e.g. stuck in a place the server thinks is illegal).
 *
 * <p><b>Note:</b> Phase 7 only queues the path request; the actual etherwarp
 * execution is handled by ZenithPath (Phase 3).</p>
 */
public class EtherwarpEscapeAction extends ReactionAction {

    private static final long DURATION = 1500L;
    private boolean fired = false;

    @Override
    protected void onStart(long nowMs) {
        Player p = Minecraft.getInstance().player;
        if (p == null) { fired = true; return; }

        double px = p.getX(), py = p.getY(), pz = p.getZ();
        float yaw = p.getYRot();
        double rad = Math.toRadians(yaw);
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        double tx = px + dx * 25d;
        double tz = pz + dz * 25d;
        double ty = py;

        ZenithEyes.getInstance().setEnabled(true);
        ZenithPath.getInstance().requestPath(PathRequest.builder()
                .from(px, py, pz).to(tx, ty, tz)
                .mode(PathMode.ETHERWARP)
                .maxComputeMs(200)
                .tag("failsafe-escape")
                .build());
        fired = true;
    }

    @Override
    public boolean isDone(long nowMs) { return fired && (nowMs - startedAt) >= DURATION; }

    @Override
    public String label() { return "etherwarp-escape"; }
}
