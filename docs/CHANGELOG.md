# CHANGELOG

All notable changes to the Zenith Client are tracked here. The format is
loosely based on Keep a Changelog; phases correspond to the 20-phase master
roadmap.

## [1.0.0] - Phase 10 revision — Failsafe behaviour overhaul + HUD corrections

### Changed (per user direction)
- **Escape command is `/is`, never `/home`:** Warp actions now send `/is`
  (private island) via `BanActionHandler.sendIsland()`. `/home` is deprecated
  and aliased to `/is`. `/hub` is still the fallback.
- **Low health fights back, does NOT warp:** LOW_HEALTH / LOW_HUNGER severity
  changed from WARP_HOME to COMBAT. The new `CombatReactionAction` aims
  ZenithEyes at the nearest hostile within 6 blocks and swings the held item
  via KeySimulator, auto-clearing after 4 s. Hypixel custom HP is accounted
  for via PlayerHealthMonitor.
- **Wiggle reactions for camera snaps:** new `WiggleReactionAction` plays a
  ~700–1200 ms ±20° yaw / ±7° pitch human-looking wiggle after rotation
  snapback / yaw flip / player-nearby / wrong-tool events. `ChatQuestionMarkAction`
  sends a single "?" in chat (rate-limited to 30 s) when a snap happens.
- **Obstruction detection + block-breaking:** new `ObstructionDetector` fires
  when the player is stuck (forward input, ~0 horizontal velocity, solid block
  ahead for >1.2 s). `RemoveObstructionAction` pauses, looks at the block,
  optionally sends "?", left-clicks (via KeySimulator.setAttack) to break it
  for up to 2.5 s, then resumes or repaths around if it won't break.
- **Instant respawn + repath on death / teleport / world-change:** DEATH now
  triggers INSTANT_RESPAWN (clicks the DeathScreen "Respawn" button or calls
  `player.respawn()`), then REPATH. TELEPORT / LAGBACK / WORLD_CHANGE / LIMBO
  also trigger REPATH. `RepathReactionAction` calls ZenithPath.requestPath
  back to the active macro's destination provider (walking + sprinting +
  etherwarp + jump pads + NPCs — macro modules register their anchor via
  `setProvider(...)`).
- **Reactive severities no longer freeze macros:** WIGGLE_REACT / COMBAT /
  REMOVE_OBSTRUCTION / INSTANT_RESPAWN / REPATH run alongside the running
  macro instead of pausing it — they drive camera/keys/path without halting
  the macro tick. Only PAUSE / WARP_ISLAND / WARP_HUB / DISCONNECT actually
  freeze input/macros.
- **HUD panels corrected:** `FarmingStatsPanel` no longer hardcodes a
  watermelon — icon/activity/skill are set dynamically via
  `setActiveMacro(icon, activity, skill)` depending on whichever macro is
  running. The "Ghost Profit" panel is renamed to generic `CombatProfitPanel`
  (the reference screenshots were design reference, not a literal target).
- **docs/HUD.md** rewritten to emphasise that the three user screenshots are
  design REFERENCES, with a table of which features to borrow from each.
- **FailsafeConfig / FailsafeStrictness** ladder expanded to 10 levels: NONE,
  NOTIFY, PAUSE, WIGGLE_REACT, COMBAT, REMOVE_OBSTRUCTION, INSTANT_RESPAWN,
  REPATH, WARP_ISLAND (/is), WARP_HUB (/hub), DISCONNECT. Safety dot colours
  updated (amber = reaction running, orange = paused/warping).

### Added
- `ChatQuestionMarkAction` — rate-limited "?" sender.
- `WiggleReactionAction` — smooth ±20°/±7° camera jitter via ZenithEyes.
- `CombatReactionAction` — auto-aim + swing at nearest hostile.
- `RemoveObstructionAction` — look-at + break block, repath on timeout.
- `RepathReactionAction` — ZenithPath.requestPath back to active macro anchor.
- `RespawnAction` — instant DeathScreen respawn click.
- `ObstructionDetector` — stuck-against-block detector.
- `CombatProfitPanel` (generic; replaces GhostProfitPanel sub-folder).

## [1.0.0] - Phase 10 — GUI Dashboard + AH Foundation

### Added
- **Zenith Dashboard** (`gui/dashboard/DashboardScreen`): primary control
  surface (per user direction; commands are SECONDARY). 5 tabs (Home, Flipper,
  Failsafe, Macros, Settings) with live Start/Stop/Resume/Panic/Disconnect
  buttons + toggles wired to the running config.
- **ZenithScreenWrapper** bridges ZenithScreen → MC Screen with dark
  semi-transparent fill, ESC close, `isPauseScreen=false`, AnimationEngine
  frame ticks.
- **ZenithToggle / ZenithTabBar** components (animated underline via
  AnimatedValue).
- **DashboardCmd** opens the GUI (`.z dashboard`).
- **Tab completion for dot commands**: `Command.suggest(argv)` default
  method, `CommandCompleter`, `MixinCommandSuggestions` (inject at HEAD of
  CommandSuggestions.updateCommandInfo, cancellable, `require=0`), plus
  suggest() stubs on FailsafeCmd and FlipCmd.
- **GUIInteractionEngine**: central tick subscriber that drives
  GUIParser.tick(), SignInputHandler.tick(), GUIWaiter.checkAll(),
  GUIClickExecutor.tick() every client tick (registered from
  ClientTickDispatcher).
- **CommandSender** (server-bound /ah /home /warp etc.) with 450 ms cooldown.
- **AuctionHouseNavigator**: `/ah` sender, title detection across all AH pages,
  `isInAH()` check, `returnToBrowser()`.
- **GUIWaiter** expanded with static `waitForTitle(substr,timeout)` /
  `checkAll()` / `hasAnyPending()` (uses GUIInteractionEngine tick).
- **AuctionHouseGUI**: Page enum (BROWSER/SEARCH_SIGN/RESULTS/CONFIRM_BUY/
  MANAGE/COLLECT/CREATE/CHOOSE_ITEM/UNKNOWN), slot finders for Search/BIN
  toggle/Sort/BuyConfirm/Create/Manage/Back, `findListings()` scans
  pre-player-inventory slots for auction heads, `parseCoins()` with K/M/B
  suffix support, `parseBinPrice/parseBidPrice` lore parsing.
- **GUIItemMatcher.nameContains / .loreContains** static factories (rule §1).
- **SignInputHandler**: reflection-based sign-line setter + Done button via
  `onDone()` method (replaces earlier speculative keyboard event injection,
  which would have fired our own KeyInputEvent).
- **AuctionHouseExecutor** state machine (buy path complete: IDLE → OPEN_AH
  → TOGGLE_BIN → SEARCH → WAIT_SIGN → WAIT_RESULTS → WAIT_CONFIRM →
  WAIT_BOUGHT). Listing flow (OPEN_MANAGE/CHOOSE_ITEM/CREATE/SET_PRICE/
  CLICK_CREATE/WAIT_LISTED) is deferred to a follow-up iteration.
- **GUIParser singleton** (`getInstance()`) — AuctionHouseExecutor no longer
  creates a second parser.
- **AuctionHouseInteractor** delegates to AuctionHouseExecutor + exposes
  `pump(OrderManager)` so FlipEngine can hand BUYING/NAVIGATING orders to it.
- **HUD panels**: `FarmingStatsPanel`, `GhostProfitPanel` (combat
  sub-package), `SessionPanel` skeletons matching the user's three reference
  screenshots (farming stats with Yaw/Pitch, Ghost-Profit-style itemised
  drops, Pumpkin-style inventory/skills/Jacob layout).
- **docs/HUD.md** spec for the tracker panels; **docs/FLIPPING.md** rewritten
  to match the user's Bazaar→AH flipper subsystem ordering
  (1. Market scanner+API → 2. Tax calc → 3. Item selector+Budget →
   4. Bazaar+AH interactors → 5. Order manager → 6. Profit+History →
   7. Craft engine → 8. NPC engine → 9. AH-craft engine → 10. Break scheduler →
   11. Mayor system).
- OPEN_DASHBOARD keybind wired in FailsafeManager.
- zenithclient.mixins.json: MixinChatScreen + MixinCommandSuggestions added.

## [1.0.0] - Phase 2 — Core Framework

### Added

- **Event bus** (`core.event.ZenithEventBus`): priority-ordered, annotation-driven, thread-safe.
- 35 domain event classes (`core.event.events.*`) covering chat, macro lifecycle,
  failsafe, inventory, world change, flips, mayor, profit milestones, bans, limbo, etc.
- **Module framework** (`core.module.Module`, `@ModuleInfo`, `@SettingInfo`):
  auto-discovered settings, enable/disable lifecycle, event-bus auto-subscribe.
- 8 setting types: Boolean, Number, Slider, Mode, Color, String, Keybind, List.
- **State machine** (`core.statemachine.StateMachine`): mandatory states
  (IDLE/STARTING/RUNNING/PAUSED/RECOVERING/BREAK/STOPPING/ERROR), priority-sorted
  transitions, enter/tick/exit callbacks.
- **Client-side chat** (`core.chat.ZenithChat`): INFO/WARN/ERROR/DEBUG/SUCCESS
  levels, colour-coded prefixes, messages never sent to server (master rule §4).
- **12 utility classes** (`core.util.*`): Math, Color, Text, File, Reflection,
  Thread, Block, Inventory, Sound, Screenshot, Clipboard, Validation.
- **Timer / DelayManager / TickTimer / RandomDelay** — no `Thread.sleep`,
  humanised Gaussian/uniform/reaction-time delays.
- **Keybind reader + mapper** (`core.input.PlayerKeybindReader`/`KeybindMapper`) —
  reads WASD/keybinds from MC Options (no hard-coded W=forward assumption,
  master rule §9).
- **Zenith keybinds** (`keybind.*`): 12 global/debug/camera binds,
  `KeybindConflictChecker` warns of collisions with MC bindings; persistent
  via config.
- **Dot-command system** (`command.*`): `.z <cmd> [args]` intercepted client-side;
  22 commands (help, version, status, toggle, stop, break, macro, flip, route,
  esp, clip, waypoint, hud, debug, theme, profit, mayor, budget, settings,
  export, dashboard, lang).
- **Bits protection** (`core.protection.BitsProtection` + `ExperimentSerumBlocker`):
  blocks accidental bits spending (configurable threshold + per-item blocklist),
  GUI click hook ready for Phase 6.
- **Config manager** (`config.ConfigManager`): Gson-backed 12-file JSON system
  with delayed auto-save, versioned schema, migration chain (ConfigMigrator).

### Changed

- `ZenithClient.onInitializeClient()` now wires Phase 2 subsystems in the
  documented init order (config → events → keybinds → chat → commands →
  modules → protection).
- Config files now actually load from disk and write defaults if missing
  (previously they were empty stubs).

## [1.0.0] - Phase 1 — Build System & Skeleton

### Added

- Full project skeleton (1,900+ file paths across 20 phases).
- Fabric Loom 1.15 build on Gradle 9.4 / Java 25 → Java 21 target.
- 12 surgical Mixin stubs (Mojang names for 26.1, no @Inject methods yet).
- 3 accessor interfaces (Mouse, Options, Camera).
- ProGuard config + dictionary for Phase 20 release obfuscation.
- SETUP.md, README.md, ARCHITECTURE.md, RESEARCH.md (R001 COMPLETE).
- Default `config/zenith.json`, en_gb language file, pack.mcmeta.

## [1.0.0] - Phase 3 — Core Engines (ZenithEyes + ZenithPath + InputEngine)

### Added

- **ZenithEyes (advanced humanized rotation engine)**:
  - `RotationRequest` with 6 priority levels (BACKGROUND→FAILSAFE), tracking mode, duration, per-request profile id, completion callbacks.
  - `RotationProfile` + `RotationProfileRegistry` with `smooth`/`snappy`/`legit`/`silent` presets, all params (acceleration, jitter, overshoot, hesitation, fatigue, output Hz).
  - `RotationQueue` with preemption (FAILSAFE cancels everything, high-priority preempts preemptible), overflow eviction.
  - **Curve layer**: `BezierRotationCurve` (Newton-Raphson inversion with bisection fallback), `CubicRotationCurve` (smoothstep/smootherstep), `ControlPointGenerator` (per-turn randomised control points, horizontal/vertical bias, distance scaling), `CurveBlender`.
  - **Humanizer stack (9 modules)**: `AccelerationModel` (Fitts's law accel/decel), `OvershootCorrector` (probabilistic small overshoot past target), `MicroCorrectionEngine` (1-3 tiny nudges after turn), `JitterInjector` (Gaussian sensor tremor), `HesitationEngine` (random 40-120 ms mid-turn pauses), `TickQuantizer` (discrete output at configurable Hz), `MouseSensitivitySimulator` (converts degrees ↔ mouse-delta units using MC's sensitivity curve), `GazeHistory` (rolling velocity window), `FatigueModel` + `ContextualBehavior` (alertness, combat, menu, session-minutes modulate speed/jitter/pauses).
  - **Behaviour layer**: `CameraWander` (subtle idle sine breathing), `IdleWander` (large slow gazes after 4s idle), `WalkingGlance` (side-glances while walking), `EnvironmentAwareness` (glances at nearby players/entities when something is "noticed").
  - `RotationExecutor` state machine that runs one request through the full pipeline each frame.
  - `RotationDebugData` snapshot for Brain View.

- **ZenithPath (advanced humanized pathfinding + movement)**:
  - Internal `BlockPos` (decoupled from MC classes for unit testing; adapters added in Phase 3+6).
  - `PathMode` (WALK / ETHERWARP / FLY / ROTATE_ONLY), `PathRequest`/`PathResult` (FOUND/PARTIAL/UNREACHABLE/TIMEOUT).
  - **A\* Pathfinder** (`AStarPathfinder`): octile heuristic, timeout-bounded, open heap + closed set, partial results, jump/drop/walk primitives from `JumpChecker` (cardinal, diagonal, jump-up 1, drops up to 4, sprint parkour up to 4 blocks).
  - `WalkabilityChecker` with pluggable `WorldView`, `canStandAt`/`canJumpTo`/`canDropTo`.
  - `EtherwarpPathfinder` (range 57 blocks, LOS stub; Phase 6 adds raycast), `FlyingPathfinder` (debug straight-line).
  - **Post-processing chain (8 modules)**: `PathSmoother` (forward-LOS greedy smoothing via Bresenham 3D), `NaturalDeviator` (perpendicular Gaussian jitter, subdivision into 4 sub-block waypoints = 0.25 block granularity markers), `PathSegmenter` (segments into STRAIGHT/TURN/JUMP/DROP/ETHERWARP), `CourseCorrector` (drift detection with automatic recompute), `ArrivalOvershoot` (probabilistic tiny overshoot + settle), `RouteMemory` (LRU of up to 64 routes), `WallAwareness` (wall-gap offset), `TerrainAdaptation` (slab/stair height stub).
  - **Movement controllers**: `MovementSimulator` (per-tick forward/strafe/jump/sprint), `StrafeController` (world→local transform + diagonal normalisation + input smoothing), `SprintController` (probabilistic sprint-tapping, turn-based release).
  - **Speed detection stack**: `SpeedDetector` aggregates sources: walk/sprint base, Speed potions per level, Rancher Boots, armor bonuses, farming bonuses, `SpeedAdaptiveTimer` (closed-loop speed measurement), `SpeedConfig` constants.
  - **Movement humanizer (6 modules)**: `SprintVariation` (burst/pause), `JumpVariation` (pre-edge jump timing + air-strafe bias), `DirectionNoise` (sinusoidal wobble + micro-corrections), `ThinkingPauses` (infrequent 200-500ms stops), `StutterStep` (occasional tap-steps), `ObstacleRecovery` (BACKOFF→STRAFE→JUMP→REPATH state machine).
  - **Path variation engine**: `PathVariationConfig`, `VariantPathGenerator` (randomly injects mid-segment nodes), `RouteHistoryTracker` (per-pair history to avoid repetition), `DeathRecoveryRouter`, `PathVariationEngine` orchestrator.
  - **Reactions**: `AnnoyedReactionEngine` (probabilistic visible micro-behaviours on errors), 6 reactions: StuckOnBlock/GUINotOpening/PathBlocked/WrongItem/Lag/InventoryFull.
  - **Movement learning system** (records + adapts to player over time):
    - `MovementSample` (tick-level telemetry), `MovementRecorder` (10-minute / 20 Hz circular buffer, auto-pauses when macro active), `MovementProfile` (mean+stddev for every dimension), 10 analyzers: Speed, Turn, Jump, Sprint, Pause, PathDeviation, ClickPattern, LookPattern, Strafing, Arrival.
    - `ProfileBuilder` distills samples → profile, `ProfileExporter`/`ProfileImporter` (Gson to data/movement_profiles/*.json), `ProfileValidator` bounds-clamps, `ProfileMerger`/`ProfileBlender` blend learned weights into active profile.
    - `DefaultProfile` (hand-tuned "average good player" baseline — works brilliantly out of the box without any learning).
    - `MovementLearner` retrains every 30 s when >200 samples, blends at a slowly-decaying weight (so early data doesn't dominate), persists every couple of retrains.
  - `PathExecutor` runs the waypoint list through all humanizers; `ZenithPath` orchestrates compute→smooth→deviate→execute with auto-repath.

- **InputEngine**: central tick that pulls movement inputs from ZenithPath and rotation from ZenithEyes, writes them to `KeySimulator`/`MouseSimulator`/`ClickSimulator`. Direct Minecraft wiring lands in Phase 4 (mixin hooks).
- Basic render stubs: `Color4f`, `RenderContext`, `ZenithRenderer`, `MC26Renderer` (Phase 5 fills in Blaze3D calls).
- Clicking pipeline stubs (`ClickFatigue/Humanizer/Profile/Variation/HoldDurationVariation/InterClickDelay`).
- Updated `ZenithClient` init order to bring up ZenithEyes, ZenithPath, and InputEngine.

### Design notes

- The default movement profile (`DefaultProfile.build()`) is production-grade from day one — learning only refines behaviour toward the individual player over sessions, never being required for legitimacy.
- Waypoints are subdivided to ~0.25-block granularity, giving the bot many tiny movement markers rather than chunky integer-coordinate targets, per master spec ("lots of small movement markers").
- All rotation/movement math uses continuous (non-integer) positions/angles; integer block positions are only used at the pathfinding layer.
- Server-invisible: ZenithEyes + ZenithPath + InputEngine produce only client-side key/mouse state changes — no custom packets, no velocity teleport, per master rule §3.

## [1.0.0] - Phase 4 — Mixins wired + GUI foundation

### Added
- All 12 mixins now target live Minecraft entry points (Mojang names, 26.1):
  - MixinMinecraft: `tick()` fires ClientTickEvent; `getClientModName()` spoofed to "vanilla" (server invisibility).
  - MixinClientConnection: `send(Packet)` fires PacketSendEvent, cancels dot-command chat packets via CommandInterceptor so `.z ...` never reaches Hypixel. Receive hook with `require=0` for version resilience.
  - MixinGameRenderer: `renderLevel` RETURN fires RenderWorldEvent for ESP/path visualisation.
  - MixinGui: `render` RETURN fires RenderHudEvent for HUD panels.
  - MixinMouse: `onMove`/`onPress` fire MouseDeltaEvent/MouseInputEvent (for synthetic movement + click humanizer).
  - MixinKeyboardHandler: `keyPress` fires KeyInputEvent and dispatches ZenithKeybinds.
  - MixinCamera: `tick()` syncs yRot/xRot back to ZenithEyes each frame.
  - MixinOptions: implements OptionsAccessor to expose sensitivity.
  - MixinEntityRenderer / MixinLevelRenderer / MixinScreen / MixinPlayerRenderer: present as no-op targets for later phases so mixin config remains valid.
- **New events**: ClientTickEvent, RenderWorldEvent, RenderHudEvent, MouseInputEvent, KeyInputEvent, MouseDeltaEvent, PacketSendEvent, PacketReceiveEvent.
- **ClientTickDispatcher** central subscriber (ModuleManager.tickAll, InputEngine.tick, MovementLearner.tick).
- **Animation framework** (moved from engine/render/animation → gui/animation to avoid duplication): AnimatedValue/Vec2/Vec3/Vec4/Color, EasingFunctions (8 easings including elastic/back/bounce), SpringAnimation (critically-damped springs), AnimationGroup/Sequence, AnimationEngine tick driver.
- **GUI foundation**:
  - Theme system (Theme + ThemeManager, dark/light palettes; custom-hue hook reserved).
  - Component base class, GuiDrawContext API (fillRect, drawRoundedRect, drawString, drawGradient — Phase 5 binds to Blaze3D).
  - Label + Button components with hover fade animation.
  - ZenithScreen base class + DashboardScreen placeholder.
  - ToastManager for corner notifications (info/warn/error/success).
  - HudManager + WatermarkPanel + ModuleListPanel (render as HUD overlays via RenderHudEvent).
  - LanguageManager: loads `assets/zenithclient/lang/*.json` with en_gb fallback and `config/lang/<code>.json` overrides.
- Server invisibility live: mod brand returns "vanilla" and dot commands are intercepted client-side with zero server traffic.

### Changed
- engine/render/animation removed (duplicate of gui/animation).
- GuiEngine now subscribes to RenderHudEvent and renders watermark + module list + toasts.
- ZenithClient.init now wires Phase 4: GuiEngine.init() + ClientTickDispatcher.register().

## [1.0.0] - Phase 5 — HUD system & Brain View

### Added
- Real rendering wired to MC 26.1 (GuiGraphics):
  - RenderBridge wraps all draw calls (fillRect, drawString, outline, gradient, rounded rect, stringWidth).
  - GuiDrawContext now accepts a live GuiGraphics instance and routes through RenderBridge.
- Render events updated:
  - RenderHudEvent carries GuiGraphics, screen width/height, partial tick.
  - RenderWorldEvent carries PoseStack for ESP/path visualisation.
  - MixinGui/MixinGameRenderer fire the events with correct context.
- Color4f: added withRed/withGreen/withBlue/withAlpha helpers.
- **HUD Panel framework**:
  - HudPanel base (position, anchor, drag-resize, visibility).
  - HudLayout persisted to config/hud_layout.json via HudLayoutManager (Gson, schema-versioned).
  - HudManager subscribes to RenderHudEvent, renders all registered panels, applies persisted layout on init.
  - WatermarkPanel + ModuleListPanel (live render).
  - All 17 panel stubs (ActiveFlips, Budget, BreakTimer, DebugBrain, Info, Keystrokes, MacroStats, Mayor, Music, Profit, Safety, Status, etc.) placed as non-drawing HudPanel subclasses ready for later phases.
- **HUD Editor**:
  - HudEditor toggle via `.z hud edit`.
  - DragController (per-panel mouse drag), SnapEngine (4px grid + edge snap), HudEditOverlay (coloured border + panel label + grid lines).
  - Auto-save layout on release via HudLayoutSaver.
- **Brain View debug overlay** (`.z debug brain`):
  - Centred translucent panel showing full snapshot of both engines:
    - Eyes: state, tag, profile, current yaw/pitch, target yaw/pitch, angular velocity, overshoot/correct/hesitate flags, fatigue, queue count, priority.
    - Path: state, mode, current speed, planned/executed nodes, cost, distance, stuck flag, current reaction, compute ms.
- Component stubs for all 26 Zenith* GUI components (Badge, Button, Card, Checkbox, ColorPicker, ContextMenu, Divider, Dropdown, Icon, Modal, Notification, ProgressBar, ScrollPane, SearchBar, Separator, Slider, TabBar, TextInput, Toggle, Tooltip, Scrollbar, Header) — extend Component and render no-op, ready for later GUI phases.
- Animation framework (gui/animation) deduplicated & retained: AnimatedValue/Vec2/Vec3/Vec4/Color, SpringAnimation (critical damping), 8 easing functions, AnimationGroup/Sequence, AnimationEngine per-frame driver.
- Theme system (Theme + ThemeManager) dark/light palettes wired; HudManager + GuiDrawContext use it.
- LanguageManager: loads assets/zenithclient/lang/<code>.json with en_gb fallback and config/lang overrides.
- DebugCmd extended with `brain`, `threads`, `state` subcommands; HudCmd supports `edit/on/off/reset`.
- ZenithClient banner updated to Phase 5.

### Changed
- GuiEngine no longer directly renders watermark/module-list (delegated to HudManager, which owns panels and is layout-aware).
- WatermarkPanel/ModuleListPanel now extend HudPanel.
- engine/render/animation removed; canonical animation package is gui/animation.

## [1.0.0] - Phase 6 — World interaction & GUI parsing

### Added
- **WorldAdapter** (World + MCWorldAdapter + EmptyWorldAdapter): full abstraction over MC block/entity/player state. Engine code no longer imports MC classes — pathfinding/LOS checks go through the adapter.
- **MCWorldAdapter** live implementation for 26.1 Mojang names: BlockState.isSolid, canOcclude/blocksMotion, ladder check, fluid checks (WATER/LAVA), BlockHitResult raycast via Level.clip(), player position/yaw/onGround/screen title/health/food, SkyBlock detection via scoreboard title.
- **WorldHook** installs/resets the adapter on world join/disconnect (WorldChangeEvent/DisconnectEvent).
- **Player trackers**: PlayerPositionTracker (velocity, yaw-vel, stuck detection, blocks travelled), PlayerHealthMonitor (recent-damage window), PlayerStateDetector (moving/sprinting/combat/menu/falling/dead/riding), CooldownTracker (per-id cooldowns), SkillXPTracker (levels by skill name).
- **ClientTickDispatcher** now drives player trackers.
- **GUI system** (rule §1: no hardcoded slots):
  - GUIItemMatcher (display-name-contains/exact/skyblock-id/lore/enchanted/glint/stack-size predicates).
  - GUIItemStack snapshot (decoupled from MC ItemStack, reads display name, lore, ExtraAttributes.id, enchanted/glint/count).
  - GUISlotFinder finds first/all matching slots and buttons by label — never by index.
  - GUIState (title, containerId, rows, slot list, player-inventory start, button labels).
  - GUIParser reads AbstractContainerMenu each tick, builds GUIItemStacks from ItemStack+NBT, collects Button labels, fires InventoryOpenEvent/InventoryCloseEvent on title change.
  - GUIClickExecutor wraps gameMode.handleInventoryMouseClick, applies DelayManager humanised delay, **BitsSpendBlocker.preClick** on click, supports left/right/shift/drop.
  - GUIWaiter condition waiter (poll-based, no Thread.sleep).
- **ChatPatternEngine** (+ChatPattern) compiles 27 regex patterns matching SkyBlock messages and fires the corresponding domain events: LimboDetected, BanDetected (ban+mute), WorldChange, CoopMessage, RareDrop, ItemCollected, PurseChange, SellComplete, SkillLevelUp, GardenLevelUp, CropMilestone, JacobContestStart, PestSpawn, VisitorArrived, CommunityUpgradeReady, MuseumMilestone, MayorChange, PlayerNearby, MacroStart/Stop, BreakStart/End, FlipComplete, BigFlipComplete, Disconnect/Reconnect, PINAttempt.
- **MixinClientConnection** updated:
  - Intercepts both send() overloads for chat packets.
  - Reads incoming ClientboundSystemChatPacket/ClientboundPlayerChatPacket and fires ChatReceivedEvent → ChatPatternEngine.
  - Packet cancellation respected.
- **EtherwarpPathfinder** now uses World.raycastClear for real LOS (replaces the stub); max range 57 blocks, eye-level start → feet target.
- **PathNavigator** bridge installs the WalkabilityChecker.WorldView from WorldAdapter so A* operates on real MC blocks once in-world.
- 28 SkyBlock GUI parser stubs (AccessoryBag, Anvil, AuctionHouse, Bank, Bazaar, BrewingStand, Calendar, Collection, CommunityShop/Upgrades, CraftingTable, EnchantmentTable, EnderChest, Forge, Garden, Greenhouse, Kuudra, Minion, Museum, PetMenu, Quests, Rift, Skills, SkyBlockMenu, Storage, Trade, Visitor, Wardrobe) and 8 click-pattern stubs (ApplyEnchant, BuyFromBazaar/NPC, CraftItem, ListOnAH, SearchAH, SellToBazaar/NPC).

### Changed
- ZenithClient banner updated to Phase 6.
- ChatPatternEngine registered & initialised.
- WorldHook.init() called from ZenithClient.
- HudManager initialised in GuiEngine chain.

### Design
- All engine→world access is now fully decoupled via WorldAdapter. Tests can substitute fake worlds; MC-specific code lives only in MCWorldAdapter/MixinClientConnection.
- Rule §1 enforced: GUISlotFinder is the only sanctioned way to find item/button slots.
- Rule §4 enforced: dot-command interception and brand spoofing both live; incoming chat never leaves the client.

## [1.0.0] - Phase 7 — Failsafe System

### Added
- **FailsafeManager** — central orchestrator. Polls 19 detectors every client tick, aggregates active triggers into a single `FailsafeStrictness` ladder (NONE → NOTIFY → PAUSE → WARP_HOME → WARP_SPAWN → DISCONNECT), auto-escalates per trigger on `escalationStepMs`, applies severity actions (freeze input, halt path/eyes, block bits spending, send `/home`, `/hub`, or disconnect).
- **FailsafeConfig** (persisted to `config/failsafe.json`): per-detector enabled flag, severity override, sound alert (name/volume/pitch/repeats), toast alert, Discord-alert toggle, escalation step, grace period, auto-reconnect, max auto-severity cap.
- **19 detectors** (`failsafe/detection/`):
  - PlayerNearby — entity scan (32b radius) + Hypixel chat proximity.
  - Ban — `DisconnectedScreen`/TitleScreen detection + BanDetectedEvent (mute/ban).
  - Limbo — LimboDetectedEvent + empty-world heuristic.
  - Teleport — per-tick Δpos >3.5h/5v with etherwarp-expected hook.
  - Velocity — Δv >3 vy or >1.8h while airborne (anti-cheat knockback signature).
  - ItemSwap — held-stack identity change outside `expectSwap(window)` windows.
  - Rotation — |yawΔ| >60° / |pitchΔ| >45° vs ZenithEyes expectation (snapback).
  - WorldChange — WorldChangeEvent subscription.
  - GUIClose — InventoryCloseEvent outside `expectClose()` windows.
  - WrongItem — held item name substring mismatch against `setExpected(...)`.
  - YawFlip — >150° yaw delta in one tick while on-ground.
  - InventoryFull — all 36 main slots filled.
  - Lagback — >0.8b horizontal jump while on-ground.
  - Dismount — rider→not-rider transition outside `expectedDismount()`.
  - Death — PlayerStateDetector.isDead() + DeathScreen.
  - LowHealth — ≤6 HP OR hunger 0 → WARP_HOME.
  - Etherwarp — hook for etherwarp confirmation (future path callback).
  - PlayerClone — ClientboundRespawnPacket/LoginPacket → teleport signal.
  - Disconnect — DisconnectEvent + connection-null screen check.
- **ReactionEngine** (`failsafe/reaction/`): runs short human "oops" sequences after triggers, so a pause looks like a surprised human rather than a bot halt. Sequences built via `ReactionSequenceBuilder` and composed from:
  - FreezeAction — Gaussian 250–900ms motionless pause.
  - PanicLookAction — 400ms snappy ±25° jerk to a random direction (FAILSAFE priority rotation).
  - SlowLookAroundAction — 900ms "legit" profile ±20–45° sweep.
  - RandomMovementAction — marker for a tiny back-step (no automatic movement while paused).
  - AccidentalChatAction — opens chat for ~600ms, types nothing, closes without sending.
  - ChatResponseAction — pre-fills chat with a response (e.g. "brb"), player sends manually.
  - EtherwarpEscapeAction — queues a 25b etherwarp path in the facing direction as last-resort.
  - InventoryOpenAction — opens inventory 800ms "which item am I holding?"
  - MistakeSimulationAction — two small 3–8° twitches then back.
- **Support classes**:
  - FailsafeType enum with default severity + auto-escalate flags.
  - FailsafeStrictness ladder with monotonic ordering and `atLeast(...)`.
  - FailsafeSoundPlayer — async main-thread sound repeat (configurable alert).
  - BanActionHandler — `/home`, `/hub`, disconnect with per-action 1.5s cooldown; uses ServerboundChatCommandPacket (26.1).
  - PlayerNotifier — bounded toast queue (5 toasts, 6s expiry) for the HUD panel.
  - SafetyStatusMonitor — coloured status dot + de-bounced severity for HUD.
  - PanicButton — bound to PANIC_BUTTON key; instant halt; held 400ms disconnects.
  - TabInHandler — pauses if window focus is lost mid-macro; tab-back does NOT auto-resume.
  - FailsafeDebugData — immutable snapshot consumed by Brain View.
- **BitsSpendBlocker** gained global `setBlocked(boolean)` atomic flag — failsafe PAUSE and above blocks all inventory clicks unconditionally.
- **New events**: HeldItemChangeEvent, TeleportEvent, DeathEvent, ScreenChangedEvent (added for future Phase 9+ consumers; detectors already live on existing events).
- PANIC_BUTTON and EMERGENCY_STOP keybinds wired in FailsafeManager.init().
- FailsafeManager.tick() added to ClientTickDispatcher.
- ZenithClient banner & startup message updated to Phase 7.

### Changed
- `ClientTickDispatcher.onTick` now calls `FailsafeManager.getInstance().tick()` after learner/module/input tick.
- `ZenithClient.onInitializeClient` calls `FailsafeManager.getInstance().init()` immediately after `WorldHook.init()`.
- `ConfigManager` registers `failsafe.json` (FailsafeConfigFile) as the 13th persisted config.
- `BitsSpendBlocker` is now instantiable-style with static atomic gate (no-op for existing preClick callers).
- Failsafe trigger logging uses WARN level (visible by default); debug lines use SLF4J DEBUG.

### Design
- Severity is monotonic per trigger; escalation is per-type with configurable step (default 3.5s) and capped by `maxAutoSeverity` (default DISCONNECT).
- Clearing requires user action — the manager never auto-resumes macros after a trigger.
- Detectors never move the player, send packets, or toggle modules; they only call `trigger(type, reason)` / `clear(type)` on the manager.
- ReactionEngine runs only below WARP_HOME severity; once escape actions start, reaction theatrics stop.
- Rule §4 (server invisibility) preserved — reaction chat actions never auto-send; chat is only opened/pre-filled.
- Rule §5 (no fixed delays) preserved — freeze durations use Gaussian jitter, action timings are humanised.

## [1.0.0] - Phase 8 — API / Data Layer

### Added
- **ApiManager** — orchestrates all external data integrations; boots wiki, scrapers, NEU fetch, Moulberry poller, Coflnet endpoints, update checker.
- **HttpClient** (`api/HttpClient.java`) — JDK 11 `java.net.http` wrapper with configurable UA, 8 s timeout, async GET, UTF-8 query builder, runs on `ThreadUtils.ioPool()`.
- **RateLimiter** + **RateLimitConfig** (`api/ratelimit/`) — token-bucket per endpoint (coflnet.flips 1/1.5s, coflnet.prices 2/s, neu.repo 1/min, moulberry.bin 1/10s, update.check 1/h).
- **Caches** (`api/cache/`):
  - DataCache — generic in-memory + disk JSON cache with TTL and async persist under `config/zenith/cache/<namespace>/`.
  - ItemDatabaseCache — NEU items.json (id→name/tier/npc-sell/stats).
  - BINCache — concurrent map of skyblock id → lowest BIN price.
  - BazaarCache — product id → (buyPrice, sellPrice, buyVolume, sellVolume, updatedMs).
  - RecipeCache — output id → list of Recipe {outputId, outputCount, ingredients[], type}.
- **NEU client** (`api/neu/`):
  - NEURepoClient — raw.githubusercontent.com fetch, rate-limited.
  - NEUItemFetcher — async items.json load → ItemDatabaseCache.
  - NEURecipeFetcher — async recipes.json parse → RecipeCache.
  - NEUConstantsFetcher — constants.json load into JsonObject for later phases.
- **Moulberry client** (`api/moulberry/`):
  - MoulberryClient — lowestbin.json GET.
  - LowestBINFetcher — start() schedules every 5 minutes; populates BINCache.
- **Coflnet client** (`api/coflnet/`):
  - CoflnetClient — base URL, path routing, rate-limit bucket selection.
  - CoflnetBazaarAPI — /api/v1/bazaar polled every 45 s → BazaarCache.
  - CoflnetAuctionAPI — active auctions lookup by item id; updates BINCache on cheapest BIN.
  - CoflnetPriceHistoryAPI — 7-day price history (avg/min/max/volume).
  - CoflnetMayorAPI — /api/v1/timer polled every 60 s; fires MayorChangeEvent on change.
  - CoflnetFlipAPI — /api/v1/flips polled every 3 s (Phase 9 will consume).
- **Scrapers** (`api/scraper/`) — ActionBar/Chat/Scoreboard/TabList/AuctionGUI/BazaarGUI/CollectionGUI/InventoryGUIScraper placeholders registered on the event bus; macro phases fill in parsing.
- **Wiki** (`api/wiki/`) — WikiDataManager boot stub + 14 Wiki* table stubs (Collections/Dungeons/Enchantments/Events/FarmDesigns/Locations/Minions/Mobs/NPCLocations/Pets/Recipes/ShopPrices/SkillTables/Slayers), lazy-loaded from NEU constants by later phases.
- **Update checker** (`api/update/UpdateChecker` + `UpdateConfig`) — GitHub releases/latest every 6 h, posts toast when a new tag is found.
- FailsafeCmd now accepts `test <TYPE>` (added in Phase 7, documented here).
- Brain view extended to show failsafe state (added in Phase 7, documented here).
- SafetyPanel (HUD) live — bottom-left dot shows severity colour with label.

### Changed
- BitsSpendBlocker gained atomic global setBlocked/clear for the failsafe manager.
- HudManager registers SafetyPanel by default (bottom-left).
- Phase banner updated to Phase 8.

## [1.0.0] - Phase 9 — Flipping Engine

### Added
- **FlipEngine** (`flipping/FlipEngine.java`) master controller. Drives MarketScanner, Bazaar/NPC/Craft/AH craft sub-engines, OrderManager, BreakScheduler, AuctionHouseInteractor, BudgetManager, ProfitTracker every tick.
- **FlipCandidate** + **FlipType** (AH_BIN, AH_BID, BAZAAR_SPREAD, CRAFT, NPC_RESELL, BOOK_APPLY, MUSEUM).
- **Tax** (`flipping/tax/`): TaxTier (GENERAL 1% / ACCESSORY 3% / BAZAAR buy 5% / sell 1.25%), TaxCalculator with `netFromBin`, `netFromBid`, `bazaarInstantBuyCost`, `bazaarInstantSellReceived`, `listPriceForNet` (inverse calc for undercutting), 400c listing fee + 50k cap, TaxBadge cache.
- **Profit** (`flipping/profit/`): FlipRecord (persisted), ProfitCalculator with volume→confidence/hold-time model, ProfitTracker session stats + FlipCompleteEvent/BigFlipCompleteEvent posting.
- **Scanner** (`flipping/scanner/`):
  - PriceHistory rolling window with mean/median/std-dev/σ-outlier detection.
  - SpreadCalculator evaluates BIN vs median after tax.
  - VolumeAnalyzer exponential-decay smoothing of Coflnet volumes; updates ProfitCalculator.
  - ItemSelector with cooldown, blacklist, filter gate.
  - MarketScanner polls Coflnet active auctions every 4 s, runs through SpreadCalculator, enqueues profitable FlipCandidates by descending profit. Uses NEU ItemDatabaseCache for display names.
  - MuseumDemandAnalyzer stub for donation-cap boosts.
- **Order** (`flipping/order/`): OrderState lifecycle (PROPOSED→QUEUED_TO_BUY→NAVIGATING→BUYING→HOLDING→LISTED→COLLECTING→COMPLETED/FAILED), OrderManager with cap on open orders, timeout on stale BUYING/NAVIGATING, failAll on failsafe; UndercutDetector; FillMonitor stub; RelistStrategy (0.5% undercut, floors/caps, never below break-even+floor).
- **Filter** (`flipping/filter/`): ItemFilter (price range, min daily volume, deny list, cosmetic/soulbound/dungeon/reforge exclusion), FilterPreset presets (lowball, high-volume, big-ticket), ItemFilterManager registry.
- **AH** (`flipping/ah/`): AuctionHouseInteractor state-machine stub (Phase 10 fills click path); BINListingStrategy; BINvsBidCalculator (bid-confidence by time remaining); AHSalesTracker (last-64 profit window + success rate); MarketTimingAdvisor (EU/US peak multiplier); AHListingOptimizer; AHCraftFlipEngine placeholder.
- **Bazaar** (`flipping/bazaar/BazaarFlipEngine.java`) scans 70+ hot products each minute, computes instant-buy→instant-sell after 5%/1.25% fees, enqueues FlipCandidate.BAZAAR_SPREAD when spread exceeds min profit/ROI; enforces 50k volume floor.
- **Craft** (`flipping/craft/`): RecipeDatabase computes ingredient cost (min BIN or Bazaar buy); CraftFlipEngine scans ~10 enchanted-material / compactors / potato-book recipes for craft→BIN profit; CraftingInteractor stub; RecipeValidator.
- **NPC** (`flipping/npc/`): NPCDatabase hardcoded seed prices; NPCFlipEngine compares NPC buy/sell vs Bazaar/BIN; NPCNavigator stub.
- **Break** (`flipping/break_/`): BreakScheduler (Gaussian-jittered ~45 min on / ~5 min off AFK breaks), IdleBehavior (slow legit-profile lookarounds while paused), BreakConfig, PostBreakActionQueue (purse refresh + BIN re-fetch).
- **Recovery** (`flipping/recovery/`): LimboDetector/LobbyNavigator/LimboRecoveryEngine/NavigationRecovery/RecoveryConfig/WorldStateRecovery stubs — all pause orders and trigger failsafes.
- **Waypoint** (`flipping/waypoint/`): WaypointType enum, Waypoint record, WaypointRegistry with AH/bazaar/bank hub BlockPos seeds.
- **Budget** (`flipping/budget/`): BudgetConfig with max-per-flip, bank reserve, session target, loss ceiling, max-open-orders; BudgetManager tracks purse via PurseChangeEvent + inventory scan, exposes canBuy(), session net.
- **FlipperConfig** (enabled flags, min-profit coin/percent, max coins per flip, candidates per tick, auto-buy/list, hot-items seed list).
- **FlipDebugData** snapshot for Brain View.
- **FlipCmd** (.z flip start|stop|status|profit|reset)
- **GuiEngine** Brain view extended with a Flipper block (running, active/listed/holding orders, session profit, scans, queue, BIN/bazaar counts, break status). Panel widened to 400×290.
- **ClientTickDispatcher** now also calls FlipEngine.tick() after FailsafeManager.tick().
- **Phase banner** updated to Phase 9.

### Design
- Master rules enforced: §1 slot lookups (future click paths go through GUISlotFinder), §2 eyes only via ZenithEyes, §3 movement via ZenithPath (none in Phase 9 — navigation is stubbed), §4 no chat to server, §5 Gaussian jitter in BreakScheduler, §6 state machines for orders/breaks/AH interaction, §7 events (FailsafeTriggerEvent, BreakStart/End, PurseChange), §8 FlipEngine/FlipperConfig persisted, §9 keybind-agnostic hotkey, §10 no hardcoded research — prices come from Moulberry/Coflnet/NEU only.
- All network I/O runs on ioPool; tick thread only polls queues and state.
- OrderManager refuses new orders when failsafes are active, over budget, or over loss ceiling.

## [1.0.0] - Phase 10 — Dashboard GUI + Command completion

### Added
- **ZenithScreenWrapper** — bridges ZenithScreen (pure component tree) to vanilla Screen for click/render/input/close, background dimming, ESC close, isPauseScreen=false.
- **DashboardScreen** — the primary control surface opened by `.z dashboard` or `OPEN_DASHBOARD` keybind. Tabs: Home, Flipper, Failsafe, Macros, Settings.
  - Home: version/phase display, Resume Failsafe, EMERGENCY STOP, Close.
  - Flipper: Start/Stop buttons + toggles (AH BIN, Bazaar spreads, Craft flips, NPC flips, Scheduled breaks).
  - Failsafe: Clear & Resume, Disconnect, Sound/Toast alert toggles.
  - Macros: placeholder panel (farming/mining/combat/fishing/foraging phases fill this in).
  - Settings: HUD visibility, Brain View, chat prefix, Open HUD Editor, Reset Layout.
  - Status footer showing failsafe state + flipper profit.
- **ZenithScreenWrapper.open/close/isOpen** for callers.
- **ZenithToggle** component — small clickable toggle switch with onToggle callback (used in dashboard panels).
- **ZenithTabBar** component — animated underline tabs used by the dashboard.
- **Command tab completion** via MixinCommandSuggestions:
  - Intercepts `CommandSuggestions.updateCommandInfo` when input starts with `.z`.
  - Provides root-command suggestions, then delegates to `Command.suggest(argv)` per command.
  - FailsafeCmd.suggest: subcommands (status/resume/list/test/clear); test suggests FailsafeType names.
  - FlipCmd.suggest: subcommands (start/stop/status/profit/stats/reset).
- **Command** interface gained `default List<String> suggest(argv)` method (default empty).
- **CommandCompleter** splits input respecting trailing-space, filters prefix-insensitive, prepends ".z ".
- **MixinChatScreen** placeholder target for future chat UI extensions.
- **DashboardCmd** now actually opens the DashboardScreen instead of returning a stub message.
- OPEN_DASHBOARD keybind wired in FailsafeManager to toggle dashboard open/closed.

### Changed
- Removed duplicate FlipCmd register in CommandManager (was registered twice).
- ZenithClient banner updated to Phase 10.
- Command system is now explicitly secondary — the dashboard GUI is the primary control surface, per design direction.

### Design
- Dot commands never reach the server; completion is entirely client-side via mixin on CommandSuggestions.
- No custom Fabric channels; brigadier is not extended (would send brand packets / register server-side commands).
- All toggles in the dashboard mutate live config fields (persistence wiring for those specific settings lands when settings panels are expanded in Phase 13).
