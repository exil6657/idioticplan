# CHANGELOG

All notable changes to the Zenith Client are tracked here. The format is
loosely based on Keep a Changelog; phases correspond to the 20-phase master
roadmap.

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
