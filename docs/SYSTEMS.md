# Zenith Client — Complete System Breakdown

**Audience:** Developer (you + me). No marketing copy. This document lists
every subsystem that exists, how it *actually* works today, where I'm making
guesses (marked `[NEEDS DEVDATA]`), what is known-broken, and the acceptance
criteria for completing each system. Each system is a standalone unit of work
that can be built, tested, and verified individually.

**Versions:** Minecraft 26.1.2 (Java 21 bytecode), Fabric Loader 0.18.9, FAPI
0.145.4+26.1.2, Loom 1.15, Gradle 9.4, JDK 25 at dev-time.

**Conventions in this document:**
- ✅ **Implemented and believed-correct.**
- 🟡 **Implemented but unverified / best-guess / may break.**
- ❌ **Stub / not implemented.**
- 🔬 **Requires DevDataMacro in-game tour to verify against real Hypixel.**

---

## Table of Systems

1. [Mod Core & Initialisation](#1-mod-core--initialisation)
2. [Mixin Layer](#2-mixin-layer)
3. [Stealth / Anti-Fingerprint](#3-stealth--anti-fingerprint)
4. [Event Bus](#4-event-bus)
5. [Tick Dispatch](#5-tick-dispatch)
6. [Config System](#6-config-system)
7. [ZenithEyes — Rotation Engine](#7-zenitheyes--rotation-engine)
8. [ZenithPath — Movement / Pathfinding](#8-zenithpath--movement--pathfinding)
9. [InputEngine — Key/Mouse Simulation](#9-inputengine--keymouse-simulation)
10. [DelayManager — Humanised Timings](#10-delaymanager--humanised-timings)
11. [GUI Automation Layer](#11-gui-automation-layer)
12. [Sign Input Handler](#12-sign-input-handler)
13. [SkyBlock Navigator / Travel Engine](#13-skyblock-navigator--travel-engine)
14. [Auction House Interactor](#14-auction-house-interactor)
15. [Bazaar Interactor](#15-bazaar-interactor)
16. [Failsafe System](#16-failsafe-system)
17. [Reaction Actions](#17-reaction-actions)
18. [Failsafe Detectors](#18-failsafe-detectors)
19. [Flipper — Top-Level](#19-flipper--top-level)
20. [Market Scanner](#20-market-scanner)
21. [API Clients & Caches](#21-api-clients--caches)
22. [Tax Calculator](#22-tax-calculator)
23. [Item Filter / Selector](#23-item-filter--selector)
24. [Budget Manager](#24-budget-manager)
25. [Order Manager & Lifecycle](#25-order-manager--lifecycle)
26. [Flip Engines (Bazaar/AH/NPC/Craft/AHCraft)](#26-flip-engines)
27. [Fill Monitor / Relist / Undercut](#27-fill-monitor--relist--undercut)
28. [Profit Tracker & History](#28-profit-tracker--history)
29. [Break Scheduler & Idle Behaviour](#29-break-scheduler--idle-behaviour)
30. [Mayor System](#30-mayor-system)
31. [Macro Framework (MacroModule / MacroManager)](#31-macro-framework)
32. [Macros — Farming Base](#32-macros--farming-base)
33. [Macros — Melon / Pumpkin](#33-macros--melon--pumpkin)
34. [Macros — Other Crops (Carrot/Potato/Wheat/…)](#34-other-crops)
35. [Macros — Garden Subsystems (Pest/Jacob/Visitor/Composter/Greenhouse)](#35-garden-subsystems)
36. [Macros — Mining](#36-macros--mining)
37. [Macros — Combat](#37-macros--combat)
38. [Macros — Fishing / Foraging / Events / Dungeons / Kuudra / Rift / Misc](#38-other-macros)
39. [DevData Harvester & Tour Macro](#39-devdata)
40. [Command System (dot-prefix `.z`)](#40-command-system)
41. [Chat System & Pattern Engine](#41-chat-system)
42. [World Hook & Player State Trackers](#42-world-hook--player-state-trackers)
43. [GUI Framework (ZenithScreen / ZenithScreenWrapper)](#43-gui-framework)
44. [Dashboard GUI](#44-dashboard-gui)
45. [Brain View (Debug HUD overlay)](#45-brain-view)
46. [HUD Panels](#46-hud-panels)
47. [Theme & Language Managers](#47-theme--language-managers)
48. [Keybind System](#48-keybind-system)
49. [Bits Protection](#49-bits-protection)
50. [Discord Webhook / Notifications](#50-discord--notifications)
51. [Build / Obfuscation / Distribution](#51-build--obfuscation)
52. [Asset Pipeline (sounds / textures / lang)](#52-asset-pipeline)

---

## 1. Mod Core & Initialisation

### Current state ✅
- Entrypoint `ZenithClient.onInitializeClient()` (implements `ClientModInitializer`).
- Init order in ZenithClient.java (lines 60-127):
  1. `ConfigManager.init()`
  2. `ZenithEventBus.init()`
  3. `KeybindManager.init()`
  4. `ZenithChat` initial message
  5. `CommandManager.registerAll()`
  6. `ModuleManager.registerAll()`
  7. `ZenithEyes.init()` / `ZenithPath.init()` / `InputEngine.init()`
  8. `GuiEngine.init()` / `HudManager.init()`
  9. `ChatPatternEngine.init()` / `WorldHook.init()`
  10. `FailsafeManager.init()`
  11. `MacroManager.register(...)` (Idle, Melon, Pumpkin, DevData)
  12. `ApiManager.init()` (NEU/Moulberry/Coflnet fetchers)
  13. `FlipEngine.init()`
  14. `DevDataHarvester.init()`
  15. `ClientTickDispatcher.register()`
  16. `BitsProtection.init()`

### Known issues 🟡
- Initialisation order is mostly-correct but `ApiManager` is started AFTER
  `FlipEngine`, which immediately tries to scan via caches that are still
  empty. There's a 10 s delay inside MarketScanner so this works by accident
  in practice, but it's architecturally wrong.
- Banner log message still says "Phase 10" even though Phase 11 work is
  underway.

### To do
- [ ] Re-order so `ApiManager.init()` starts fetches before `FlipEngine` and
      `BazaarFlipEngine`/`MarketScanner`.
- [ ] Add an explicit `systemsReady` flag that prevents ticking subsystems
      until init completes (currently every tick method null-guards).
- [ ] Remove hard-coded Phase banner, replace with version string from
      `ZenithClientInfo`.

---

## 2. Mixin Layer

### Current state ✅ (but risky)
14 mixins + 3 accessors listed in `zenithclient.mixins.json`, all with
`require=0` on injections where the target could move across versions.

| Mixin | Target | Purpose | Confidence |
|---|---|---|---|
| `MixinMinecraft` | `net.minecraft.client.Minecraft` | Tick hook for ClientTickEvent | ✅ |
| `MixinClientConnection` | `net.minecraft.network.Connection` | Intercept outgoing chat (`.z` prefix) + incoming chat events; spoof client brand to "vanilla" | 🟡 |
| `MixinGameRenderer` | `net.minecraft.client.renderer.GameRenderer` | Render hook for Brain View / HUD | ✅ |
| `MixinMouse` | Mouse | Event bus mouse events, keybind dispatch | ✅ |
| `MixinKeyboardHandler` | Keyboard handler | Key events, keybind dispatch | 🟡 — method names `onPress(JIII)V` / `keyPress(JIIII)V` not verified in 26.1 |
| `MixinGui` / `MixinScreen` | Gui/Screen | GUI open/close events (InventoryOpenEvent / InventoryCloseEvent) | ✅ |
| `MixinLevelRenderer` | LevelRenderer | World render hooks | ✅ |
| `MixinEntityRenderer` | EntityRenderer | Nametag/player ESP hooks (for player-nearby detection) | ✅ |
| `MixinOptions` | Options | Keybind defaults access | 🟡 |
| `MixinCamera` | Camera | ZenithEyes rotation sync (overrides yaw/pitch) | 🔬 — camera `setup` method signature may have shifted |
| `MixinPlayerRenderer` | PlayerRenderer | Nametag visibility | ✅ |
| `MixinChatScreen` | ChatScreen | Placeholder — currently does nothing | ❌ |
| `MixinCommandSuggestions` | CommandSuggestions | Suppress Brigadier suggestions for `.z` commands and inject our own tab-completions | 🔬 — shadow fields `input`/`commandUsage`/`rawCommandUsage`/`pendingSuggestions` are guesses |

Accessors: `MouseAccessor`, `OptionsAccessor`, `CameraAccessor`.

### To do
- [ ] On first real dev build, fix any shadow/redirect targets that fail to
      apply (mixins are `require=0` so failure is silent — check logs for
      "injection error" warnings).
- [ ] Add `MixinEntity` to expose `onGround`/`horizontalCollision` / `noPhysics`
      flags reliably for stuck detection.
- [ ] Remove `MixinChatScreen` placeholder or implement it (should suppress
      vanilla command error toast when `.z …` is sent).

---

## 3. Stealth & Anti-Fingerprint

### Current state 🟡
- **Client brand string** spoofed to "vanilla" via mixin (✅).
- **Dot commands** intercepted before they hit the wire (`.z …` stripped and
  never sent to server) ✅.
- **No Brigadier commands registered** ✅ (rule §4).
- **No custom plugin channels** ✅.
- **Tab completion** injected client-side via mixin ✅.
- **All GUI clicks** go through `handleInventoryMouseClick` (real packet, same
  as a human clicking) ✅.
- **All sign input** goes through `SignEditScreen.onDone()` reflectively —
  same code path a human triggers (🟡 see SignInputHandler).

### Known gaps ❌ (severe — ban risk)
- **Rotation velocity is perfectly linear.** Human head movement accelerates
  and decelerates; ZenithEyes currently lerps at constant angular velocity.
  Watchdog flags this.
- **Key holds are perfectly steady.** `setForward(true)` tick after tick
  produces no key-jitter, no micro-releases, no sneeze-pauses. Real players
  have tiny blips.
- **Click timing** uses Gaussian-distributed delays between GUI clicks (good),
  but attack/use key holds while farming/combat are a solid held boolean
  (bad).
- **Packet rate is perfectly uniform** when rotating/moving (no jitter).
- **Camera snaps** when failsafe fires (FAILSAFE priority overrides instantly).
  Real players take 50–80 ms to react.
- **No anti-spectator check.** Staff in spectator mode within 3 blocks should
  trigger STAFF_PULL, but our entity-distance check doesn't filter
  spectator/adventure game mode.
- **Client-side swing animation is suppressed** when we attack via
  `setAttack(true)` — other players see the swing packet but the local client
  doesn't render it (minor, other mods have the same trait).
- **Screenshot key** doesn't flush the screen of Zenith UI (if a player
  screenshots and sends to staff, the dashboard/HUD would be visible — need
  a "panic hide" key that renders nothing on F2).

### Acceptance criteria
- [ ] ZenithEyes uses easing (ease-in-out cubic) on all rotations, not linear
      lerp. Add per-profile fatigue/noise.
- [ ] KeySimulator injects Gaussian micro-pauses (20–80ms releases on forward
      every 3–10 s) when sustained input is held >10 s.
- [ ] FAILSAFE-priority rotation still ramps over 60–80 ms, never instant.
- [ ] Add spectator/staff gamemode check to PlayerNearbyDetector.
- [ ] F2 interception: unregister all HUD render layers during the screenshot
      frame, restore after.
- [ ] Send brand "vanilla" verified via packet log (DevData).

---

## 4. Event Bus

### Current state ✅
- `ZenithEventBus.getInstance()` singleton.
- `register(Object subscriber)` reflects over methods annotated
  `@SubscribeEvent` (from `com.zenith.client.core.event.annotation.SubscribeEvent`).
- `post(Event event)` dispatches to subscribers by parameter type.
- Event types in `core/event/events/`:
  - `ClientTickEvent`, `RenderEvent`, `WorldChangeEvent`,
  - `InventoryOpenEvent`, `InventoryCloseEvent`,
  - `ChatReceivedEvent`, `ActionBarEvent`, `ScoreboardUpdateEvent`,
  - `BossBarEvent`, `TabListUpdateEvent`,
  - `BreakStartEvent`, `BreakEndEvent`,
  - `FlipCompleteEvent`, `FailsafeTriggerEvent`,
  - Player move / key / mouse events.
- Priority levels: `EventPriority.HIGHEST/HIGH/NORMAL/LOW/LOWEST`.

### Known gaps
- No unregister (subscribers are held for the life of the mod — fine).
- No async event posting (all on main thread, which is correct).
- `@SubscribeEvent` listener discovery happens at register-time via reflection
  — fast enough (few hundred subscribers).

### Acceptance criteria
- [ ] Verify all 21 detectors + FlipEngine + GUIInteractionEngine +
      ChatPatternEngine + WorldHook are subscribed.
- [ ] Add event-bus stats (counts per event type) to Brain View for debug.

---

## 5. Tick Dispatch

### Current state ✅
`ClientTickDispatcher.register()` subscribes a singleton subscriber to
`ClientTickEvent`. Each tick it dispatches (in order):

1. `PlayerPositionTracker.tick()` (only if `World.get().playerReady()`)
2. `PlayerHealthMonitor.tick()` (ditto)
3. `ModuleManager.tickAll()`
4. `InputEngine.tick()`
5. `MovementLearner.tick()`
6. `FailsafeManager.tick()`
7. `MacroManager.tick()`
8. `TravelEngine.tick()`
9. `FlipEngine.tick()`
10. `ConfigManager.tick()` (debounced save)

`GUIInteractionEngine.register()` separately subscribes to tick and drives
`GUIParser.tick()`, `SignInputHandler.tick()`, `GUIWaiter.checkAll()`,
`GUIClickExecutor.tick()`.

### Issues 🟡
- Failsafe ticks AFTER macros; if a macro tick sets up a rotation, failsafe
  can't override it until next tick (1 tick = 50 ms, acceptable).
- `GUIInteractionEngine` subscribes itself independently but dispatch order
  relative to other tick subscribers isn't deterministic — GUI state can be
  one tick stale.
- Render dispatch (GuiEngine / HudManager) is called from MixinGameRenderer
  which fires every frame (not every tick), which is correct.

---

## 6. Config System

### Current state ✅ (newly added)
- `ConfigManager` singleton.
- GSON-backed, 13 files under `<gameDir>/config/zenithclient/`.
- Debounced save via `requestSave()` → written on next tick.
- `register(filename, class, getter, setter, initial)` API.
- `MainConfig` (zenith.json) with fields:
  - `commandPrefix = ".z"`
  - `language = "en_gb"`
  - `theme = "dark"`
  - `customThemeHue = 0.58f`
  - `openDashboardOnStart = false`
  - `devDataAuto = true`
  - `keybinds = Map<String,String>` (configId → GLFW name)

### Known gaps
- [ ] Only the failsafe config is currently registered via `register(...)`
      (the other 11 files: budget, debug, filter, history, hud_layout, lock,
      npc, player, routes, session, waypoints) have **POJO classes but are
      not yet registered** with ConfigManager. Each subsystem currently reads
      its own JSON ad-hoc or uses defaults.
- [ ] No schema-version migration (bumping `schemaVersion` doesn't do
      anything yet).
- [ ] Atomic write writes to `.tmp` then moves — good, but doesn't handle
      crash-during-write recovery beyond overwriting on next load.

### Acceptance criteria
- [ ] Register every subsystem config file with ConfigManager.
- [ ] Add a migration hook (old schema → new schema).
- [ ] Write unit tests for load/save defaults.

---

## 7. ZenithEyes — Rotation Engine

### Current state 🟡
- `RotationRequest` builder:
  `.yaw(float).pitch(float).priority(Priority).durationMs(long)
   .profile(String).tag(String).tracking(bool).preemptible(bool).onComplete(Runnable)`
- Priorities (low → high): `BACKGROUND, WANDER, MACRO, COMBAT, MACRO_TICK, FAILSAFE`.
- Higher priority preempts lower if `preemptible=true`.
- `setEnabled(boolean)` disables and clears queue; re-enables camera sync.
- Smooth interpolation over `durationMs` toward target.
- `RotationDebugData` exposes state/activeTag/currentYaw/currentPitch/targetYaw/targetPitch/angularVelocity/overshooting/correcting/hesitating/fatigue/queued/priority.

### Known gaps ❌
- **Interpolation is linear** (constant angular velocity), not ease-in-out.
- **Fatigue / hesitating / overshooting / correcting fields exist on
  debugData but the math never sets them** — they're stubs for humanisation
  that never got implemented.
- **tracking=true** (entity tracking) is declared in the builder but not
  implemented (need per-tick aim adjustment toward a moving target, used by
  CombatReactionAction).
- **Camera sync via MixinCamera** is 🔬 — the mixin must overwrite the camera
  yaw/pitch before rendering; need to verify that vanilla mouse input
  doesn't fight our rotations (currently we write directly to
  `player.yRot`/`player.xRot` but the mouse handler may overwrite).
- **No random head-movement during idle** (IdleBehavior is supposed to do
  that; see Break Scheduler).
- **No gaussian jitter** on the final 5° of a rotation to look more human.
- **Does not check `RotationState` for screen open** — if a GUI is open the
  player can't rotate in world, but we still emit rotation requests (they
  get ignored by MC but waste queue slots).

### Acceptance criteria
- [ ] Replace linear lerp with ease-in-out cubic.
- [ ] Implement tracking=true (recompute target yaw/pitch each tick toward a
      living entity for combat).
- [ ] Add gaussian micro-jitter (±0.5°–1.5°) on MACRO/COMBAT priority rotations.
- [ ] Verify MixinCamera defeats mouse overwrites in 26.1 (DevData: log
      `player.yRot` before/after mouse handler runs).
- [ ] Add fatigue/hesitation simulation (10-20% chance of 100-200 ms
      overshoot-correct cycle during long MACRO turns).
- [ ] Skip rotation requests while a Screen is open.

---

## 8. ZenithPath — Movement / Pathfinding

### Current state 🟡 (API solid; pathfinder incomplete)
- `PathRequest` builder:
  `.from(x,y,z three doubles).to(x,y,z three doubles).mode(PathMode)
   .allowEtherwarp(bool).allowSprint(bool).stopDistance(double)
   .maxComputeMs(long).tag(String)`
- Modes: `WALK, ETHERWARP, FLY, ROTATE_ONLY`.
- `ZenithPath.stop()` and `requestPath(PathRequest)`.
- `PathDebugData`: state/mode/currentSpeedBps/stuck/plannedNodes/executedNodes/totalCost/distanceToTarget/reaction/computeMs.

### Known gaps ❌
- **A* pathfinder exists in name only** — `plannedNodes/executedNodes/totalCost`
  fields are exposed but the actual node graph computation isn't finished.
  In practice, calling `requestPath(WALK, to=…)` will set up state but
  doesn't tick along a real block-level walk plan.
- **Etherwarp mode** does not raytrace to the target or send the Aspect of
  the End right-click — it accepts the flag but doesn't act on it.
- **FLY mode** is unimplemented (would require creative mode / fly mod
  permission; not used in SkyBlock anyway — ignore).
- **Jump-pad / NPC cross-server travel** is not implemented. If player is in
  Limbo, REPATH will try to walk which doesn't work across servers.
- **Stuck detection** exposes `stuck()` but doesn't trigger the
  Obstruction/REPATH failsafe automatically (ObstructionDetector only covers
  forward-held + horizontal velocity = 0 for >1.2 s, specific to farming).
- **`stopDistance`** does not cause the path to terminate early.

### Acceptance criteria
- [ ] Implement A* over a block walk graph (solid top, 1.8 m clearance,
      half-step up, 1-block drop, fence/wall handling).
- [ ] Walk execution: per-tick forward/sprint sneak input, look-ahead to next
      node.
- [ ] Etherwarp: raytrace 57 blocks (AOTE max) through non-solid blocks,
      right-click with AOTE in hand if clear.
- [ ] Cross-server travel state machine: when dest is in a different SkyBlock
      zone (Barn/Park/Deep Caverns/etc.), send `/warp <zone>` via TravelEngine
      then resume WALK once world changes.
- [ ] Wire stuck detection → failsafe OBSTRUCTION trigger after 2.5 s of
      progress < 0.1 blocks while having forward input.

---

## 9. InputEngine — Key/Mouse Simulation

### Current state ✅ (but needs humanisation)
- `InputEngine.getInstance().keys()` returns `KeySimulator`.
- KeySimulator API: `halt()`, `setForward/Back/Left/Right/Jump/Sneak/Sprint/Use/Attack(boolean)`, plus boolean getters.
- **No `leftClick()`/`rightClick()` method** — we use `setAttack(true/false)` and `setUse(true/false)`.
- `halt()` releases all keys.
- Writes to `KeyBinding.setKeyPressed` reflectively on the vanilla keybinds (`key_up`/`key_down`/etc.).
- Ticked from `ClientTickDispatcher`.

### Known gaps 🟡
- **No inventory-click simulation** — all GUI clicks go through
  `handleInventoryMouseClick` (GUIClickExecutor), which is correct. No
  window-click simulation needed on KeySimulator.
- **Attack key held boolean does not trigger vanilla item-swing** on the
  client; server sees the packet and other players see the swing, which is
  fine.
- **Sustained holds are jitter-free** (see Stealth section).
- **No keyboard-char typing** — we don't need to type in chat (we send
  packets directly), but it means we can't type into anvil/enchant tables.
  Unused in SkyBlock macros.
- **`setSprint(true)`** sets the sprint keybind but does NOT call
  `player.setSprinting(true)`; Hypixel will see the sprint key held which is
  correct.
- The sprint key doubles as the "sprint and sprint-add" key (Ctrl in vanilla
  defaults) — if the user has rebound sprint to something other than LCtrl,
  our reflection on the vanilla `key_sprint` keybind still works, but the
  player must be using our `PlayerKeybindReader` to load their actual binds.

### Acceptance criteria
- [ ] Add micro-pause humanisation (stochastic 20-100ms drops on held
      forward/attack every 4-12 s, not on fixed timers).
- [ ] Confirm PlayerKeybindReader correctly reads the configured sprint key
      from Options.

---

## 10. DelayManager — Humanised Timings

### Current state 🟡
- `isReady(tag)` returns true if the Gaussian-distributed delay since
  `resetHumanised(tag, minMs, maxMs)` has elapsed.
- `resetHumanised(tag, minMs, maxMs)` samples from a Gaussian centred at
  (min+max)/2 clamped to [min,max].
- Used by GUIClickExecutor to space clicks.

### Known gaps
- [ ] Not yet used between rotations (ZenithEyes uses durationMs directly —
      should pull the per-rotation gap from DelayManager for MACRO/COMBAT).
- [ ] Not used between key state changes in KeySimulator.
- [ ] Fixed delays are still present in places (e.g. TravelEngine waits 2.5s
      after /warp before clearing travelling flag — should be
      world-change-event driven).

---

## 11. GUI Automation Layer

### Files
- `GUIParser` — reads open container → `GUIState` (title, slot list, player inv start, detected page hints). Singleton `getInstance()`.
- `GUIState` — record holding title, stacks, playerInventoryStart, containerId, windowId.
- `GUIItemMatcher` — matches items by nameContains/byName/bySkyblockId/byItemId/byMinSize/byMaxSize/enchanted/glint/loreContains.
- `GUIItemStack` record: `displayName, List<String> lore, itemId, skyblockId, stackSize, enchanted, glint`.
- `GUISlotFinder.findFirst(matcher, state)` — returns first slot index matching, or -1.
- `GUIClickExecutor.leftClick/rightClick/shiftClick(slot)` — gates on BitsSpendBlocker + DelayManager. Sends `handleInventoryMouseClick` packet.
- `GUIWaiter` — `waitForTitle(substr, timeout)`, `checkAll()` ticks each waiter and fulfills promises.
- `GUIInteractionEngine` — singleton, subscribes to tick, drives `GUIParser.tick() / SignInputHandler.tick() / GUIWaiter.checkAll() / GUIClickExecutor.tick()` each client tick.

### Confidence ✅
This is a mature subsystem (NEU/DSM/Athena all use the same architecture).
The only risk is Mojang-mapping signature drift for `handleInventoryMouseClick(containerId, slotId, button, ClickType, player)` — verified on 26.1 per Handoff.

### Known gaps
- [ ] `GUIClickExecutor` doesn't support hotbar swap clicks (button 0-8 with
      SWAP ClickType) or throw/drop. Not needed for AH/Bazaar but will be
      needed for inventory management macros in later phases.
- [ ] `GUIParser` parses lore via `Component<->String` conversion — need to
      verify newlines are preserved and § codes are stripped correctly in
      26.1's Component system.
- [ ] Skyblock ID extraction from NBT ExtraAttributes works via reflective
      NBT traversal. 🔬 Need to verify the NBT path to ExtraAttributes.id on
      26.1 (ItemStack tag path may have shifted).

### Acceptance criteria
- [ ] DevDataMacro captures GUI dumps for AH/Bazaar/Sign/Manage pages and
      writes them to OBSERVATIONS.md so title strings and slot layouts can
      be validated.

---

## 12. Sign Input Handler

### Current state 🟡🔬
- Reflectively searches the current Screen class hierarchy for a
  `signText`/`frontText`/`backText` field (SignText record in 1.21+) and
  for an `onDone()` method.
- `requestType(text)` queues text to be typed into the next sign screen;
  when `isInSignScreen()` returns true, next tick reflectively sets line 0
  of the sign and calls `onDone()`.

### **High risk of breakage — 🔬**
- On 1.21.4+ (MC 26.1), signs use a two-sided SignText record with
  `getMessages(int isFrontText)` returning `Text[]`. We don't yet support
  this — we're looking for a settable `signText` List, which doesn't exist
  on newer mappings. This is the most likely first-run breakage.
- The "Done" button is bound to a specific method name; on some versions it's
  `method_31460` intermediary.
- We don't clear existing text (only overwrite line 0) — works for the
  single-line signs Hypixel uses (quantity/price) but may leave garbage on
      other signs.
- Sign titles for Bazaar quantity/price are currently matched broadly — need
  DevData to capture exact titles.

### Acceptance criteria
- [ ] Support the 1.21+ SignText record API (set messages via a fresh SignText
      instance if field is final/record).
- [ ] Verify method name for onDone (candidates: `onDone`, `checkDone`,
      `method_31460`) via reflection fallback chain.
- [ ] Clear all four lines before typing, not just line 0.
- [ ] Add DevData snapshots of every sign encountered (title + class name +
      declared methods) to pin down exact names.

---

## 13. SkyBlock Navigator / Travel Engine

### Current state 🟡
- `SkyblockNavigator`: sends `/is`, `/hub`, `/warp <name>` via CommandSender.
  Warps supported: `barn, park, deep, gold, spider, end, mines, da, crypt,
  museum, wizard`, plus `isle` for Crimson Isle (NOT nether). `escapeLimbo()`
  sends `/lobby` then `/play skyblock`; `toSkyblock()` sends `/play skyblock`.
- `TravelEngine.travelTo(destinationId)` dispatches /command; `isTravelling()`
  flag cleared after 2.5s on ground.

### Known issues
- 🔬 Warp name list is best-effort. `/warp da` for Dark Auction, `/warp crypt`
  for Spider's Den crypt area (actually `/warp` doesn't support "crypt" —
  need DevData to verify which warps exist in 2026 SkyBlock).
- `escapeLimbo()` `/lobby` then `/play skyblock` works, but from Limbo `/is`
  is a no-op (correct per Handoff). However, once in lobby you have to wait
  for the lobby world to load before sending `/play skyblock`; we have no
  load detection — we fire them 50 ms apart, which Hypixel will throttle.
- `TravelEngine.isTravelling()` uses a fixed 2.5s timer instead of
  WorldChangeEvent. Should listen to world change instead.
- **No NPC / jump-pad travel.** Many Hypixel zones require right-clicking an
  NPC or stepping on a launch pad (e.g. Blazing Fortress from the Spider
  Den portal). We send `/warp isle` for Crimson — that works if the player
  has it unlocked; otherwise nothing.
- **Queue / server-slowdown detection** when warping is not implemented
  (should listen for "That warp is on cooldown" / "You cannot warp while..." chat).

### Acceptance criteria
- [ ] DevData tour runs every `/warp` and captures the response chat message
      (success / unknown warp / cooldown) so the warp list is accurate.
- [ ] Replace 2.5s timer with WorldChangeEvent-driven completion.
- [ ] Add rate limit tracking per command (cooldown per warp from chat).
- [ ] NPC interaction state machine for future zones (walk to NPC → right
      click → wait for chat GUI → click response option).

---

## 14. Auction House Interactor

### Files
- `AuctionHouseNavigator` — sends `/ah`, `isInAH()` by title prefix.
- `AuctionHouseGUI` — `Page` enum + slot finders.
- `AuctionHouseExecutor` — buy + list state machines.
- `AuctionHouseInteractor` — pumps OrderManager into executor.

### Current state 🟡🔬

#### Buy flow (BROWSER → TOGGLE_BIN → SEARCH → WAIT_SIGN → WAIT_RESULTS → WAIT_CONFIRM → WAIT_BOUGHT)
1. Open `/ah`.
2. Click gold block (Refresh BINs) — actually we don't click it; we go straight
   to the Search sign.
3. Click Search oak sign → type item name.
4. Wait for RESULTS page, click first head.
5. Wait for CONFIRM page, click "Buy Item" / "Confirm".
6. Mark bought.

**Problems:**
- 🔬 We do **not click the Sort diamond** to sort by Price: Low → High. The
  default sort is "Recently Added" which means we'll often overpay. **This is
  a critical profitability bug.**
- We do not page through results via the arrow heads (page 2, 3…). If the
  cheapest listing is on page 2 we miss it.
- 🔬 The BIN toggle is described in comments as a gold ingot (correct
  per-Handoff) but we click a slot found by `GUIItemMatcher.nameContains("BIN")`
  — the item is actually called "Switch to BIN view" / "Show BINs only";
  need DevData to confirm exact item name.
- 🔬 Search sign title is "Enter search query:" or "Search:" — need DevData.
- We do not handle the "You cannot purchase this auction while in a
  different world" transfer case.
- We sanity-check the price against `candidate.buyPrice * 1.05` but the
  check fires *after* clicking the head; by then we're already in the
  confirm screen, so if the first head is a wrong item (different enchants
  but similar name), we still confirm. We should re-read the lore price on
  the confirm screen and bail if it's > 105% expected.

#### List flow (MANAGE → CLICK_CREATE_HEAD → CHOOSE_ITEM → CREATE → SET_PRICE → CLICK_LIST → WAIT_LISTED)
- 🔬 Finds "Create BIN Auction" gold ingot on the Manage page and clicks it.
- Clicks the held item in the player inventory to choose it (our
  implementation does not currently scan player inventory for the target
  item — `CHOOSE_ITEM` state is stubbed).
- 🔬 Price sign matching searches for an item whose lore contains "Buy it now"
  or "Price". Need DevData to see the exact prompt.
- After sign, clicks a gold ingot to list.
- Does not handle "You can only have 10/15/25 auctions at once" chat message
  (rank-dependent cap).

### Acceptance criteria
- [ ] Click Sort → Price: Low → High BEFORE entering the search sign.
- [ ] Parse confirm-screen price from lore; abort if > 105% expected.
- [ ] Implement CHOOSE_ITEM: scan player inventory slots (slots after
      playerInventoryStart) for the held item with correct SKYBLOCK_ID, click it.
- [ ] Pagination: if results found but 0 slots match max-price, click right
      arrow head and continue searching up to N pages.
- [ ] Detect full-auction-cap chat message and pause listing until orders fill.
- [ ] DevData tour visits /ah Manage, Create BIN, search, confirm pages and
      dumps exact item names/lores/sign titles.

---

## 15. Bazaar Interactor

### Files
- `BazaarNavigator` — sends `/bz`, isInBazaar().
- `BazaarGUI` — Page enum, findBuyInstantly/findSellInstantly/findCreateBuyOrder/findCreateSellOrder/findManageOrders/findBack/findConfirm/findProduct/findCategory + click helpers.
- `BazaarCategory` — product id → {Farming, Mining, Combat, "Woods & Fishes", Oddities} hard-coded map.
- `BazaarExecutor` — full 4-mode state machine (instant buy, instant sell, create buy order, create sell order).

### Current state 🟡🔬 (just rewritten)

#### State machine (all four modes):
1. OPEN_BZ* — send `/bz`, wait for catalog.
2. FIND_CATEGORY — click the correct category head for the product.
3. WAIT_CATEGORY — wait for category page.
4. FIND_PRODUCT — find product by skyblockId in the category.
5. WAIT_PRODUCT — wait for product page.
6. PRODUCT_READY — click Buy Instantly / Sell Instantly / Create Buy Order / Create Sell Order.
7. WAIT_QUANTITY* — sign for count.
8. WAIT_PRICE_CREATE_* — (limit orders only) sign for price per unit.
9. WAIT_CONFIRM* — click stained-glass Confirm button.
10. WAIT_DONE / WAIT_ORDER_PLACED — return to product/catalog and mark done.

### Known issues / guesses 🔬
- **Category map is hard-coded** (BazaarCategory.java). New/renamed products
  will mis-categorise. The DevData tour needs to visit every category and
  record every product name+id to build a real map.
- Sign titles:
  - Quantity assumed `How much do you want …?` or `How many do you want …?`
    — exact wording unknown.
  - Price assumed `At what price …?` — exact wording unknown.
- Confirm-page titles for limit orders guessed as "Buy Order … Confirm" and
  "Sell Order … Confirm" — actual titles unknown.
- The "Buy Instantly" / "Sell Instantly" / "Create Buy Order" / "Create Sell
  Order" buttons are green/red/gold paper items. Exact display names unverified.
- Quantity/price sign parsing accepts plain integers or K/M/B suffixes
  (formatPrice outputs 12.3k / 1.23M / 1.23B). Hypixel accepts these on
  signs — tested in prior knowledge but 🔬 confirm.
- After instant buy we call `markBoughtBazaar()` → routes to bazaarSellQueue.
  FlipEngine.pumpBazaar then calls `instantSell` (not createSellOrder) —
  immediate resell, which is the correct BEHAVIOUR for instant flips, but
  we should prefer a limit sell to capture better price. This is a simple
  strategy flag — NOT a bug, but needs thinking.
- The top-bar utility items (Go Back / Manage Orders / Sell Inventory Now /
  Sell Order… / Buy Order… / Buy/Sell * Order) are skipped by isTopBarItem so
  we don't confuse them with categories/products. The set of skipped names
  is a guess.
- "Sell Inventory Now" does a bulk instant-sell of all items in inventory
  — we don't use it (we sell one product at a time) but we need to avoid
  clicking it by accident.

### Acceptance criteria
- [ ] DevData tour opens `/bz`, clicks every category, clicks every product,
      snapshots sign prompts for Buy Instantly / Sell Instantly / Create Buy
      / Create Sell. This pins down every single title/button name.
- [ ] Replace BazaarCategory hard-coded map with a file loaded from
      `config/zenithclient/bazaar_categories.json` generated by DevData or
      updated from NEU.
- [ ] Consider strategy flag (`bazaar.sellMode = INSTANT | LIMIT`) so the
      user can choose between instant turnover vs better profit.
- [ ] Handle "You don't have enough coins" / "You don't have enough items to
      sell" chat failures and mark order failed.
- [ ] Manage Orders fill/collect flow: after buy order fills, navigate to
      Manage Orders → click the filled order → claim items. Currently
      unimplemented (FillMonitor exists but has no GUI executor).

---

## 16. Failsafe System

### Files
- `FailsafeManager` — orchestrator, init/tick, `trigger(type, reason, severity)`/`clear(type)`/`failAll()`/`areMacrosPaused()`.
- `FailsafeStrictness` — 11-level severity ladder: NONE(0)→NOTIFY(1)→PAUSE(2)→WIGGLE_REACT(3)→COMBAT(4)→REMOVE_OBSTRUCTION(5)→INSTANT_RESPAWN(6)→REPATH(7)→WARP_ISLAND(8)→WARP_HUB(9)→DISCONNECT(10).
- `FailsafeType` — 21 types with default severities.
- `BanActionHandler` — `sendIsland()=/is`, `sendHub()=/hub`, `respawn()`, `disconnect(reason)`, `sendWarp(name)`.
- `SafetyStatusMonitor` — dot colour for HUD (green/amber/orange/red).
- `ReactionEngine` — dispatches reaction actions by severity.
- `TabInHandler` — pauses macros on Minecraft window focus loss (NO auto-resume).

### Per-type defaults (user-specified)
| Type | Default Severity | Notes |
|---|---|---|
| PLAYER_NEARBY | WIGGLE_REACT | Escalates on repeat |
| BAN_DETECTED | DISCONNECT | Chat pattern "Ban" / "banned" + related |
| LIMBO | REPATH | 🔬 detection text unverified |
| TELEPORT | REPATH | Position delta threshold |
| VELOCITY_KICK | WARP_ISLAND | /is |
| STAFF_PULL | WARP_ISLAND | Staff gamemode / tab rank detected |
| ITEM_DESELECT | WIGGLE_REACT | Wrong-item held mid-macro |
| ROTATION_RESET | WIGGLE_REACT | External yaw change > 90° |
| WORLD_CHANGE | REPATH | Dimension/level change |
| GUI_CLOSE | NOTIFY | 🔬 (user spec — is this right?) |
| WRONG_TOOL | WIGGLE_REACT | Tool id doesn't match expected |
| YAW_FLIP | WIGGLE_REACT | 180° snap |
| INVENTORY_FULL | NOTIFY | No empty hotbar slots |
| LAGBACK | REPATH | Position rollback |
| DISMOUNT | WIGGLE_REACT | Horse/strider dismount |
| DEATH | INSTANT_RESPAWN | `player.respawn()` / click DeathScreen |
| LOW_HEALTH | COMBAT | 🔬 HP from action-bar not yet parsed |
| LOW_HUNGER | COMBAT | 🔬 hunger doesn't exist in many zones |
| OBSTRUCTION | REMOVE_OBSTRUCTION | forward held + v=0 for >1.2 s |
| CUSTOM | NOTIFY | For user scripts |

### Key user-specified behaviours (all implemented)
- ✅ Escape is `/is` NEVER `/home`.
- ✅ `/hub` fallback.
- ✅ Low health = COMBAT (fight back) not warp.
- ✅ Camera snap / player nearby / wrong item → WIGGLE_REACT (macros keep running).
- ✅ Blocks in farm path → REMOVE_OBSTRUCTION (wiggle + look + "?" in chat + break block, then repath).
- ✅ Death → INSTANT_RESPAWN (`player.respawn()`).
- ✅ Teleport / world change / limbo / lagback / player clone → REPATH via
  ZenithPath back to active macro's DestinationProvider.
- ✅ Reactive severities DO NOT PAUSE macros (only PAUSE / WARP_ISLAND /
  WARP_HUB / DISCONNECT freeze keys).

### Known issues 🔬
- **Hypixel custom HP system** (Defense/❤/Absorption shown on action bar) is
  NOT parsed by `PlayerHealthMonitor`. It currently reads `player.getHealth()`
  which is always 20/20 in SkyBlock. **LOW_HEALTH will never fire until
  this is fixed.** This is the #1 failsafe correctness bug.
- **Hypixel death screen** — the DeathScreen class may have a different title
  or may be a custom Hypixel GUI. RespawnAction clicks the "Respawn" button
  if present, else calls `mc.player.respawn()`.
- **"?" in chat** on REMOVE_OBSTRUCTION is sent via `ServerboundChatPacket`
  with a 30s rate limit. Risky — staff see these. Consider making it opt-in.
- **Ban action patterns** in chat are a small hard-coded list. Hypixel ban
  messages have varied over the years. Need DevData snapshot of a real ban
  screen (don't test on main account).
- **Limbo detection** — checks for "You were spawned in Limbo." in chat AND
  scoreboard title "LIMBO" or empty world. Need DevData.
- **REPATH after respawn** calls `ZenithPath.requestPath(WALK …)` — but the
  player may be in a different server (e.g. respawn on private island
  after dying in Deep Caverns). Need TravelEngine to handle cross-server
  repath, not just walking.
- **Staff rank detection** looks for `[ADMIN]`, `[MOD]`, `[HELPER]`, `[OWNER]`
  in tab list names via TabListScraper. Game mode check for spectator is
  NOT implemented (see Stealth gaps).
- **PanicButton** (held 400 ms triggers DISCONNECT). Wiring exists but
  debounce timing not tested with key-repeat.

### Acceptance criteria
- [ ] Parse action-bar HP (regex on the action bar text for ❤/HP numbers,
  absorption, defense) and feed to PlayerHealthMonitor.
- [ ] Parse real death title via DevData observation (intentionally die in a
      safe way on an alt to capture it).
- [ ] On REPATH, if in different world than expected, route through
      TravelEngine.travelTo(zone) instead of direct walk.
- [ ] Add spectator gamemode check to PlayerNearbyDetector.
- [ ] Add severity escalation for PLAYER_NEARBY (3rd WIGGLE_REACT within
      60 s → WARP_ISLAND).
- [ ] Unit-test reaction dispatch per severity.

---

## 17. Reaction Actions

Located in `failsafe/reaction/actions/`.

| Action | Status | Notes |
|---|---|---|
| WiggleReactionAction | ✅ | ±20° yaw / ±7° pitch, FAILSAFE priority, preemptible, 700–1200 ms |
| CombatReactionAction | 🟡 | Aims nearest non-villager/non-armorstand living ≤6b, setAttack up to 4s. **tracking=true not implemented in ZenithEyes, so aim is a one-off snap instead of tracking.** |
| RemoveObstructionAction | 🟡 | Wiggle, looks at block via eye-ray, sends "?" via ChatQuestionMarkAction, holds setAttack up to 2.5s then triggers RepathReactionAction. Eye-ray look-at uses vanilla raytrace — should work. |
| RespawnAction | 🟡 | Clicks DeathScreen "Respawn" if present, else `player.respawn()`. |
| RepathReactionAction | ❌🟡 | Calls `ZenithPath.requestPath(WALK, allowSprint, allowEtherwarp, stopDistance=1.5, maxComputeMs=150)` to active macro's DestinationProvider. **ZenithPath WALK is not implemented yet, so this sets up state but doesn't actually walk.** |
| ChatQuestionMarkAction | ✅ | Rate-limited 30s, sends "?" via ServerboundChatPacket. |
| MistakeSimulationAction | ❌ | Intended to add human-looking micro-mistakes post-reaction — stub. |
| PanicLookAction | ✅ | Quick snap to a random direction then slow back. |
| SlowLookAroundAction | ✅ | Slow wander used by IdleBehavior. |

### Acceptance criteria
- [ ] ZenithEyes tracking=true so CombatReactionAction follows a moving target.
- [ ] Wire RepathReactionAction into real ZenithPath walking once A* lands.
- [ ] Add cross-server fallback: if dest is in a different zone, call
      TravelEngine.travelTo instead of ZenithPath.

---

## 18. Failsafe Detectors

Located in `failsafe/detection/`. 21 detectors registered. Quick status:

| Detector | Status | Notes |
|---|---|---|
| PlayerNearbyDetector | 🟡 | Distance-only check; no NPC/spectator filter |
| BanDetector | 🟡 | Hard-coded chat patterns |
| LimboDetector | 🟡 | Chat + world proxy |
| TeleportDetector | 🟡 | Position delta |
| VelocityKickDetector | ❌ | Not implemented (server velocity packets) |
| StaffDetector | 🟡 | Tab-list rank regex |
| ItemDeselectDetector | 🟡 | Detects held-item change mid-macro |
| RotationResetDetector | ✅ | External yaw delta |
| WorldChangeDetector | ✅ | WorldChangeEvent |
| GuiCloseDetector | ✅ | InventoryCloseEvent |
| WrongToolDetector | 🟡 | Expected-tool registry is empty (macros don't register their tool yet) |
| YawFlipDetector | ✅ | 180° snap |
| InventoryFullDetector | ✅ | Scans main inventory |
| LagbackDetector | 🟡 | Position rollback check |
| DismountDetector | 🟡 | Vehicle change |
| DeathDetector | 🟡🔬 | DeathScreen presence + `player.getHealth() <= 0` (unreliable in SB) |
| LowHealthDetector | ❌🔬 | Reads vanilla HP — always 20/20 in SB |
| LowHungerDetector | ❌ | Reads vanilla hunger (unused in SB) |
| ObstructionDetector | 🟡 | forward held + v* 1.2s |
| CustomDetector | ✅ | API for user scripts |
| TabInDetector | ✅ | Focus loss |

---

## 19. Flipper — Top-Level (FlipEngine)

### Current state ✅
- Driven by `ClientTickDispatcher` each tick (when running).
- Subsystems: MarketScanner, BazaarFlipEngine, CraftFlipEngine, NPCFlipEngine,
  AHCraftFlipEngine, OrderManager, AuctionHouseInteractor, BudgetManager,
  ProfitTracker, AHSalesTracker, FillMonitor, BazaarExecutor.
- Start/Stop wired to dashboard Flipper toggle and `.z flip start/stop`.
- Pauses on scheduled breaks (BreakScheduler) and on failsafe.
- Consumes up to 2 candidates per tick from the AH scanner queue plus 1
  from BazaarFlipEngine and 1 from NPCFlipEngine; budgets enforced.

### Known issues
- [ ] `pumpBazaar()` only dispatches instant buys/sells; doesn't dispatch
      `createBuyOrder` / `createSellOrder`.
- [ ] AHCraftFlipEngine is pumped (ahCraft.tick()) but enqueues via generic
      queue; no craft GUI interactor exists yet (Phase 12).
- [ ] Flip type routing: AH_BUY → buyQueue → AH; BAZAAR_SPREAD → bazaarBuyQueue
      → Bazaar instant buy → bazaarSellQueue → Bazaar instant sell (correct).

---

## 20. Market Scanner

### Current state ✅🟡
- Polls every 4 s on the IO pool via `ThreadUtils.scheduler().scheduleAtFixedRate`.
- Waits for BINCache to have ≥100 entries (from Moulberry fetch).
- Each tick picks 8 items from a hotlist (or from ItemDatabaseCache if hotlist
  is sparse), randomises, calls Coflnet for active listings (20 per id).
- Finds cheapest listing + median, runs `SpreadCalculator.evaluateBin(...)`
  against profit thresholds.
- Enqueues viable FlipCandidates priority-sorted by expectedProfit.
- Tracks recent 30 finds for dashboard display.

### Known issues
- [ ] Waits for BINCache which is seeded from Moulberry lowestbin.json on a
      10 s post-start delay. If Moulberry is down, scanner never starts.
- [ ] Sample size of 8 per 4 s = 120 ids/min — with ~1500 commonly-traded
      items it takes ~12 min to scan the market once. Acceptable for v1.
- [ ] SpreadCalculator thresholds (minProfitCoins/minProfitPercent) loaded
      from FlipperConfig but not yet tunable from dashboard beyond defaults.

---

## 21. API Clients & Caches

### Created this session:
- ✅ `BINCache` (lowest BIN prices, ConcurrentHashMap, `get/put/putAll/size/keySet/ageMs/clear`).
- ✅ `BazaarCache` (top-of-book buy/sell, with `instantBuyPrice= sellPrice`, `instantSellPrice= buyPrice` aliases matching convention; buyVolume/sellVolume weekly).
- ✅ `ItemDatabaseCache` (NEU items.json: getName/getNpcSell/getItem/items/raw/size).
- ✅ `RecipeCache` (NEU recipes keyed by outputId; recipesFor returns list of Recipe records).

### API clients
- ✅ `CoflnetAuctionAPI` — active auctions for an item.
- ✅ `CoflnetBazaarAPI` — bazaar snapshot → populates BazaarCache.
- ✅ `CoflnetFlipAPI` — Coflnet premium flip feed (not used; API key needed).
- ✅ `CoflnetMayorAPI` — current mayor + perks (fetched but not yet consumed).
- ✅ `Moulberry LowestBINFetcher` — pulls moulberry.codes/lowestbin.json.
- ✅ `NEURepoClient` + `NEUItemFetcher` + `NEURecipeFetcher` + `NEUConstantsFetcher` — NEU GitHub repo fetches.
- ✅ `RateLimiter` — per-host rate limits.
- ✅ `HttpClient` — simple Java 11+ HTTP wrapper.
- 🟡 Scrapers (ActionBarScraper, ChatScraper, ScoreboardScraper,
  TabListScraper, BossBarScraper, AuctionGUIScraper, BazaarGUIScraper,
  CollectionGUIScraper, InventoryGUIScraper) — exist as skeletons; most are
  stubs that don't fire events. Need to wire into DevDataHarvester (already
  does its own scraping) OR consolidate.
- ✅ UpdateChecker — checks GitHub releases.
- ❌ Wiki* data classes (WikiCollections, WikiLocations, WikiDungeons,
  WikiMinions, WikiMobs, WikiNPCLocations, WikiPets, WikiRecipes, WikiShopPrices,
  WikiSkillTables, WikiSlayers, WikiEvents, WikiEnchantments, WikiFarmDesigns)
  — skeletons, no real data load. These are research-holders per rule §10.

### Known gaps
- [ ] Caches do not persist to disk — every restart re-downloads.
      Acceptable; NEU/Moulberry are fast to fetch.
- [ ] No failure backoff on HTTP errors (fixed number of retries in HttpClient?).
- [ ] API keys for Coflnet premium not supported (no key config field).

---

## 22. Tax Calculator

### Current state 🟡
- `TaxCalculator.bazaarInstantBuyCost(price, count)` and `bazaarInstantSellReceived(price, count)` with hard-coded 5%/1.25%.

### **Known wrong — need to verify with real Hypixel numbers**
Hypixel Bazaar fees (as of last SkyBlock update I'm confident about):
- Instant buy: 1.25% fee on the coins spent (on top of purchase price).
- Instant sell: 1.25% fee deducted from coins received.
- Create buy order: 1% tax on coins escrowed (refunded if cancelled).
- Create sell order: 1% tax on sale (deducted from proceeds on fill).
- AH listing fee: 1% capped at a level that changes (verify), plus AH sale
  cut of ~2% or ~4% depending on item type.
- The handoff doc says "AH 1/2/8%, Bazaar 5%/1.25%" which I believe is wrong.
  The 8% appears when selling certain things or certain tiers but the common
  AH rates are 1% listing (capped at 25k? 100k?) + 2% sale cut.

**🔬 Verify actual rates from https://api.hypixel.net or by posting a
test auction on an alt.** Do not trust my numbers here.

---

## 23. Item Filter / Selector

### Current state 🟡
- `ItemFilter` — matches item ids against a presets list (whitelist/blacklist by tier/pattern).
- `ItemFilterManager` — presets "high-volume" (default), "all", "custom".
- `FilterPreset` — named preset object.

### Known gaps
- [ ] Presets are hard-coded. Need config file + dashboard editor.
- [ ] `shouldCheckNow(id)` / `markChecked(id)` throttling per item exists but
      cooldown is hard-coded.

---

## 24. Budget Manager

### Current state ✅
- Reads coins available from purse+bank (parsed from scoreboard via ScoreboardScraper — but scraper is a stub; current config has `coinsAvailable` field settable manually).
- `canBuy(price)` checks against maxBudget loss ceiling.
- `recordBuy/recordSell` updates session P&L.
- `recalc()` recomputes available coins from scrapers (stub).

### Acceptance criteria
- [ ] Wire ScoreboardScraper to parse "Purse: <n>" / "Piggy: <n>" and fill
      coinsAvailable.

---

## 25. Order Manager & Lifecycle

### Current state ✅ (updated this session)
- States: `PROPOSED, QUEUED_TO_BUY, NAVIGATING, BUYING, HOLDING, LISTED, COMPLETED, FAILED`.
- Four queues: buyQueue (AH buy), ahListQueue (AH list), bazaarBuyQueue, bazaarSellQueue.
- API: enqueue(FlipCandidate), pollToBuy/pollToList/pollToBazaarBuy/pollToBazaarSell,
  markBought(AH), markBoughtBazaar, markListed, markCompleted, fail, failAll, tick.
- Timeout: orders stuck in BUYING/NAVIGATING for >45s fail automatically.

### Known issues
- [ ] Active orders in LISTED state are never cancelled/relisted automatically
      (RelistStrategy exists but isn't pumped in OrderManager.tick).
- [ ] Order timeout of 45s is too short for AH paging/large buys.

---

## 26. Flip Engines

### BazaarFlipEngine ✅
- Scans 60 s after start, every 50 s.
- Iterates HOT_PRODUCTS (hard-coded list of ~80 items), compares top-of-book
  buy/sell from BazaarCache.
- After-fees profit must exceed minProfitCoins AND minProfitPercent AND
  weekly volume ≥ 50,000.
- Enqueues BAZAAR_SPREAD FlipCandidates.

### AH bin flip engine — part of MarketScanner ✅ (above)

### CraftFlipEngine 🟡
- Uses RecipeDatabase (computes cheapest ingredient cost per known output).
- Iterates 11 known outputs (ENCHANTED_DIAMOND etc.); compares cost to BIN sell price.
- Very limited set; will expand when NEU recipes fully load.

### NPCFlipEngine 🟡
- Compares NPC buy prices (hard-coded seed list of NPC prices) against BIN/Bazaar sell.
- Limited set.

### AHCraftFlipEngine 🟡
- Placeholder — buys inputs from AH, crafts, lists crafted item on AH.
- Cannot run without CraftingInteractor (Phase 12).

---

## 27. Fill Monitor / Relist / Undercut

### State ❌
- `FillMonitor` — 30-line stub, doesn't poll Manage Orders GUI.
- `RelistStrategy` — logic exists (cancel and relist if undercut by > N coins).
- `UndercutDetector` — reads low BIN vs our listPrice and recommends relist.
- None of these are pumped from a tick loop; they need a Manage-Orders-GUI
  walk state machine in AuctionHouseExecutor/BazaarExecutor to actually detect
  fills/cancels.

### Acceptance criteria
- [ ] Add Manage Orders walk to AuctionHouseExecutor: open AH → Manage →
      iterate orders → detect filled/cancel/underfill → claim coins/items or
      cancel/relist.
- [ ] Same for Bazaar Manage Orders (claim fills, cancel stale orders).

---

## 28. Profit Tracker & History

### Current state ✅
- `ProfitTracker.record(FlipRecord)` appends to in-memory list.
- sessionProfit/sessionFlips counters.
- FlipRecord holds itemId, itemName, type, buyPrice, sellPrice, profit,
  heldMs, success, note.
- No persistence to JSON yet.
- AHSalesTracker.successRate() stub.

### Known gaps
- [ ] Persist flip history to `history.json` (one of the 13 configs).
- [ ] SessionPanel HUD reads from these but doesn't render the data yet.

---

## 29. Break Scheduler & Idle Behaviour

### Current state 🟡
- `BreakScheduler` — Gaussian schedule ~45 min work / 5 min break (default; configurable).
- Emits BreakStartEvent/BreakEndEvent.
- `IdleBehavior` — on break, does slow camera wander (SlowLookAroundAction at WANDER priority).

### Known gaps
- [ ] Does not walk to a safe spot before break.
- [ ] No "walk to edge of farm / sit in safe corner" logic.
- [ ] IdleBehavior rotation wander is basic (constant yaw drift); doesn't look
      at other players, doesn't move the mouse off-screen, doesn't open
      inventory/chat to look AFK.
- [ ] Break schedule is global; should be per-activity with configurable
      work/break durations.

---

## 30. Mayor System

### State ❌
- CoflnetMayorAPI is fetched but the data is never used.
- No mayor-aware flip tuning (Derpy = AH closed / extra XP; Jerry = reduced
  tax/perks; Diana = mob-event-related price spikes).

---

## 31. Macro Framework (MacroModule / MacroManager)

### Current state ✅
- `MacroModule` abstract class implementing `RepathReactionAction.DestinationProvider`.
- Lifecycle states: IDLE/STARTING/RUNNING/PAUSED/ON_BREAK/STOPPING/ERROR.
- Hooks: onStart/onTick/onStop/onBreakStart/onBreakEnd/onFailsafePause/onFailsafeResume.
- Auto-pause on FailsafeManager.areMacrosPaused(), auto ON_BREAK on
  BreakScheduler.isOnBreak().
- Auto-register as DestinationProvider for RepathReactionAction.
- `MacroManager.register(module)/all()/get(id)/active()/start(id)/stopAll/pauseAll/resumeActive/tick()` — all wired.
- HudManager setActiveMacro(icon, displayName, skill) called on start so HUD
  icon is dynamic (not hard-coded watermelon) ✅.

### Known gaps
- [ ] Per-macro configuration (expected tool, row length, yaw, anchor
      position) is hard-coded in each macro. Need a config object per macro.
- [ ] Macro toggle via keybind (MACRO_TOGGLE keybind exists but doesn't start/stop yet).
- [ ] "Is any macro running" flag not used consistently by failsafe (e.g.
      COMBAT reaction shouldn't fire if no macro is running).

---

## 32. Macros — Farming Base (AbstractFarmingMacro)

### Current state 🟡 (naive "walk forward")
- On start: yaw to `targetYaw()`, pitch -50°, smooth 220 ms rotation.
- On tick: hold W (no sprint), raytrace 1.5 blocks in front at 50° down,
  left-click (setAttack) if the block matches `cropMatcher(state)`.
- Smooth yaw/pitch correction each tick at MACRO_TICK priority, preemptible.
- On stop: halt keys, disable ZenithEyes.
- On failsafe pause: releases forward + attack.

### Known gaps ❌ (critical — "macro farms one row then walks into the next row's crops")
- **No end-of-row detection.** forwardTicks increments forever but never
  turns around. The player walks straight into the wall at the end of the
  row and triggers ObstructionDetector → wiggles + breaks the block in
  front (usually a fence/wall) then repaths.
- **No row-length config.** User should be able to set "my rows are X blocks long".
- **No U-turn logic.** At end of row: strafe 1–2 blocks over, rotate 180°,
  continue.
- **No replanting.** Crops that require replanting (wheat/carrot/potato)
  need seeds in hand and right-click on farmland after breaking.
- **No tool equip/validation** (WrongToolDetector will fire if we pick up
  a pumpkin block and it swaps to hand).
- **No speed cap.** Holding W with no sprint works for slow farms but most
  Garden farms use Rabbit/DH/Farm Armor speed; we need to either hold
  forward consistently regardless of speed, or modulate based on crop
  break time (pumpkin/melon break in 1 tick with efficient axe).
- **Pitch is fixed -50° regardless of crop position.** This works for
  1.5-block look-ahead but doesn't adjust for the block actually being
  under the player vs 1 block forward.
- **No Y-axis containment** (if player falls off the farm, no response).
- **forwardPressed + no sprint means about 4.3 m/s** (walking speed). With
  sprint 5.6 m/s; with speed pot/armor up to ~9 m/s. Fixed tick loop
  doesn't adapt.
- **Doesn't respect right-click-to-harvest crops** (cocoa beans are on
  jungle logs — need different facing; sugarcane/cactus break any of the
  three column blocks).

### Acceptance criteria
- [ ] Add `rowLength()` abstract method (subclasses override).
- [ ] Forward walk until forwardTicks ≥ rowLength → execute U-turn: stop
      forward, shift laterally 1-2 rows (strafe Left/Right for ~300-500 ms
      depending on row spacing), rotate 180° yaw, toggle returning flag,
      resume forward.
- [ ] Track returning flag so on second pass we can re-detect row end.
- [ ] Add `expectedToolId()` so WrongToolDetector knows what should be held;
      equip tool from hotbar if found.
- [ ] Add replanting support (seeds in hotbar, right-click farmland after
      break for replantable crops).
- [ ] After U-turn, rotate head down again and re-wiggle for a few hundred
      ms for human look.

---

## 33. Melon / Pumpkin Macros

### Current state 🟡
- Both extend AbstractFarmingMacro.
- MelonMacro: targetYaw read from config (default 0f = south), matches
  Blocks.MELON / MELON_STEM? Actually matches `state.is(Blocks.MELON)`.
- PumpkinMacro: same for Blocks.PUMPKIN.
- Hard-coded `destX/Y/Z = 0,0,0` (user's anchor is always origin — needs
      to record starting position as anchor).

### Known gaps
- [ ] destX/Y/Z should be set to player position at onStart() so REPATH
      returns to the start.
- [ ] targetYaw should be read from config/set at start (or auto-detected
      from player's facing direction when starting).
- [ ] Stem detection: mature stems have a different state; we don't break
      them (correct) but we should avoid walking over them.
- [ ] Melons/pumpkins can be offset to the left or right of the stem. Our
      1.5b look-ahead only checks one position; should check ±X of target
      position.

---

## 34. Other Crop Macros

### State ❌
Files exist as 0-byte placeholders:
- CactusMacro, CarrotMacro, CocoaBeanMacro, EchoMacro, FlowerMacro,
  GardenLevelGrinder, MushroomMacro, PotatoMacro, SugarcaneMacro, WartMacro,
  WheatMacro.

Each needs to:
- Extend AbstractFarmingMacro
- Provide cropMatcher (correct blocks; note cocoa is on jungle logs side-faces;
  sugarcane/cactus are columns that break by breaking any block in the column;
  mushrooms spread; wart is nether-only crop)
- Provide expectedToolId (hoe vs axe; cocoa uses axe; cane uses hoe/hand)
- Flag replanting=true for carrot/potato/wheat/wart (need seeds in hand)
- Flag column-break=true for cane/cactus (break bottom block, whole column breaks)
- Set targetYaw / rowLength from user config

---

## 35. Garden Subsystems

### PestDetector ❌
- Chat pattern "A Pest has appeared!" or similar — need DevData to capture exact text.
- Should trigger WIGGLE_REACT + optionally swap to weapon + kill pest → swap back.
- Pest spawns rarely; not a blocker for v1 farming.

### JacobContestTracker ❌
- Jacob's Contest starts every X minutes; chat announcements + scoreboard timer.
- Need to detect contest start, track points/position, optionally switch
  crops/strategies.
- 🔬 Capture exact chat patterns and scoreboard layout via DevData during a
  real contest.

### VisitorHelper ❌
- Visitors appear on Garden with offers; need to click offer, trade items.

### Composter/Greenhouse ❌
- Subsystems for automatic composting/greenhouse stocking.

---

## 36. Mining macros

### State ❌
Subdirectories exist (mining/* 15 files) but are mostly stubs. Mining requires:
- Route-based waypoint walking (set of waypoints in a mining zone, e.g.
  Dwarven Mines / Glacite Mineshafts).
- Mithril/gemstone block detection (block hardness check on
  BlockState.getDestroySpeed).
- Mining speed/tool awareness (pickaxe with Mining Speed buffs).
- Chest looter (open loot chests after mining).
- Powder/CH per hour tracking → MiningStatsPanel HUD.
- Route editor (eventually in dashboard).
- Requires ZenithPath WALK mode to actually work (currently missing).

---

## 37. Combat macros

### State ❌
- CombatProfitPanel exists (skeleton).
- Subdirectories: combat/ (5 folders) exist as stubs.
- Needs ZenithEyes tracking=true (follow entity), KeySimulator.setAttack
  against a target, weapon swap, loot pickup, Magic Find tracking.
- Ghost combat in Mist / Enderman Slayer / Dragon fights are all different;
  start with simple aim-at-nearest-within-range for wolfs/spiders in Spider's Den.

---

## 38. Other macros

### State ❌
Empty/near-empty module dirs:
- fishing/, foraging/, hunting/, dungeon/, event/, kuudra/, rift/, misc/

These are post-Phase-15. Each needs its own design document. Do not attempt
until farming + mining + combat v1 are complete.

---

## 39. DevData Harvester & Tour Macro

### State ✅ (added previous session)
- `DevDataHarvester` — passive subscriber listening to InventoryOpen/Close,
  ChatReceived, WorldChange, ClientTick (every 2.5s passive snapshot). Writes
  markdown to `<gameDir>/zenith/devdata/OBSERVATIONS.md` every 30s + on GUI/world
  transitions. Captures GUI slot tables, sign prompts, scoreboard lines,
  action bar text, boss bars, tab header/footer, normalised chat, nearby
  entities, manual notes via `.z devdata note <text>`.
- `DevDataMacro` — autonomous tour macro: state machine that walks through
  /is → /hub → every /warp → opens /ah and browses Manage/Create/Search →
  opens /bz and browses every category + first product of each. Does NOT
  buy/sell anything. Used to collect dev data in one run.
- `DevDataCmd` — `.z devdata on|off|status|flush|snapshot|note|tour|stop-tour|path`.
- Dashboard Settings tab has "Record dev data" toggle + flush button + tour button.
- Enabled by default in dev config (`devDataAuto=true`).

### Known gaps
- [ ] Does NOT yet reflectively capture SignText lines / method names (the
      SignInputHandler reflection search finds fields but DevDataHarvester
      doesn't record what it sees). Add a SignOpenSnapshot that writes
      sign class name + field names + current text to OBSERVATIONS.md.
- [ ] Boss bar capture uses reflection to probe for "getBossEvents"
      method/field "events"; add fallback.
- [ ] The macro tour clicks the first category and first product; should
      iterate ALL categories and ALL products for a full dump (slow but
      thorough). Configurable via tour.fully=true flag.

---

## 40. Command System (dot-prefix `.z`)

### State ✅
- Command interface: getName/getUsage/getDescription/execute(String[])/suggest(argv default empty)/requiresDebug(default false).
- CommandManager singleton: register/registerAll/get/execute/getAll/tabComplete.
- Registered commands: Help, Status, Failsafe, Flip, Macro, Hud, DevData,
  Lang, Theme, Dashboard.
- CommandInterceptor in MixinClientConnection intercepts outgoing chat packets;
  if content starts with configured prefix (default ".z"), it's routed to
  CommandManager.execute() and cancelled (never sent to server).
- MixinCommandSuggestions injects at HEAD of CommandSuggestions.updateCommandInfo
  to suppress Brigadier's "unknown command" for `.z` and injects our own
  completions from Command.suggest().

### Known issues 🔬
- MixinCommandSuggestions shadow field names are unverified (see §2).
- Commands are SECONDARY to the dashboard (per user spec) — they exist but
  all actions must be possible via GUI.

### To do
- [ ] Add missing commands: `.z warp <name>`, `.z path stop`, `.z route`,
      `.z debug brain`, `.z debug rotation`, `.z debug path`.

---

## 41. Chat System & Pattern Engine

### State ✅🟡
- `ZenithChat.info/warn/error/success/debug(...)` — sends client-side chat via
  ChatHud (no server packet).
- `ChatPatternEngine.init()` registers regex patterns against incoming chat
  and fires events (e.g. chat "You picked up …" → DropTracker; "A Pest
  appeared!" → PestEvent; Ban patterns → BanEvent).
- `ChatReceivedEvent.getMessage()/.getRawFormatted()/.getTypeId()` —
  typeId distinguishes system chat vs player chat vs action bar.

### Known gaps
- [ ] Pattern list is small — needs patterns for: coin pickup, XP gain,
      auction bought/sold, bazaar order filled, warp success/cooldown,
      pest spawn, Jacob start/end, visitor arrived, etc. Each of these is
      `[RESEARCH NEEDED]` until DevData captures the exact text.
- [ ] Action bar parsing for HP/mana/energy/defense not yet implemented
      (needed for LOW_HEALTH).
- [ ] Chat timestamp suppression not implemented.

---

## 42. World Hook & Player State Trackers

### State 🟡
- `WorldHook.init()` — registers world-load callbacks, fires WorldChangeEvent.
- `MCWorldAdapter` — wraps Minecraft level with helper queries (getLoadedChunks,
  nearby entities, blockState at pos).
- `PlayerPositionTracker.getInstance().tick()` — records last-on-ground, position
  history (for lagback detection).
- `PlayerHealthMonitor.getInstance().tick()` — ❌ reads vanilla `player.getHealth()`
  / `player.getFoodData().getFoodLevel()` (useless in SkyBlock).
- `World.get().playerReady()` — checks player != null && level != null && connected.

### To do
- [ ] Action bar → HealthParse regex feed into PlayerHealthMonitor (HP,
      Absorption, Mana, Defense, Speed).
- [ ] Inventory tracker (hotbar contents) for wrong-tool detection (read
      each tick, diff, fire HeldItemChangeEvent).
- [ ] Nearby entity tracker (populates PlayerNearbyDetector).

---

## 43. GUI Framework (ZenithScreen / ZenithScreenWrapper)

### State ✅
- `ZenithScreen` — abstract base with render/mouse/keyboard/init/close hooks;
  `ZenithScreenWrapper.open(new ZenithScreen())` wraps it as a vanilla Screen
  with dark 0x55000000 fill, ESC closes, `isPauseScreen = false`, calls
  AnimationEngine.frame() per frame.
- `GuiDrawContext` wraps GuiGraphics with fillRect/drawRoundedRect/drawOutline/
  drawGradient/drawString(text,x,y,color,shadow)/stringWidth.
- `ZenithToggle` — animated pill toggle button.
- `ZenithTabBar` — horizontal tab bar with animated underline via AnimatedValue.
- `AnimatedValue` — float animator with spring/easing used by widgets.
- Animation system: `AnimationEngine` + `AnimationGroup` with onOpen/onClose actions.
- Theming: `Theme` fields (background/border/panel/accent/textPrimary/textSecondary).

### Known gaps
- [ ] `drawRoundedRect` radius should be configurable (hard-coded).
- [ ] Scroll panel component for long lists.
- [ ] Slider / number input / dropdown components (needed for config UI).

---

## 44. Dashboard GUI

### State ✅
- `DashboardScreen` with 5 tabs: Home, Flipper, Failsafe, Macros, Settings.
- Start/Stop/Resume/Panic/Disconnect buttons.
- Toggles wired to config (failsafe toggle, flipper toggle, devdata toggle,
      etc.).
- Live status panels (flipper stats, failsafe status, macro list).

### Known gaps
- [ ] Macros list doesn't dynamically populate from MacroManager; shows
      hard-coded Idle/Melon/Pumpkin entries.
- [ ] Flipper tab doesn't show live OrderManager active orders list.
- [ ] Config editing for budget/thresholds not yet wired.
- [ ] Theme preview not implemented.

---

## 45. Brain View (GuiEngine)

### State 🟡
- A 400×290 debug panel (toggle via OPEN_DASHBOARD + Ctrl? or `.z debug brain`).
- Shows Flipper block (AH state, BZ state, busy flag), Failsafe block (reaction state stub).

### To do
- [ ] Add rotation state (current/target yaw/pitch, priority, active profile).
- [ ] Add path state (planned nodes, distance to target, stuck flag, compute ms).
- [ ] Add detector states (which failsafe detector is currently triggered,
      if any).
- [ ] Add tick timing (subsystem ms per tick).

---

## 46. HUD Panels

### Files
HudManager registers panels; HudPanel base class.

| Panel | State | Notes |
|---|---|---|
| Watermark | ✅ | "Zenith vX" bottom-left |
| ModuleList | ✅ | Active modules list |
| Safety (dot) | ✅ | Green/amber/orange/red with label |
| FarmingStatsPanel | 🟡 | Header + dynamic icon/activity/skill set on macro start; body is placeholder |
| CombatProfitPanel | 🟡 | Header; body placeholder (no drop tracking wired) |
| SessionPanel | 🟡 | Header; inventory/skills/jacob placeholders |

### Known gaps ❌
- **No live data wired.** Panels draw header + placeholder text. Need:
  - FarmingStats: crops/min, blocks/s, farming XP/hr, yaw/pitch, session time.
  - CombatProfit: drop list with right-aligned coin column, total profit.
  - Session: inventory worth, skills XP, Jacob's contest info.
- **Default positions** not set in HudManager.applyLayout() except
      SafetyPanel and ModuleListPanel. New panels stack at (0,0) unless pinned.
- `.z hud toggle <panelId>` command exists but doesn't set visible flag on panel.
- Panel drag-to-reposition not implemented (edit mode).

---

## 47. Theme & Language Managers

### State 🟡
- `ThemeManager` loads theme by name, has "dark"/"amethyst"/"ember"/"ocean"/"custom".
- Custom theme uses customThemeHue for HSL accent.
- `LanguageManager` loads en_us/en_gb (defaults to en_gb); loads language
  JSON from assets (not populated — see Assets).
- Themes/Language switches update live.

### Acceptance criteria
- [ ] Populate `assets/zenithclient/lang/en_gb.json` (per ASSET_PROMPTS §8).
- [ ] Add more themes (solarised, nord) after asset pipeline.

---

## 48. Keybind System

### State ✅
- `ZenithKeybinds` enum of 12 binds (OPEN_DASHBOARD, EMERGENCY_STOP, TOGGLE_HUD,
  HUD_EDIT_MODE, DEBUG_MODE, MACRO_TOGGLE, PANIC_BUTTON, FREECAM, ZOOM, SAVE_CLIP,
  TOGGLE_FLIPPER, AUTOPILOT_OVERRIDE).
- `KeybindConfig` defaults set; `KeybindManager.init()` loads persisted binds
  from MainConfig.keybinds and checks conflicts.
- `KeybindManager.register(key, action)` + `onKeyPress(key)` dispatch.
- `rebind(key, glfwName)` persists to config + requests save.

### Known gaps
- [ ] Fabric KeyBinding registration — Phase 3 note said "registration happens
      when MixinKeyboardHandler is complete" which it isn't. Current keys
      work via our MixinKeyboardHandler reading GLFW key codes directly but
      they don't appear in vanilla Controls menu.
- [ ] Key conflict checking against vanilla keybinds not implemented
      (only Zenith-internal conflicts).

---

## 49. Bits Protection

### State ✅
- `BitsSpendBlocker` all-static: `preClick(featureId, costBits, shiftHeld)`
  called before every bits-spending GUI click (bits = Hypixel bits from
  Booster Cookie).
- `setBlocked(boolean)` / `isBlocked()` gates based on config + failsafe.
- GUIClickExecutor calls preClick before sending click packets.

### Known gaps
- [ ] No UI to grant bits-spending consent per feature.

---

## 50. Discord Webhook / Notifications

### State ❌
No sounds, no Discord webhook, no toast notifications for flip completed /
failsafe fired / macro stopped. Phase 20.

- Notification sounds specified in ASSET_PROMPTS §7 (notification.wav,
  flip_complete.wav, failsafe_alert.wav, etherwarp_chime.wav).
- Discord webhook should post flip completions + failsafe alerts to a user-
      configured URL.

---

## 51. Build / Obfuscation / Distribution

### State
- Fabric Loom 1.15, Gradle 9.4, Java 21 bytecode target.
- `gradle-wrapper.jar` is a 0-byte placeholder bootstrapped on first run.
- `proguard-rules.pro` exists (ProGuard obfuscation) but not yet tested.
- No release pipeline / GitHub Actions workflow.

### To do
- [ ] Set up GitHub Actions CI: build on PR, upload artifacts on release.
- [ ] Test proguard-rules.pro doesn't eat reflection in EventBus/ConfigManager
      (need -keep for @SubscribeEvent annotated methods and Gson serialised classes).

---

## 52. Asset Pipeline

### State
- Rejected procedural Pillow art (deleted in commit 1e0381a).
- `docs/ASSET_PROMPTS.md` is the mega-prompt covering: brand mark (16/64/128/512 icons + compact/full logo + social + favicon), 24 GUI widget PNGs, hotbar pill, 30 category icons, ~130 macro icons, 3 animated sprite sheets (spinner/ripple/alarm pulse), 4 WAV notification sounds, language JSON files (16 locales), splash screen spec, negative prompts & style suffixes.
- Logo directory `assets/zenithclient/textures/logo/` is empty.
- `fabric.mod.json` references `assets/zenithclient/icon.png` — file does NOT exist
  (first batch of generated art must include a 128px icon.png at that path for
  Fabric to load).
- GUI widgets directory, hotbar, icons/categories, icons/macro/* directories
  exist but PNGs are missing/placeholders.
- sounds.json exists but WAVs are missing.
- lang/ has placeholder or empty JSONs.

### Acceptance criteria
- [ ] Generate all assets per ASSET_PROMPTS.md (Midjourney/Flux/SDXL or human
      artist), drop into specified paths.
- [ ] Translate en_gb.json to at least the 14 other locales in the spec, or
      accept community contributions.

---

## Immediate Priority Order

1. **Fix PlayerHealthMonitor** to read HP from action bar (unblocks LOW_HEALTH).
2. **Get the dev environment to compile** (fix mixin shadow names on first
   build — MixinCommandSuggestions, SignInputHandler 1.21+ SignText).
3. **Run DevDataMacro tour on an alt** and populate OBSERVATIONS.md — this
   pins down every 🔬 guess above.
4. **AbstractFarmingMacro end-of-row turns** → Melon/Pumpkin actually farm
   more than one row.
5. **AH Buy flow: click Sort → Price Low→High before search** (profitability
   blocker).
6. **Implement ZenithPath A* walk** (unblocks REPATH from failsafe and mining macros).
7. **Add ease-in-out rotation curves + key micro-pauses** (stealth before running
   any macro on main).
8. **SignInputHandler 1.21+ SignText support** (unblocks AH/Bazaar quantity/price
   signs — needed for profitable flipping).
9. **Wire live HUD panels** (BlockBreakTracker, DropTracker, ProfitTracker data
   into FarmingStats/CombatProfit/Session).
10. **Other crop macros** (carrot/potato/wheat with replant; cane/cactus
    column-break; cocoa side-farming).
11. **Cross-server TravelEngine** (NPC/jump-pad/warp-fallback for REPATH).
12. **Fill monitor + manage orders walks** (relist undercuts, claim fills).
13. **Bazaar categories map from DevData dump** (replace BazaarCategory hard-coded map).
14. **Mining macros** (after ZenithPath walks).
15. **Combat macros** (after ZenithEyes tracking implemented).
16. **Phases 16-20** (fishing/foraging/events/dungeons/kuudra/rift/discord/polish).

---

## Open Research Questions

Tracked in `docs/RESEARCH.md` (R001-R047). Key items DevData must answer:

- R002 Exact scoreboard layout in each SkyBlock zone (lines, positions, HP/purse/defense format).
- R003 Tab-list header/footer format for staff detection.
- R005 Boss bar titles in combat / events.
- R041 AH sort button exact name; sort-click sequence.
- R042 Full Bazaar sign/button titles (quantity, price, buy order, sell order,
  manage orders fill buttons).
- R045 Death screen class / title in SkyBlock.
- R046 Action bar HP regex (❤/HP/Defense/Absorption numbers).
- R047 SignEditScreen field names in 26.1 (SignText record API).

The DevDataMacro tour exists specifically to answer all of these in one run.
