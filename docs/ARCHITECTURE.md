# Zenith Client — Architecture

## Overview

Zenith Client is a Fabric mod for Minecraft 26.1.2 that automates Hypixel SkyBlock
gameplay. It is organised as a set of layered subsystems communicating primarily
through an internal event bus, with engines providing humanized primitives
(rotation, movement, clicks) on top of which macros and the Autopilot meta-AI run.

```
┌──────────────────────────────────────────────────────────┐
│  AUTOPILOT (meta-AI: analysis, decision, schedule)       │
├──────────────────────────────────────────────────────────┤
│  MACROS (~151)  │  FLIPPING (4 engines)  │  DISCORD/RPC  │
├──────────────────────────────────────────────────────────┤
│  CORE ENGINES                                            │
│  ZenithEyes (rotation) │ ZenithPath (movement)           │
│  ZenithRenderer        │ InputEngine (clicks/keys)       │
├──────────────────────────────────────────────────────────┤
│  CORE FRAMEWORK                                          │
│  Modules · StateMachines · EventBus · GUI Interaction    │
│  World/Parsers · Failsafe · Mayor · Banking · Config     │
├──────────────────────────────────────────────────────────┤
│  MINECRAFT 26.1.2 / FABRIC API (12 surgical mixins)     │
└──────────────────────────────────────────────────────────┘
```

## Core Design Principles

1. **Undetectability First** — every action passes through humanization layers.
2. **Fully Autonomous** — Autopilot plays the game on a human schedule.
3. **Modular** — ~151 macros, any of which can run independently.
4. **Zero Server Fingerprint** — no custom channels, no brand leak, dot-commands intercepted.
5. **Failsafe-First** — staff-check detection precedes every action.
6. **Persist Everything** — all settings/state/profiles survive restarts via JSON.

## System Initialization Order

See Part 4 §5.1 of the master document. The authoritative order is:

1. `ConfigManager.load()`
2. `LanguageManager.init()`
3. `ZenithRenderer.init()`
4. `AnimationEngine.init()`
5. `ZenithEventBus.init()`
6. `KeybindManager.register()`
7. `CommandManager.register()`
8. `ModuleManager.registerAll()`
9. `ZenithEyes.init()`
10. `ZenithPath.init()`
11. `FailsafeManager.init()`
12. `MayorSystem.init()`
13. `BankingManager.init()`
14. `DiscordManager.init()`
15. `HudManager.init()`
16. `MacroManager.init()`
17. `AutopilotManager.init()`
18. `PINLockManager.checkLock()`

## Threading Model

- **Main (Minecraft tick) thread** — all macro logic, rotation/movement application,
  GUI interaction, failsafe checks.
- **Render thread** — HUD/debug overlay rendering, world-space visualizers. Read-only
  access to engine state via volatile fields.
- **Background threads** — HTTP API calls (Coflnet/NEU/Moulberry), Discord webhooks,
  screenshot encoding, file I/O. Must dispatch MC interactions back via
  `ThreadUtils.runOnMainThread()`.

## Event Flow

Systems communicate through `ZenithEventBus.publish(ZenithEvent)`. Example:

```
EntityScanner.tick()
  -> publish(PlayerNearbyEvent)
      -> FailsafeManager     (HIGH)   — evaluate staff-check risk
      -> FailsafeSoundPlayer (HIGH)   — play alert
      -> DiscordManager      (NORMAL) — queue webhook + screenshot
      -> HudManager          (NORMAL) — update SafetyPanel
      -> ZenithChat          (NORMAL) — client-side warning
      -> DebugLogger         (LOW)    — append to log
```
