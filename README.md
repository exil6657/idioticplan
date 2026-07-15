# Zenith Client

Advanced Hypixel SkyBlock macro client for Minecraft 26.1.2 (Fabric).

**Developer:** Exil
**Version:** 1.0.0 (Phase 2 — core framework complete)
**Target:** Minecraft 26.1.2 / Fabric Loader 0.18.9+ / Fabric API 0.145.4+26.1.2
**Language:** Java 21 (bytecode target), Java 25 (Gradle JVM)
**Distribution:** Private; release builds ProGuard-obfuscated.

## Phase 2 Status — COMPLETE

Core framework is wired: event bus, module system with 8 setting types,
state machine with 8 mandatory states, client-side chat (never sent to
server), 12 utility classes, humanised timer/delay system, player-keybind
reader (no hard-coded W=forward), Zenith keybind registry with conflict
checking, dot-command dispatcher (`.z`) with 22 commands, bits-spend
protection, and a Gson-backed 12-file config system with migration chain.

Module toggling, key handling, and command interception are live APIs;
concrete implementations (macros, GUI, engines, failsafes) are filled in
during Phases 3–17.

## Phase 1 Status — COMPLETE

Build system research (R001) is resolved. The project compiles as a Fabric
mod for Minecraft 26.1.2 using Loom 1.15's new **unobfuscated mode** (26.1 is
the first unobfuscated MC release — no Yarn mappings, no `remapJar`, Mojang
official names used directly). See [SETUP.md](SETUP.md) for developer setup.

**Phase 1 deliverables:**
- `build.gradle` configured for Loom 1.15 / Gradle 9.4.0 / Java 25 JVM / Java 21 target
- JDA 5.2.1 and Gson 2.10.1 declared as jar-in-jar dependencies
- `ZenithClient.java` entrypoint prints a version banner on load
- `ZenithClientInfo.java` constants (MOD_ID, VERSION, MC_VERSION, feature flags)
- 12 surgical mixin stubs (compile, no logic yet) + 3 accessor interfaces
- `fabric.mod.json` and `zenithclient.mixins.json` valid for 26.1.2
- ProGuard rules and obfuscation dictionary present (Phase 20 use)
- Full ~1,900-file skeleton in place per master doc Part 1
- `docs/RESEARCH.md` updated with R001 findings

**Next:** Phase 2 — Core Framework (Module system, State Machines, Event Bus,
Keybinds, Commands, Chat, Utilities, Config, Bits protection).

## Repository Layout

```
build.gradle              Loom 1.15 build (unobfuscated mode, MC 26.1.2)
settings.gradle
gradle.properties        Version constants for all dependencies
gradlew / gradlew.bat    Gradle 9.4 wrapper scripts
proguard-rules.pro       Release obfuscation rules (Phase 20)
proguard-dictionary.txt  Obfuscation word list
SETUP.md                 Developer setup guide
LICENSE                  Private distribution license
src/main/java/com/zenith/client/
    ZenithClient.java     Fabric entrypoint (banner only in Phase 1)
    ZenithClientInfo.java Mod metadata/constants
    mixin/               12 surgical mixins + 3 accessors
    engine/              ZenithEyes, ZenithPath, ZenithRenderer, InputEngine
    core/                Module system, state machines, event bus, GUI interaction
    failsafe/            Staff-check detection & human reaction engine
    autopilot/           Meta-AI (account analysis, decision engine, scheduling)
    flipping/            4 flip engines (Bazaar, Craft, NPC, AH-craft)
    macro/               ~151 macros (farming/mining/combat/foraging/fishing/
                         events/hunting/rift/dungeon/kuudra/misc)
    gui/                 Dashboard, HUD, HUD editor, themes, components
    discord/             Webhook, bot (JDA), screenshot, RPC, remote control
    api/                 Coflnet, Moulberry (lowest BIN), NEU repo, wiki, cache
    config/ keybind/ command/ qol/ render/ camera/ session/ security/ stats/ world/ debug/ mayor/ economy/ antidetection/
src/main/resources/
    fabric.mod.json      Mod metadata (entrypoints, dependencies)
    zenithclient.mixins.json
    pack.mcmeta
    assets/zenithclient/  Lang (15 files), textures, icons, sounds
config/ cache/ data/ logs/ docs/
```

## Building

```bash
# Requires JDK 25 for the Gradle JVM.
./gradlew build        # produces build/libs/zenith-client-1.0.0.jar
./gradlew runClient    # launches the Fabric dev client
```

## 10 Critical Implementation Rules

1. **No hardcoded slot indices** — `GUISlotFinder.find*` by name/lore always.
2. **No instant rotations** — `ZenithEyes.requestRotation(...)` only.
3. **No direct movement** — `ZenithPath.requestPath(...)` or `MovementSimulator`.
4. **No server-visible branding** — zero custom packet channels; brand suppressed.
5. **No fixed delays** — `DelayManager.humanDelay(...)` with Gaussian variance.
6. **State machines for everything** — every multi-step flow is a `StateMachine`.
7. **Events for cross-system communication** — `ZenithEventBus.publish(...)`.
8. **Config persists everything** — all state/settings written to JSON.
9. **Never assume keybinds** — read the player's actual bindings at runtime.
10. **Research before implementing** — classes marked `[RESEARCH NEEDED]` wait.

## Roadmap (20 phases, ~63 weeks)

| Phase | Content | Est. |
|-------|---------|------|
| 1 | Build system & skeleton | **Week 1 — DONE** |
| 2 | Core framework (modules, events, state machines, keybinds, commands) | **Wk 2-4 — DONE** |
| 3 | ZenithEyes & ZenithPath engines | Wk 5-8 |
| 3 | ZenithEyes & ZenithPath engines | Wk 5-8 |
| 4 | ZenithRenderer & GUI foundation | Wk 9-11 |
| 5 | HUD system & Brain View debug | Wk 12-13 |
| 6 | World interaction & GUI parsing | Wk 14-16 |
| 7 | Failsafe system | Wk 17 |
| 8 | API & data layer (Coflnet/NEU/Moulberry/wiki) | Wk 18-19 |
| 9 | Bazaar/AH/NPC/Craft flipping | Wk 20-22 |
| 10 | Farming macros | Wk 23-26 |
| 11 | Mining macros | Wk 27-30 |
| 12 | Combat macros + all 6 slayers | Wk 31-34 |
| 13 | Foraging & fishing | Wk 35-37 |
| 14 | Events & hunting | Wk 38-40 |
| 15 | Rift suite | Wk 41-43 |
| 16 | Museum & misc macros | Wk 44-46 |
| 17 | Autopilot (full AI) | Wk 47-52 |
| 18 | QOL, render, camera modules | Wk 53-55 |
| 19 | Dungeons & Kuudra | Wk 56-60 |
| 20 | Discord, polish, ProGuard release | Wk 61-63 |
