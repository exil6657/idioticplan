# Failsafe Subsystem — Phase 7

The failsafe system is the last line of defence between a macro running on
Hypixel SkyBlock and an account ban. It is designed to (1) detect when the
player is in a dangerous/unexpected state, (2) look like a confused human
when it fires, and (3) get the player out (pause → /home → /hub → disconnect)
if the condition does not clear.

## Architecture

```
FailsafeManager (singleton, ClientTickDispatcher tick)
 ├─ FailsafeConfig              ← persisted to config/failsafe.json
 ├─ 19 × AbstractDetector       ← polled each tick; many also @SubscribeEvent
 ├─ ReactionEngine              ← mistake-simulation sequences
 │   └─ 9 × ReactionAction      ← Freeze/PanicLook/SlowLook/RandomMovement/
 │                                 AccidentalChat/ChatResponse/EtherwarpEscape/
 │                                 InventoryOpen/MistakeSimulation
 ├─ BanActionHandler            ← /home, /hub, disconnect
 ├─ FailsafeSoundPlayer         ← repeated alert sound on trigger
 ├─ PlayerNotifier              ← bounded toast queue for the HUD
 ├─ SafetyStatusMonitor         ← de-bounced coloured status dot
 ├─ PanicButton                 ← bound to PANIC_BUTTON keybind; held = disconnect
 └─ TabInHandler                ← pauses on window focus loss
```

## Severity ladder

| Severity   | Effect                                                                          |
|------------|---------------------------------------------------------------------------------|
| NONE       | Clear — macros & input run normally.                                            |
| NOTIFY     | Log + toast + sound; input and macros keep running.                              |
| PAUSE      | Freeze `KeySimulator`, stop ZenithPath, disable ZenithEyes, block bits spending.|
| WARP_HOME  | PAUSE + send `/home`.                                                           |
| WARP_SPAWN | WARP_HOME + send `/hub` if still triggered after 3× escalation steps.           |
| DISCONNECT | Kill the server connection.                                                     |

Each trigger starts at its configured default severity and auto-escalates one
rung every `escalationStepMs` (default 3.5 s) if the condition keeps firing.
`maxAutoSeverity` (default DISCONNECT) caps the automatic ladder.

## Detectors

| Detector              | Source              | Default severity | Escalates |
|-----------------------|---------------------|------------------|-----------|
| PlayerNearby          | Entity scan + chat  | PAUSE            | yes       |
| Ban                   | Screen + BanEvent   | DISCONNECT       | yes       |
| Limbo                 | Chat + world heur.  | PAUSE            | yes       |
| Teleport              | Position Δ + packet | PAUSE            | yes       |
| Velocity              | Δv per tick         | WARP_HOME        | yes       |
| ItemSwap              | Held stack change   | PAUSE            | no        |
| Rotation              | ZenithEyes Δ yaw/p  | NOTIFY           | no        |
| WorldChange           | WorldChangeEvent    | PAUSE            | yes       |
| GUIClose              | InventoryCloseEvent | NOTIFY           | no        |
| WrongItem             | Expected-vs-held    | PAUSE            | no        |
| YawFlip               | >150°/tick yaw      | NOTIFY           | no        |
| InventoryFull         | Slot fill count     | NOTIFY           | no        |
| Lagback               | >0.8b/tick on ground| PAUSE            | yes       |
| Dismount              | Passenger flag Δ    | NOTIFY           | no        |
| Death                 | isDead + DeathScreen| PAUSE            | yes       |
| LowHealth             | ≤6 HP or hunger 0   | WARP_HOME        | no        |
| Etherwarp             | Hook for path calls | PAUSE            | yes       |
| PlayerClone           | Respawn packets     | PAUSE            | yes       |
| Disconnect            | Screen + event      | PAUSE            | yes       |

Detectors never move the player, send packets, or toggle modules. They only
report through `FailsafeManager.trigger(type, reason, severity)` / `.clear(type)`.

## Mistake-simulation reactions

Below WARP_HOME severity the reaction engine plays a short (1–3 s) sequence
of human-looking reactions so a trigger doesn't look like a bot that just
halted. Reactions are composed via `ReactionSequenceBuilder` and contain any
combination of:

- `FreezeAction` — Gaussian 250–900 ms of no movement.
- `PanicLookAction` — snappy ±25° eye jerk on FAILSAFE priority.
- `SlowLookAroundAction` — slow ±20–45° sweep on the "legit" profile.
- `MistakeSimulationAction` — two 3–8° twitches then back.
- `InventoryOpenAction` — opens inventory 800 ms as if checking items.
- `AccidentalChatAction` — opens chat, types nothing, closes without sending.
- `ChatResponseAction` — pre-fills chat with a script like "brb" (player sends).
- `RandomMovementAction` — marker for a back-step (we don't auto-move while
  paused — player can override with their own input).
- `EtherwarpEscapeAction` — last-resort 25b etherwarp in the facing direction.

## Rules enforced

1. No automatic key presses while paused — reactions use eye rotation only
   (which runs through the normal ZenithEyes pipeline), never direct movement.
2. Reaction chat actions never send messages; they only open/pre-fill chat.
3. Disconnect is the final action and always runs on the main thread.
4. Tab-back does NOT auto-resume macros; player must press resume or issue
   `.z failsafe resume`.
5. BitsSpendBlocker is globally flipped on at PAUSE+, blocking all inventory
   clicks unconditionally.

## Commands

- `.z failsafe status` — print current trigger, severity, reason, reaction.
- `.z failsafe resume` — clear all triggers and resume.
- `.z failsafe test <TYPE>` — fire a synthetic NOTIFY trigger (debug).
- `.z failsafe list` — print per-detector enabled state.

## Brain View integration

The DebugBrain panel (`.z debug brain`) now shows a Failsafe block:
`active`, `severity`, `type`, `frozen`, `paused`, `reason`, `activeForMs`,
`reactionState`, `total triggers`. The SafetyPanel (bottom-left HUD dot) shows
a coloured indicator: green / amber / orange / red.

## Open items for later phases

- Discord webhook notification on trigger (Phase 18).
- Auto-reconnect timer after DISCONNECT (config option already present, not
  yet wired).
- Staff-vanish detection (check player game-mode / NPC flags) for a tighter
  PlayerNearbyDetector.
- EtherwarpDetector should subscribe to a "completed etherwarp" event from
  ZenithPath (added when etherwarp execution is fully wired).
- Macro modules should call `WrongItemDetector.setExpected(...)` at each step
  and `ItemSwapDetector.expectSwap(...)` before intentional swaps.
