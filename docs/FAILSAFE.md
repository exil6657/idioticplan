# Failsafe Subsystem

The failsafe system is the last line of defence between a macro running on
Hypixel SkyBlock and an account ban. It is designed to:

1. Detect when the player is in a dangerous/unexpected state.
2. React in a human-looking way (wiggle, look around, fight back, break a
   blocking block, send "?" in chat) while the macro keeps running.
3. Escalate only as far as necessary: pausing, warping to **the private
   island** (`/is`, NOT `/home` — `/home` does not exist in SkyBlock), falling
   back to `/hub`, and finally disconnecting.

Key design choices (per user direction):

- **Low health → fight back, don't flee.** Combat reaction swings at the
  attacker; warp is only used for ban screens / velocity kicks / staff pulls.
- **Obstructions are broken, not fled from.** If blocks are placed to block
  a farming row, pause briefly, look at the block, optionally say "?", break
  it, and continue farming. Only repath if the block can't be cleared.
- **Death is instant-respawn + repath.** Hypixel's SkyBlock death doesn't
  show a real game-over; click respawn immediately and pathfind back to the
  macro location.
- **Teleports / world changes / lagback / limbo → repath, don't stop.** The
  player is re-routed to the active macro destination via ZenithPath, which
  supports walking, sprinting, etherwarp, jump pads, and NPC-driven
  server-swaps.
- **Reactive severities run alongside macros.** Wiggle / combat /
  obstruction / respawn / repath reactions do NOT pause the macro — they
  play concurrently and let the macro keep ticking.

## Architecture

```
FailsafeManager (singleton, ticked by ClientTickDispatcher)
 ├─ FailsafeConfig              ← persisted to config/zenith/failsafe.json
 ├─ 20 × AbstractDetector       ← polled each tick; many also @SubscribeEvent
 ├─ ReactionEngine              ← dispatches bespoke reactions by severity
 │   ├─ WiggleReactionAction        ±20° yaw / ±7° pitch "oops" wiggle
 │   ├─ CombatReactionAction        aim at nearest hostile + swing
 │   ├─ RemoveObstructionAction     look + break blocking block, repath on fail
 │   ├─ RespawnAction               click DeathScreen / player.respawn()
 │   ├─ RepathReactionAction        ZenithPath.requestPath back to anchor
 │   ├─ ChatQuestionMarkAction      send "?" in chat (rate-limited 30 s)
 │   └─ legacy ReactionSequence     Freeze/PanicLook/SlowLook/RandomMovement/
 │                                  AccidentalChat/ChatResponse/EtherwarpEscape/
 │                                  InventoryOpen/MistakeSimulation
 ├─ BanActionHandler            ← /is, /hub, disconnect, respawn (cooldowned)
 ├─ FailsafeSoundPlayer         ← repeated alert sound on trigger
 ├─ PlayerNotifier              ← bounded toast queue for the HUD
 ├─ SafetyStatusMonitor         ← de-bounced coloured status dot
 ├─ PanicButton                 ← PANIC_BUTTON keybind; held 400 ms = disconnect
 └─ TabInHandler                ← pauses on window focus loss (no auto-resume)
```

## Severity ladder

| Severity            | Effect                                                                                    | Macro state |
|---------------------|-------------------------------------------------------------------------------------------|-------------|
| NONE                | Clear — macros & input run normally.                                                      | running     |
| NOTIFY              | Log + toast + sound only.                                                                 | running     |
| WIGGLE_REACT        | Short 0.7–1.2 s camera wiggle, occasional "?" in chat (rotation snap / player-nearby).    | running     |
| COMBAT              | Aim at nearest hostile ≤6 b, swing held item (low health / mob aggro).                    | running     |
| REMOVE_OBSTRUCTION  | Look at blocking block, break it for up to 2.5 s, then repath if it doesn't clear.        | running*    |
| INSTANT_RESPAWN     | Click Respawn on DeathScreen or call `player.respawn()`.                                  | running     |
| REPATH              | Re-pathfind (walk/sprint/etherwarp/jump-pad/NPC) back to the active macro anchor.         | running     |
| PAUSE               | Freeze KeySimulator, stop ZenithPath, disable ZenithEyes, block bits.                     | paused      |
| WARP_ISLAND         | PAUSE + send `/is`.                                                                       | paused      |
| WARP_HUB            | WARP_ISLAND + send `/hub` after 3× escalation steps.                                      | paused      |
| DISCONNECT          | Close the server connection.                                                              | paused      |

\* The obstruction reaction temporarily holds attack via KeySimulator; farming
ticks keep firing so the macro stays in sync, but forward movement is skipped
while the block is being broken.

Each trigger starts at its configured default severity and auto-escalates one
rung every `escalationStepMs` (default 3.5 s) if the condition keeps firing.
`maxAutoSeverity` caps the automatic ladder.

## Detectors

| Detector              | Source                   | Default severity    | Escalates |
|-----------------------|--------------------------|---------------------|-----------|
| PlayerNearby          | Entity scan + chat       | WIGGLE_REACT        | yes       |
| Ban                   | Screen + BanEvent        | DISCONNECT          | yes       |
| Limbo                 | Chat + world heuristics  | REPATH              | yes       |
| Teleport              | Position Δ + packet      | REPATH              | yes       |
| Velocity              | Δv per tick              | WARP_ISLAND         | yes       |
| StaffPull             | Staff/mod presence       | WARP_ISLAND         | yes       |
| ItemSwap              | Held stack change        | WIGGLE_REACT        | no        |
| Rotation              | ZenithEyes Δ yaw/pitch   | WIGGLE_REACT        | no        |
| WorldChange           | WorldChangeEvent         | REPATH              | yes       |
| GUIClose              | InventoryCloseEvent      | NOTIFY              | no        |
| WrongTool             | Expected-vs-held         | WIGGLE_REACT        | no        |
| YawFlip               | >150°/tick yaw           | WIGGLE_REACT        | no        |
| InventoryFull         | Slot fill count          | NOTIFY              | no        |
| Lagback               | >0.8b/tick on ground     | REPATH              | yes       |
| Dismount              | Passenger flag Δ         | WIGGLE_REACT        | no        |
| Death                 | isDead + DeathScreen     | INSTANT_RESPAWN     | yes       |
| LowHealth             | ≤6 HP or hunger 0        | COMBAT              | no        |
| Obstruction           | Stuck + solid block ahead| REMOVE_OBSTRUCTION  | yes       |
| Etherwarp             | Hook for path calls      | PAUSE               | yes       |
| PlayerClone           | Respawn packets          | REPATH              | yes       |
| Disconnect            | Screen + event           | PAUSE               | yes       |

Detectors never move the player, send packets, or toggle modules; they only
report through `FailsafeManager.trigger(type, reason, severity)` / `.clear(type)`.

## Ban actions

- `sendIsland()` → `/is` (private island) cooldown 1.5 s.
- `sendHub()` → `/hub` cooldown 1.5 s.
- `respawn()` → `player.respawn()` (or clicks the DeathScreen Respawn button).
- `sendWarp(name)` → `/warp <name>`.
- `disconnect(reason)` → closes the connection with a 2 s cooldown.

## Rules enforced

1. **No `/home` ever** — use `/is`; `/home` does not exist in SkyBlock and
   would look suspicious.
2. Reactive severities (WIGGLE_REACT through REPATH) use ZenithEyes /
   KeySimulator for all camera/input; no instantaneous rotations or direct
   movement (master rules §2, §3).
3. Reaction chat actions only send "?" — no other automatic chat.
4. Combat reaction never flees; it swings and hopes for the best, escalating
   to /is → /hub → disconnect only if health keeps dropping for several
   seconds despite fighting back.
5. Disconnect always runs on the main thread.
6. Tab-back does NOT auto-resume; player must press resume or issue
   `.z failsafe resume`.
7. BitsSpendBlocker is flipped on at PAUSE+ only; reactive severities do not
   block inventory clicks so the obstruction breaker can still click items.
8. Macro modules register a `RepathReactionAction.DestinationProvider` so the
   repath reaction knows where to walk back to after a teleport/respawn.

## Commands

- `.z failsafe status` — current trigger, severity, reason, reaction.
- `.z failsafe resume` — clear all triggers and resume.
- `.z failsafe test <TYPE>` — fire a synthetic NOTIFY trigger (debug).
- `.z failsafe list` — per-detector enabled state.

## Brain View integration

The DebugBrain panel (`.z debug brain`) shows a Failsafe block: `active`,
`severity`, `type`, `frozen`, `paused`, `reason`, `activeForMs`,
`reactionState`, `total triggers`. SafetyPanel (bottom-left HUD dot):

- Green — NONE
- Amber — NOTIFY / WIGGLE_REACT / COMBAT / REMOVE_OBSTRUCTION / INSTANT_RESPAWN / REPATH
- Orange — PAUSE / WARP_ISLAND / WARP_HUB (label shows "… → /is")
- Red — DISCONNECT

## Open items for later phases

- Eat/drink during combat reaction (hotbar food/potion scan) — Phase 15.
- Repath across NPCs/jump pads more robustly (NPC interaction state machine)
  — Phase 11 (macro framework).
- Discord webhook notification on trigger (Phase 20).
- Auto-reconnect timer after DISCONNECT.
- Staff-vanish detection for tighter PlayerNearbyDetector.
- Macro modules should call `WrongToolDetector.setExpected(...)` at each step
  and `ItemSwapDetector.expectSwap(...)` before intentional swaps.
