# Zenith Client — Continuation Prompt

Use this prompt if this chat session ends. Merge PR #1 first so the codebase
contains all Phase 10 work, then paste this entire file into a fresh chat
(mentioning the repo is checked out at `/home/user/idioticplan` on branch
`arena/019f56d8-idioticplan`).

---

## Paste below this line

You are continuing development of **Zenith Client** — a Hypixel SkyBlock
macro/automation Fabric mod for **Minecraft 26.1.2, Java 21 bytecode** (Gradle
JVM is Java 25 because Loom 1.15 requires it). Repo is at
`/home/user/idioticplan`, branch `arena/019f56d8-idioticplan` (do NOT switch
branches), remote `https://github.com/exil6657/idioticplan.git`. Use
`./gradlew build` (in the dev environment; this sandbox has no JDK 25 so
don't try to compile here). The `gradle/wrapper/gradle-wrapper.jar` is a
0-byte placeholder bootstrapped on first `./gradlew` invocation.

The user (British English, technical, precise) has been reviewing each phase
carefully. Communicate concisely in en_GB.

### Master implementation rules (DO NOT violate)

1. **No hard-coded GUI slot indices** — every GUI click uses
   `GUISlotFinder` + `GUIItemMatcher` (`nameContains`, `loreContains`,
   `bySkyblockId`, `byName`, `byNameContains`).
2. **No instantaneous rotations** — only `ZenithEyes.requestRotation(...)`
   using the **builder API** (`.yaw(...)`/`.pitch(...)`/`.priority(Priority)`/
   `.durationMs(...)`/`.profile(String id)`/`.tag(...)`/`.tracking(bool)`/
   `.preemptible(bool)`/`.onComplete(cb)`). There is NO
   `.profileId(...)` setter and no `.profileId(String)` field accessor from
   outside — use `.profile(String id)` on the builder. Priority enum is
   `RotationRequest.Priority.{BACKGROUND,WANDER,MACRO,COMBAT,MACRO_TICK,FAILSAFE}`.
3. **No direct movement** — only `ZenithPath.requestPath(PathRequest)` and
   `MovementSimulator`. `PathRequest.builder().from(x,y,z).to(x,y,z)
   .mode(PathMode).allowEtherwarp(bool).allowSprint(bool).stopDistance(double)
   .maxComputeMs(long).tag(String).build()` — `from()`/`to()` take three
   doubles, NOT a Vec3.
4. **Zero server-visible branding** — brand is spoofed to "vanilla", dot
   commands (`.z …`) are intercepted client-side via `MixinClientConnection`,
   **never** register Brigadier commands (use `MixinCommandSuggestions` for
   tab-completion), NO custom plugin channels.
5. **No `Thread.sleep`/fixed delays** — use `DelayManager` with Gaussian
   humanised delays (via `isReady(tag)` / `resetHumanised(tag,minMs,maxMs)`).
6. **State machines for everything** — each macro/interactor has explicit
   states and enter/tick/exit transitions.
7. **Events via ZenithEventBus** — `@SubscribeEvent` from
   `com.zenith.client.core.event.annotation.SubscribeEvent`; optional
   `priority=EventPriority.NORMAL/HIGHEST/etc`.
8. **Config persists via Gson JSON** — 13 config files registered
   (zenith/budget/debug/filter/history/hud_layout/lock/npc/player/routes/session/waypoints/failsafe).
9. **`PlayerKeybindReader` reads Options at runtime** — never hardcode key
   codes.
10. **Research before implementing; mark unknowns `[RESEARCH NEEDED]`** and
    record notes in `docs/RESEARCH.md`.

**Mixins**: exactly the surgical mixins listed in
`src/main/resources/zenithclient.mixins.json` (12 mixins + 3 accessors).
Use Mojang names, `require=0` on risky injections. Existing mixins:
`MixinMinecraft, MixinClientConnection, MixinGameRenderer, MixinMouse,
MixinKeyboardHandler, MixinGui, MixinScreen, MixinLevelRenderer,
MixinEntityRenderer, MixinOptions, MixinCamera, MixinPlayerRenderer,
MixinChatScreen, MixinCommandSuggestions` and accessors
`MouseAccessor, OptionsAccessor, CameraAccessor`.

Distribution is private, ProGuard-obfuscated (`proguard-rules.pro`), NO
Hypixel API key, dot-prefix `.z` commands.

### Version / API cheatsheet (verified working in prior phases)

- MC 26.1.2, Fabric Loader 0.18.9, FAPI 0.145.4+26.1.2, Loom 1.15 (plain
  `implementation`, no mappings jar, no remapJar), Gradle 9.4.
- `ClientboundSystemChatPacket`, `ClientboundPlayerChatPacket` (body via
  `p.body().content()`), `ServerboundChatPacket(String, Instant, long, byte[],
  int, BitSet)` for unsigned chat, `ServerboundChatCommandPacket(String cmd,
  Instant timeStamp)`.
- `handleInventoryMouseClick(containerId, slotId, button, ClickType, player)`.
- `DisplaySlot.SIDEBAR` for scoreboard.
- `BlockState.isSolid/.canOcclude/.blocksMotion`, `ClipContext/BlockHitResult`.
- `GuiGraphics.fill/drawString/fillGradient`, `Component.literal(...)`.
- `Minecraft.tick` fires `ClientTickEvent`; `renderLevel(FJZLnet/minecraft/client/Camera;)V`;
  `render(Lnet/minecraft/client/gui/GuiGraphics;F)V`; mouse `onMove(JDD)V` /
  `onPress(JIII)V`; keyboard `keyPress(JIIII)V`.
- `Connection.send(Packet)` has two overloads (second takes
  `GenericFutureListener`); `channelRead0` has `require=0`.
- `InputEngine.keys()` returns `KeySimulator` (methods: `halt()`,
  `setForward/Back/Left/Right/Jump/Sneak/Sprint/Use/Attack(boolean)`, and
  boolean getters — no `leftClick()`/`leftClickHold()` methods exist; use
  `setAttack(true/false)`).
- `ZenithEyes.setEnabled(boolean)` disables + clears queue; camera sync comes
  from `MixinCamera`.
- `ZenithPath.stop()`, `requestPath(PathRequest)` (builder described above).
- `RotationDebugData`/`PathDebugData` methods:
  `.state()/.activeTag()/.profileId()/.currentYaw()/.currentPitch()/.targetYaw()
  /.targetPitch()/.angularVelocityDegPerSec()/.overshooting()/.correcting()
  /.hesitating()/.fatigue()/.queued()/.priority()` for rotations;
  `.state()/.mode()/.currentSpeedBps()/.stuck()/.plannedNodes()/.executedNodes()
  /.totalCost()/.distanceToTarget()/.reaction()/.computeMs()` for paths.
- `BitsSpendBlocker` is all-static:
  `preClick(featureId, costBits, shiftHeld)`, `setBlocked(boolean)`,
  `isBlocked()`.
- `ChatReceivedEvent.getMessage()/.getRawFormatted()/.getTypeId()`;
  `FailsafeTriggerEvent(name, reason, severity)` where severity 0=notify/wiggle,
  1=warp-island, 2=disconnect (mapped in FailsafeManager.onExternalTrigger).
- `ItemFilter`/`ItemDatabaseCache.getName(id)/.getNpcSell(id)/.getItem(id)/.items()`;
  `BINCache.getInstance().get(id)/.put/.size/.keySet/.ageMs`;
  `BazaarCache.getInstance().get(id)/.instantBuyPrice/.instantSellPrice`.
- `mc.isWindowActive()` exists;
  `mc.getConnection().getConnection().disconnect(Component)` exists;
  `SoundEvents.NOTE_BLOCK_PLING.value()` for Holder unwrap.
- `Command` interface: `getName()/getUsage()/getDescription()/execute(String[] args)
  /suggest(String[] argv)` (default empty); `requiresDebug()` (default false).
- `Theme` fields: `.background/.border/.panel/.accent/.textPrimary/.textSecondary`.
- `GuiDrawContext`: `fillRect(x,y,w,h,Color4f)/drawRoundedRect/drawOutline/
  drawGradient/drawString(text,x,y,color,shadow)/stringWidth(text)/graphics()`.
- `HudPanel` extends `Component`; public fields `x, y, width, height, visible,
  hovered, id, panelId, anchor (HudPanelAnchor), pinned, offsetX, offsetY`.
  Default-size via `getDefaultWidth()/getDefaultHeight()`.
- `AnimationEngine.getInstance().frame()` (NOT `.update(float)`).
- `SignEditScreen.onDone()` is reflectively invoked by `SignInputHandler` to
  submit signs; the `signText` field is a `List<Component>` set via reflection
  (line 0 = our text).
- Sign input: do NOT call `mc.keyPressHandler.keyPressed(...)` — it re-enters
  our own MixinKeyboardHandler and double-fires.
- `KeySimulator` uses `setAttack(boolean)` for left click; there is no
  `leftClick()`/`leftClickHold()`/`rightClick()` method.
- Block queries: `level.getBlockState(pos).isSolid()/.blocksMotion()/.canOcclude()/.isAir()`.

### Failsafe design — IMPORTANT, user-specified behaviour

The user corrected the failsafe design heavily. Read `docs/FAILSAFE.md` fully.
Summary:

- **Escape command is `/is` (private island), NEVER `/home`** — `/home` does
  not exist in SkyBlock. `BanActionHandler.sendIsland()` is the method;
  `sendHome()` is a deprecated alias. `/hub` is the fallback.
- **Low health triggers COMBAT reaction, NOT warp** — `CombatReactionAction`
  aims ZenithEyes at the nearest non-villager/non-armor-stand living entity
  within 6 blocks and holds attack via `keys.setAttack(true)` for up to 4 s.
  Hypixel has a custom HP system (Defense/❤/Absorption), read via
  `PlayerHealthMonitor` (which already pulls from ActionBar/scoreboard).
- **Camera snap / player nearby / wrong-item → WIGGLE_REACT** — 700-1200 ms
  smooth ±20° yaw / ±7° pitch wiggle via `ZenithEyes.requestRotation`, plus
  a rate-limited "?" in chat via `ChatQuestionMarkAction` (30 s cooldown).
  Macros keep running.
- **Blocks placed in front of a farming row → REMOVE_OBSTRUCTION** —
  `ObstructionDetector` fires when forward input + ~0 horizontal velocity +
  solid block ahead for >1.2 s. `RemoveObstructionAction` wiggles, looks at
  the block, sends "?", holds attack for up to 2.5 s, then resumes or
  repaths if the block won't break.
- **Death → INSTANT_RESPAWN** — `RespawnAction` clicks the DeathScreen
  respawn button or calls `mc.player.respawn()`; `WORLD_CHANGE/TELEPORT/
  LAGBACK/LIMBO/PLAYER_CLONE` → REPATH. `RepathReactionAction` calls
  `ZenithPath.requestPath` back to the active macro's `DestinationProvider`
  (registered by macro modules via `RepathReactionAction.setProvider(...)`).
  Supports walk/sprint/etherwarp; jump-pad / NPC / cross-server travel
  noted for Phase 11.
- **Reactive severities DO NOT PAUSE MACROS.** Only PAUSE / WARP_ISLAND /
  WARP_HUB / DISCONNECT freeze KeySimulator + ZenithPath + ZenithEyes.
- Severity ladder (in `FailsafeStrictness`):
  NONE(0), NOTIFY(1), PAUSE(2), WIGGLE_REACT(3), COMBAT(4),
  REMOVE_OBSTRUCTION(5), INSTANT_RESPAWN(6), REPATH(7), WARP_ISLAND(8),
  WARP_HUB(9), DISCONNECT(10).
- SafetyPanel dot colours: green=NONE; amber=NOTIFY/WIGGLE_REACT/COMBAT/
  REMOVE_OBSTRUCTION/INSTANT_RESPAWN/REPATH; orange=PAUSE/WARP_ISLAND/WARP_HUB
  (label shows "→ /is"); red=DISCONNECT.
- Failsafe types (20 detectors registered in FailsafeManager.init):
  PLAYER_NEARBY (WIGGLE_REACT, escalates), BAN_DETECTED (DISCONNECT),
  LIMBO (REPATH), TELEPORT (REPATH), VELOCITY_KICK (WARP_ISLAND),
  STAFF_PULL (WARP_ISLAND), ITEM_DESELECT (WIGGLE_REACT),
  ROTATION_RESET (WIGGLE_REACT), WORLD_CHANGE (REPATH),
  GUI_CLOSE (NOTIFY), WRONG_TOOL (WIGGLE_REACT), YAW_FLIP (WIGGLE_REACT),
  INVENTORY_FULL (NOTIFY), LAGBACK (REPATH), DISMOUNT (WIGGLE_REACT),
  DEATH (INSTANT_RESPAWN), LOW_HEALTH (COMBAT), LOW_HUNGER (COMBAT),
  OBSTRUCTION (REMOVE_OBSTRUCTION), CUSTOM (NOTIFY).
- TabInHandler pauses on focus loss with NO auto-resume; player must press
  resume / `.z failsafe resume`.

### The GUI dashboard is the PRIMARY control surface

Commands are secondary per explicit user direction. The dashboard is at
`gui/dashboard/DashboardScreen.java` with 5 tabs (Home, Flipper, Failsafe,
Macros, Settings), live Start/Stop/Resume/Panic/Disconnect buttons, and
toggles. `.z dashboard` opens it via `ZenithScreenWrapper.open(new
DashboardScreen())`. The OPEN_DASHBOARD keybind (in FailsafeManager keybind
wiring) toggles it open/closed.

### Flipper subsystem ordering (from user spec)

Order of implementation and wiring in `docs/FLIPPING.md` is exactly:

1. Market scanner + API clients (Coflnet, Moulberry, NEU, Mayor)
2. Tax calculator (AH 1/2/8%, Bazaar 5%/1.25%, listing fees, cap)
3. Item selector + Budget manager (presets, purse+bank+inventory, loss
   ceiling)
4. Bazaar interactor (GUI automation — TO BE BUILT) + AuctionHouseExecutor
   (BUY path built, LIST path stubbed)
5. Order manager (Order + OrderState lifecycle, pollToBuy())
6. Profit tracker + History
7. Craft flip engine
8. NPC flip engine
9. AH craft flip engine
10. Break scheduler (~45/5 min Gaussian, IdleBehavior camera wander)
11. Mayor system (Derpy/Jerry/Diana modifiers, perk-aware flips)

### Phase status (commits so far)

Phases 1–10 are on branch `arena/019f56d8-idioticplan`, pushed to origin,
PR #1 exists. Commits:
ea6988f (Phase 1 Build), 927ffee (Phase 2 Core), 235a764 (Phase 3 ZenithEyes
+ ZenithPath engines), 9669f7b (Phase 4 Mixins + GUI), 87ac8f7 (Phase 5
HUD + Brain View), d677ef3 (Phase 6 World + GUI), 8c27127 (Phase 7 Failsafe),
a22b5e3 (Phase 8 API/data), c097633 (Phase 9 Flipping),
82c4dc6 + 281f551 (Phase 10 — GUI Dashboard, dot-command tab completion, AH
foundation + failsafe overhaul/HUD corrections described above).

### What Phase 10 contains (built already)

- **Tab completion for dot commands:** `Command.suggest(argv)` default
  method, `CommandCompleter`, `MixinCommandSuggestions` (injects at HEAD of
  `CommandSuggestions.updateCommandInfo`, cancellable, `require=0` — DO NOT
  use Brigadier). `FailsafeCmd.suggest()` and `FlipCmd.suggest()` return
  subcommand lists. `MixinChatScreen` is a placeholder.
- **ZenithScreenWrapper** — bridges ZenithScreen→Screen with dark 0x55000000
  fill, ESC closes, `isPauseScreen=false`, calls `AnimationEngine.frame()`.
- **ZenithToggle**, fully-implemented **ZenithTabBar** (animated underline
  via `AnimatedValue`).
- **DashboardScreen** with 5 tabs and live buttons/toggles wired to config.
- **CommandSender** for server `/ah /is /hub /warp` (450 ms cooldown,
  `ServerboundChatCommandPacket`).
- **AuctionHouseNavigator** — sends `/ah`, title detection for
  BROWSER/Confirm/Create/Choose/Manage/Collect, `isInAH()`, `returnToBrowser()`.
- **GUIWaiter** expanded with static `waitForTitle(substr,timeout)` /
  `checkAll()` / `hasAnyPending()`.
- **GUIInteractionEngine** (was a stub, now a real tick subscriber
  registered from `ClientTickDispatcher.register()`) — drives
  `GUIParser.tick()`, `SignInputHandler.tick()`, `GUIWaiter.checkAll()`,
  `GUIClickExecutor.tick()` every client tick.
- **GUIParser** promoted to singleton with `getInstance()`;
  `AuctionHouseExecutor` no longer creates a second parser instance.
- **AuctionHouseGUI** — `Page` enum (BROWSER/SEARCH_SIGN/RESULTS/CONFIRM_BUY/
  MANAGE/COLLECT/CREATE/CHOOSE_ITEM/UNKNOWN), slot finders for Search/
  BIN-toggle/Sort/Buy-confirm/Create/Manage/Back, `findListings()` scans
  pre-player-inventory slots for auction heads, `parseCoins()` supports
  K/M/B suffixes, `parseBinPrice/parseBidPrice` lore parsing. Fixed the
  broken operator-precedence bug in detectPage.
- **GUIItemMatcher.nameContains / .loreContains** static factories (rule §1).
- **SignInputHandler** — reflectively sets `signText` List line 0 to a
  `Component.literal(value)`, then reflectively invokes `sign.onDone()` via
  `mc.execute(...)` on the next tick (no keyPress hack).
- **AuctionHouseExecutor** state machine — buy path complete:
  IDLE→OPEN_AH→TOGGLE_BIN→SEARCH→WAIT_SIGN→WAIT_RESULTS→WAIT_CONFIRM→
  WAIT_BOUGHT→IDLE (markBought + chat success). LIST flow
  (OPEN_MANAGE→CHOOSE_ITEM→CREATE→SET_PRICE→CLICK_CREATE→WAIT_LISTED) is
  stubbed for the next iteration.
- **AuctionHouseInteractor** delegates to `AuctionHouseExecutor` and exposes
  `pump(OrderManager)` so FlipEngine hands BUYING/NAVIGATING orders into it.
- **FlipEngine.tick()** calls `ah.pump(orders)` after driving the legacy
  `ah.tick()`.
- **HUD panels registered in HudManager.init():** Watermark, ModuleList,
  Safety, **FarmingStatsPanel** (dynamic icon/activity/skill via
  `setActiveMacro(icon, activity, skill)` — NOT hardcoded watermelon),
  **CombatProfitPanel** (generic, setTitle, no Ghost-specific), **SessionPanel**
  (inventory worth / skills / Jacob's contest).
- **ZenithClient banner** bumped to Phase 10; success message updated.
- docs: `docs/FLIPPING.md` reordered to user's sequence, `docs/HUD.md`
  (reference screenshots as design input), `docs/FAILSAFE.md` rewritten
  for new ladder, `docs/CHANGELOG.md` has Phase 10 + revision entries,
  `docs/HANDOFF.md` (this file).

### What is NOT done yet (next up)

1. **AuctionHouseExecutor listing flow** — OPEN_MANAGE → CHOOSE_ITEM (click
   held item or click gold block head) → CREATE → SET_PRICE (sign handler
   with BIN price formatted without commas, K/M suffix supported? Check
   Hypixel AH format — need RESEARCH) → CLICK_CREATE (confirm via
   `findCreateButton`) → WAIT_LISTED (detect return to Manage Auctions or
   browser with item gone from cursor). Add `list(Order)` method alongside
   `buy(Order)` and drive it from `OrderManager.pollToList()` after
   `markBought`.
2. **BazaarInteractor GUI state machine** (Phase 10 follow-up, per flipper
   ordering #4): `/bz`, click buy/sell order, fill price+quantity via sign,
   confirm, collect fills. Same primitives as AH (GUIParser/GUIClickExecutor/
   SignInputHandler/GUIItemMatcher/DelayManager). Need RESEARCH for exact
   Bazaar GUI titles and slot positions.
3. **Brain View panel** (`DebugBrainPanel` / `GuiEngine`) — add AH executor
   state line; widen panel if needed; already 400x290 for flipper block,
   add reaction-state to the failsafe block.
4. **Phase 11: Macro framework base** (next major phase) —
   - `MacroModule` abstract class with start/pause/resume/stop/break
     lifecycle, auto-pause on FailsafeStrictness >= PAUSE (but NOT on the
     reactive tiers WIGGLE_REACT/COMBAT/REMOVE_OBSTRUCTION/REPATH/
     INSTANT_RESPAWN), auto-resume after successful REPATH.
   - `MacroDestinationProvider` registration to `RepathReactionAction` so
     failsafe repaths go back to the macro anchor (x,y,z + description).
   - BreakScheduler integration across all macros (already exists, just
     needs per-module hook).
   - Inventory snapshot helper for "is the target item in hand" checks.
   - Cross-server pathfinding: jump-pad / NPC interaction state machine for
     travelling between hubs/islands when REPATH lands us in the wrong spot.
5. **Phase 12: Crafting macro module** — open crafting table, click recipe
   in NEU-style recipe book or arrange manually, pull from / push to
   inventory, use RecipeDatabase + RecipeCache already built in Phase 8/9.
6. **Phase 13: Farming macro** — angle-based row walking, block break
   (pumpkin/melon/cane/cocoa/wheat/mushroom/cactus/potato/carrot), pest
   detection (chat pattern), Jacob's Contest tracker (chat + scoreboard),
   FarmingStatsPanel populated with real counter/crops-per-min/blocks-s/
   farming-XP-per-hour/farming-level-progress; dynamic icon set via
   `FarmingStatsPanel.setActiveMacro(...)`.
7. **Phase 14: Mining macro** — MiningStatsPanel, route-based mining (Mithril/
   Gemstone/Nml/Glacite), powder/h rates, chest looter, comms.
8. **Phase 15: Combat macro** — GhostProfit-style CombatProfitPanel driven
   by real drop events, ghost/enderman/dragon combat (aim via ZenithEyes
   COMBAT priority), kill-aura-style left click using KeySimulator, Magic
   Find tracking, Bestiary.
9. **Phases 16–19:** fishing/foraging, events (jerry/diana/spooky), hunting,
   rift, museum, misc.
10. **Phase 20:** Discord webhook, final polish, ProGuard obfuscation.

### HUD panel next steps

The three skeletons (FarmingStatsPanel / CombatProfitPanel / SessionPanel)
currently draw header + placeholder text. Need to:

- Add data sources (`MacroSession`, `BlockBreakTracker`, `DropTracker`,
  `RollingRateTracker`, `SkillXPTracker`, `InventoryValuer`,
  `JacobContestTracker`, `BuffTracker` for Magic Find).
- Wire Yaw/Pitch from `ZenithEyes.debugData().currentYaw()/currentPitch()`.
- Render 20-char progress bars using `ctx.fillRect`.
- Right-align the coin column in CombatProfitPanel using
  `ctx.stringWidth(text)` for offset maths.
- Make panels honour `pinned` and `HudPanelAnchor` properly (current
  defaults place them at 0,0 if no layout entry; add default positions in
  `HudManager.applyLayout()` like SafetyPanel and ModuleListPanel have).
- Support `.z hud toggle <panelId>` via HudCmd (already exists; needs wiring
  to set `visible`).

### Config files

13 configs registered in ConfigManager: zenith, budget, debug, filter,
history, hud_layout, lock, npc, player, routes, session, waypoints, failsafe.
Each uses `register(filename, class, factory, setter, initial)` — the setter
for failsafe calls `FailsafeManager.getInstance().loadConfig((FailsafeConfigFile)o)`.

### Key files to know

- `ZenithClient.java` — entrypoint, init order (Config → EventBus → Keybind →
  Chat → Commands → Modules → ZenithEyes/ZenithPath/InputEngine → GuiEngine/
  HudManager → ChatPatternEngine/WorldHook → FailsafeManager → ApiManager →
  FlipEngine → ClientTickDispatcher.register → BitsProtection).
- `core/ClientTickDispatcher.java` — central tick; calls
  PlayerPositionTracker, PlayerHealthMonitor, ModuleManager.tickAll,
  InputEngine.tick, MovementLearner.tick, FailsafeManager.tick,
  FlipEngine.tick. GUIInteractionEngine.register() hooks its own tick via
  the bus.
- `flipping/FlipEngine.java` — master controller; drive order lifecycle
  (`orders.tick, fillMonitor.tick, ah.tick, ah.pump(orders), ahCraft.tick`)
  then consume candidates.
- `flipping/ah/AuctionHouseExecutor.java` — per-order AH state machine
  (buy path complete, list path TODO).
- `core/interaction/GUIParser.java` (singleton, reads container → GUIState),
  `GUIClickExecutor` (leftClick/rightClick/shiftClick with bits-block +
  DelayManager gating), `GUIItemMatcher` (rule §1), `GUISlotFinder`,
  `GUIWaiter` (title waits + checkAll), `SignInputHandler` (reflection-based),
  `CommandSender`, `skyblock/AuctionHouseGUI`/`AuctionHouseNavigator`.
- `failsafe/FailsafeManager.java` (orchestrator), `FailsafeStrictness`
  (10-level ladder), `FailsafeType` (20 types), `BanActionHandler`
  (sendIsland/sendHub/respawn/warp/disconnect), `SafetyStatusMonitor`
  (dot colour), `SafetyPanel`, `ReactionEngine` (dispatches Wiggle/Combat/
  RemoveObstruction/Respawn/Repath actions), 21 detectors in `failsafe/detection/`
  (including ObstructionDetector), reaction actions in `failsafe/reaction/actions/`.
- `gui/GuiEngine.java` — renders Brain View; the failsafe + flipper blocks
  live here.
- `gui/dashboard/DashboardScreen.java` — primary GUI.
- `gui/hud/HudManager.java` — registers panels.

### Bugs / known issues to fix when you pick up

- AuctionHouseExecutor still imports nothing broken; however
  `pendingSearch` is set from `nameForSearch(current)` which uses
  `o.candidate.itemName` — confirm `FlipCandidate` has that field
  (it does per earlier phases, but worth checking `candidate.buyPrice`
  usage — `best.price() <= current.candidate.buyPrice * 1.05`).
- SignInputHandler uses reflection for both `signText` and `onDone()`
  (searches superclass hierarchy), which should be robust across 26.x, but
  should be smoke-tested in a dev env (can't test in sandbox — no JDK 25).
- `MixinCommandSuggestions` has proper `@Shadow @Final` declarations for
  `input`, `commandUsage`, `rawCommandUsage`, `pendingSuggestions`, plus
  `@Shadow protected abstract void fillUsage(List<String>, String, boolean)`
  — verify against yarn/Mojang mappings on first compile; if the method is
  named `showSuggestions` or `customSuggestion` in 26.1, adjust.
- `KeySimulator` has `setForward/Back/Left/Right/Jump/Sneak/Sprint/Use/
  Attack(boolean)` — double-check setter names on first compile (they were
  confirmed earlier but worth verifying).
- PanicButton in FailsafeManager is wired; check that the held-400ms
  disconnect behaviour still works after ReactionEngine changes.
- ClientTickDispatcher's `register()` is called AFTER engine init but is
  itself idempotent — ensure GUIInteractionEngine's bus subscribe fires
  before the first tick.
- BazaarFlipEngine/NPCFlipEngine/CraftFlipEngine/AHCraftFlipEngine all run
  in FlipEngine.tick(); they enqueue candidates via `orders.enqueue(c)`.
  The Bazaar interactor (to actually place/fill Bazaar orders) doesn't
  exist yet — that's the Phase 10 follow-up after the AH list flow.

### Workflow

- Communicate in British English, technical and precise.
- Before writing code for a subsystem, read the existing files in that
  package first; do not duplicate classes that already exist.
- After editing, run `./gradlew build` mentally (can't run here) and fix any
  obvious issues. Commit in phase-sized commits with descriptive messages,
  push to `origin arena/019f56d8-idioticplan` only (never switch branches).
- Update the relevant docs/ markdown file (FLIPPING.md / FAILSAFE.md /
  HUD.md / CHANGELOG.md / API.md) with each phase.
- The user has provided screenshots as design REFERENCES only (farming
  panel, Ghost Profit Tracker, Pumpkin inventory/skills/Jacob panel);
  the corresponding panels are FarmingStatsPanel (dynamic icon),
  CombatProfitPanel (generic), SessionPanel. Do not literally reproduce
  crop names/values; build the panels to match the feature set.
- The user will send `.z` commands as SECONDARY; the dashboard is primary.
  Don't add Brigadier commands.

Pick up from **"What is NOT done yet"** item #1
(AuctionHouseExecutor listing flow), then #2 (Bazaar interactor), get
Phase 10 to a clean stopping point, and move into Phase 11 (macro
framework base). The goal is to get Phases 10–13 to a runnable state where
farming + AH flipping actually work end-to-end in a dev environment. Good luck.
