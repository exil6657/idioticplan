package com.zenith.client.failsafe.reaction;

import com.zenith.client.ZenithClient;
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
 * When a failsafe triggers, we don't just freeze — we play a short human-looking
 * reaction sequence appropriate to the situation (see FailsafeStrictness docs):
 *
 * <ul>
 *   <li><b>WIGGLE_REACT</b> — rotation snap/player-nearby/wrong-tool: wiggle
 *       camera, send "?" in chat if warranted, then resume.</li>
 *   <li><b>COMBAT</b> — low health/hunger: look at attacker, swing to fight
 *       back (NOT warp).</li>
 *   <li><b>REMOVE_OBSTRUCTION</b> — block in the way: pause, look at block,
 *       maybe "?", break the block, resume.</li>
 *   <li><b>INSTANT_RESPAWN</b> — death: click respawn immediately.</li>
 *   <li><b>REPATH</b> — teleport/world-change/lagback/limbo/post-respawn:
 *       re-pathfind back to the macro destination.</li>
 *   <li><b>WARP_ISLAND/HUB/DISCONNECT</b> — handled directly by
 *       {@link com.zenith.client.failsafe.BanActionHandler}; reactions stop here.</li>
 * </ul>
 *
 * <p>All rotations go through ZenithEyes (rule §2) and all key presses go
 * through KeySimulator (no instantaneous movements, rule §3).</p>
 */
public final class ReactionEngine {

    private final FailsafeManager mgr;
    @SuppressWarnings("unused")
    private final Map<FailsafeType, List<ReactionAction>> sequences = new EnumMap<>(FailsafeType.class);
    @SuppressWarnings("unused")
    private final Random rng = new Random();

    private List<ReactionAction> running;
    private int index = -1;
    private long startedAt;
    private String currentLabel = "";

    ReactionEngine(FailsafeManager mgr) { this.mgr = mgr; }

    public void init() {
        // Classic "oops" sequences for minor triggers.
        sequences.put(FailsafeType.PLAYER_NEARBY, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(PanicLookAction::new)
                .then(SlowLookAroundAction::new)
                .then(RandomMovementAction::new)
                .build());

        sequences.put(FailsafeType.WRONG_TOOL, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(InventoryOpenAction::new)
                .then(MistakeSimulationAction::new)
                .build());
        sequences.put(FailsafeType.ITEM_DESELECT, sequences.get(FailsafeType.WRONG_TOOL));
        sequences.put(FailsafeType.DISMOUNT, sequences.get(FailsafeType.WRONG_TOOL));

        sequences.put(FailsafeType.GUI_CLOSE, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(() -> new AccidentalChatAction(false))
                .build());

        sequences.put(FailsafeType.INVENTORY_FULL, ReactionSequenceBuilder.create()
                .then(FreezeAction::new)
                .then(() -> new AccidentalChatAction(true))
                .build());

        ZenithClient.LOGGER.info("[ReactionEngine] Loaded {} reaction sequences (wiggle/combat/obstruction/respawn/repath enabled).", sequences.size());
    }

    public void onTrigger(FailsafeType type, String reason, FailsafeStrictness severity, long nowMs) {
        // Dispatch purpose-built reactions by severity tier.
        switch (severity) {
            case WIGGLE_REACT -> {
                WiggleReactionAction.trigger();
                if (type == FailsafeType.ROTATION_RESET || type == FailsafeType.YAW_FLIP
                        || type == FailsafeType.PLAYER_NEARBY) {
                    ChatQuestionMarkAction.send();
                }
                currentLabel = "wiggle";
                return;
            }
            case COMBAT -> {
                CombatReactionAction.trigger();
                currentLabel = "combat";
                return;
            }
            case REMOVE_OBSTRUCTION -> {
                RemoveObstructionAction.trigger();
                currentLabel = "remove-obstruction";
                return;
            }
            case INSTANT_RESPAWN -> {
                RespawnAction.trigger();
                // After respawn fires the DEATH detector clears; REPATH takes over
                // via WORLD_CHANGE/TELEPORT if the position is wrong.
                currentLabel = "respawn";
                return;
            }
            case REPATH -> {
                RepathReactionAction.trigger(type.displayName() + ": " + reason);
                currentLabel = "repath";
                return;
            }
            case WARP_ISLAND, WARP_HUB, DISCONNECT -> {
                running = null;
                index = -1;
                currentLabel = "escaping";
                return;
            }
            default -> { /* NOTIFY/NONE/PAUSE: no bespoke reaction */ }
        }

        // Fall back to the old "freeze + theatrics" sequences for NOTIFY/PAUSE.
        List<ReactionAction> seq = sequences.get(type);
        if (seq == null || seq.isEmpty()) {
            running = null; index = -1; currentLabel = ""; return;
        }
        running = new ArrayList<>(seq);
        index = 0;
        startedAt = nowMs;
        startCurrent(nowMs);
    }

    public void tick(long nowMs) {
        // Drive always-on action tickers.
        WiggleReactionAction.tick();
        CombatReactionAction.tick();
        RemoveObstructionAction.tick();
        if (RespawnAction.consumeTriggered()) {
            RepathReactionAction.trigger("post-respawn");
        }

        // Drive legacy sequence actions.
        if (running == null || index < 0 || index >= running.size()) return;
        ReactionAction a = running.get(index);
        a.tick(nowMs);
        currentLabel = a.label();
        if (a.isDone(nowMs)) {
            a.cancel();
            index++;
            if (index < running.size()) startCurrent(nowMs);
            else { running = null; index = -1; currentLabel = WiggleReactionAction.isActive() ? "wiggle"
                    : CombatReactionAction.isActive() ? "combat"
                    : RemoveObstructionAction.isActive() ? "remove-obstruction"
                    : "done"; }
        }
    }

    public void onClear() {
        if (running != null) for (ReactionAction a : running) a.cancel();
        running = null; index = -1; currentLabel = "";
        WiggleReactionAction.cancel();
        CombatReactionAction.cancel();
    }

    public String stateName() {
        if (running != null && index >= 0) return currentLabel;
        if (WiggleReactionAction.isActive()) return "wiggle";
        if (CombatReactionAction.isActive()) return "combat";
        if (RemoveObstructionAction.isActive()) return "remove-obstruction";
        return currentLabel.isEmpty() ? "idle" : currentLabel;
    }

    private void startCurrent(long nowMs) {
        ReactionAction a = running.get(index);
        try { a.start(nowMs); currentLabel = a.label(); }
        catch (Throwable t) {
            ZenithClient.LOGGER.error("[ReactionEngine] action {} failed", a.label(), t);
            index++;
            if (index < running.size()) startCurrent(nowMs);
        }
    }
}
