package com.zenith.client.failsafe.reaction;

import com.zenith.client.ZenithClient;
import com.zenith.client.core.util.MathUtils;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.failsafe.FailsafeStrictness;
import com.zenith.client.failsafe.FailsafeType;
import com.zenith.client.failsafe.reaction.actions.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * When a failsafe triggers, we don't just freeze — we play a short
 * human-looking reaction sequence: a nervous look-around, maybe a step back,
 * close the screen, type something in chat, etc. This makes the client look
 * like a confused human rather than a bot that just halted.
 *
 * <p>Sequences are short (1–3 seconds) and only play up to PAUSE severity;
 * by the time we reach WARP_HOME/DISCONNECT we just execute the escape.</p>
 */
public final class ReactionEngine {

    private final FailsafeManager mgr;
    private final Map<FailsafeType, List<ReactionAction>> sequences = new EnumMap<>(FailsafeType.class);
    private final Random rng = new Random();

    private List<ReactionAction> running;
    private int index = -1;
    private long startedAt;
    private String currentLabel = "";

    ReactionEngine(FailsafeManager mgr) { this.mgr = mgr; }

    public void init() {
        // Player nearby: brief pause, look at the player, panic-look a bit, step back slightly.
        sequences.put(FailsafeType.PLAYER_NEARBY, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)                 // ~400-700ms freeze
                .then(PanicLookAction::new)              // snap 20-60° towards random direction
                .then(SlowLookAroundAction::new)         // slow human look around
                .then(RandomMovementAction::new)         // tiny backward strafe
                .build());

        // Teleport: freeze longer, re-orient.
        sequences.put(FailsafeType.TELEPORT, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(SlowLookAroundAction::new)
                .build());

        // GUI close: "what happened?" momentary freeze, accidental-chat opener.
        sequences.put(FailsafeType.GUI_CLOSE, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(() -> new AccidentalChatAction(false))
                .build());

        // Wrong item: "oh wrong key" — open inventory.
        sequences.put(FailsafeType.WRONG_ITEM, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(InventoryOpenAction::new)
                .then(MistakeSimulationAction::new)
                .build());

        sequences.put(FailsafeType.ITEM_DESELECT, sequences.get(FailsafeType.WRONG_ITEM));

        // Yaw flip / rotation reset: surprise look-around.
        sequences.put(FailsafeType.YAW_FLIP, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(SlowLookAroundAction::new)
                .build());

        sequences.put(FailsafeType.ROTATION_RESET, sequences.get(FailsafeType.YAW_FLIP));

        // Inventory full: pretend to notice, type "inv full" in chat (doesn't send — just opens chat).
        sequences.put(FailsafeType.INVENTORY_FULL, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(() -> new AccidentalChatAction(true))
                .build());

        // Lagback/velocity: mistake jitter.
        sequences.put(FailsafeType.LAGBACK, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(MistakeSimulationAction::new)
                .build());
        sequences.put(FailsafeType.VELOCITY_KICK, sequences.get(FailsafeType.LAGBACK));

        // Ban / disconnect / death: no reaction — just let BanActionHandler do its thing.
        // Limbo: nothing.
        // World change: small freeze + look.
        sequences.put(FailsafeType.WORLD_CHANGE, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(SlowLookAroundAction::new)
                .build());

        sequences.put(FailsafeType.DISMOUNT, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(SlowLookAroundAction::new)
                .build());

        ZenithClient.LOGGER.info("[ReactionEngine] Loaded {} reaction sequences.", sequences.size());
    }

    public void onTrigger(FailsafeType type, String reason, FailsafeStrictness severity, long nowMs) {
        if (severity.level() >= FailsafeStrictness.WARP_HOME.level()) {
            // Too late for reaction theatrics — escape in progress.
            running = null;
            index = -1;
            currentLabel = "escaping";
            return;
        }
        List<ReactionAction> seq = sequences.get(type);
        if (seq == null || seq.isEmpty()) {
            running = null;
            index = -1;
            currentLabel = "";
            return;
        }
        running = new ArrayList<>(seq);
        index = 0;
        startedAt = nowMs;
        startCurrent(nowMs);
    }

    public void tick(long nowMs) {
        if (running == null || index < 0 || index >= running.size()) return;
        ReactionAction a = running.get(index);
        a.tick(nowMs);
        currentLabel = a.label();
        if (a.isDone(nowMs)) {
            a.cancel();
            index++;
            if (index < running.size()) {
                startCurrent(nowMs);
            } else {
                running = null;
                index = -1;
                currentLabel = "done";
            }
        }
    }

    public void onClear() {
        if (running != null) {
            for (ReactionAction a : running) a.cancel();
        }
        running = null;
        index = -1;
        currentLabel = "";
    }

    public String stateName() {
        if (running == null || index < 0) return currentLabel.isEmpty() ? "idle" : currentLabel;
        return currentLabel;
    }

    private void startCurrent(long nowMs) {
        ReactionAction a = running.get(index);
        try {
            a.start(nowMs);
            currentLabel = a.label();
        } catch (Throwable t) {
            ZenithClient.LOGGER.error("[ReactionEngine] action {} failed", a.label(), t);
            index++;
            if (index < running.size()) startCurrent(nowMs);
        }
    }
}
