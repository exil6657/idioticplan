# DevData — passive harvester + autonomous developer tour macro

The DevData subsystem exists so we don't have to manually visit every menu,
warp, boss, sign, and chat pattern to fill in the `[RESEARCH NEEDED]` gaps
across the codebase. It has two pieces:

1. **`DevDataHarvester`** (`devdata/DevDataHarvester.java`) — a passive
   listener that subscribes to `InventoryOpenEvent`, `InventoryCloseEvent`,
   `ChatReceivedEvent`, `WorldChangeEvent`, `ClientTickEvent`, and polls MC
   state for scoreboard / action-bar / boss-bar / tab-header / entities /
   sign screens every 2.5 s. Every observation is deduplicated and flushed
   to a single self-updating markdown file on disk.
2. **`DevDataMacro`** (`macro/dev/DevDataMacro.java`) — a `MacroModule`
   that drives an autonomous tour of SkyBlock warps + `/ah` + `/bz` so the
   passive harvester collects all the layout/title/chat data it needs
   without a human at the keyboard. Start it from the dashboard Settings
   tab ("Run dev data tour") or with `.z devdata tour`.

## Output

```
<gameDir>/zenith/devdata/OBSERVATIONS.md
```

* Loom dev run: `run/zenith/devdata/OBSERVATIONS.md`
* Production install: `.minecraft/zenith/devdata/OBSERVATIONS.md`

The folder is auto-created on first launch. The file is **rewritten on
every flush** (every 30 s, plus on GUI open/close, world change, and
`.z devdata flush`). Sections:

1. **Summary** — unique counts per category.
2. **Worlds & Locations Visited** — every world-id from `WorldChangeEvent`.
3. **Sign Prompts** — full 4-line dump of every `SignEditScreen` (AH price
   signs, Bazaar quantity/price, warp signs, etc.).
4. **GUI Layouts** — the most useful section. Slot-by-slot table
   (slot | item | skyblockId | count | ench | first 3 lore lines) for
   every unique container title seen, plus on-screen button labels.
   Deduplicated by `title|slots|rows|content-fingerprint`.
5. **Scoreboard Lines** — sidebar objective + team prefix/suffix entries
   + score lines.
6. **Action Bar Lines** — chat type-id 2 messages (HP/defense/mana/
   ability cooldowns, etc.) with § codes stripped.
7. **Boss Bar Titles** — every boss-bar name seen (dragons, demons, etc.).
8. **Tab-List Header/Footer** — useful for location banners, active mayor,
   Jacob contest timer, event alerts.
9. **Chat Patterns** — normalised (numbers → `<NUM>`, UUIDs → `<UUID>`,
   hex → `<HEX>`) so repeated messages collapse to one entry. Feeds
   `ChatPatternEngine` regex design.
10. **Entity Types Seen** — every registry id rendered in the client's
    entity list while the harvester is active.
11. **Manual Notes** — timestamped entries added via `.z devdata note`.

## How to use the autonomous tour

1. Launch the client and log into SkyBlock. Stand somewhere safe (Hub
   spawn, private island, or an empty lobby) with no inventory open.
2. The harvester is **on by default** in dev workspaces; verify with
   `.z devdata status`.
3. Start the tour: **Settings → "Run dev data tour"** or `.z devdata tour`.
4. Don't touch the keyboard/mouse while it runs. It will:
   1. `/is` (record island scoreboard/GUI).
   2. `/hub` (record hub scoreboard).
   3. `/warp barn` → `/warp park` → `/warp deep` → `/warp gold`
      → `/warp spider` → `/warp end` → `/warp mines` → `/warp da`
      → `/warp museum` → `/warp wizard` → `/warp crypt`, recording
      scoreboard / action bar / boss bar / entities at each stop.
   4. `/ah` — snapshots Auction Browser, clicks "Manage Auctions" to
      capture that page (so R041 BIN Create price-gold-bar slot can be
      identified), backs out, clicks the search sign item to capture
      the sign screen title (R042-adjacent), backs out.
   5. `/bz` — snapshots Bazaar catalog, opens the first non-top-bar
      category (Farming, usually), opens the first product whose lore
      indicates "Buy Instantly"/"Sell Instantly", captures the product
      layout (so we get exact slot positions for all four order buttons,
      filling R042), closes.
   6. Returns to wherever it started (no automatic warp home).
5. After the tour ends (roughly 1–2 minutes depending on warp lag) run
   `.z devdata flush` and copy findings into `docs/RESEARCH.md`. Stop
   the tour early with `.z devdata stop-tour` or the EMERGENCY STOP
   button.

## Passive (non-tour) use

Even without running the tour the harvester logs anything you open
manually — useful while building/testing new GUI interactors (bank,
anvil, enchanting, calendar, skills, pets, wardrobe, storage, ender
chest, etc.). Just toggle "Record dev data" on the Settings tab and play
normally; `OBSERVATIONS.md` accumulates data.

## Commands

| Command | Effect |
|---|---|
| `.z devdata status` | Show enabled flag, counts, session uptime, output path. |
| `.z devdata on` / `off` | Toggle recording. |
| `.z devdata flush` | Force an immediate write to disk. |
| `.z devdata snapshot` | One-shot snapshot of the current GUI/scoreboard + flush. |
| `.z devdata note <text>` | Append a timestamped manual annotation. |
| `.z devdata tour` | Stop any running macro and start the DevData tour. |
| `.z devdata stop-tour` | Stop the tour early. |
| `.z devdata path` | Print the output file path. |

## Design constraints

* **Zero server visibility beyond ordinary player actions** — the tour
  sends the same `/warp`, `/ah`, `/bz` commands a player would press,
  opens/closes menus normally, and never buys/sells/lists anything.
* **Deduplicated** — opening the same page 400 times writes one entry,
  bumping a `seen xN` counter.
* **Crash-safe** — flushes every 30 s and on every GUI/world transition;
  manual notes drain from a concurrent queue each tick.
* **Non-blocking I/O** — one buffered `Files.writeString` per flush,
  typically <100 KB / <2 ms on the main thread.
* **Master rule compliant** — no hardcoded slots (uses `GUISlotFinder`/
  `GUIItemMatcher` for AH Manage/Search clicks and BZ category/product
  clicks), no `Thread.sleep` (all waits via `DelayManager.isReady`),
  state-machine driven, event-bus subscribers, British English docs.

## Extending the tour

Add new stops by extending `DevDataMacro.Step`:

1. Add the enum constant plus a `_WAIT` companion for the post-command
   settling delay.
2. Add the entry in `onTick()`: send the command/click, call
   `DelayManager.reset(DELAY_TAG, …)`, then `advance(WAIT_STEP)`.
3. In the wait state check `DelayManager.isReady(DELAY_TAG)`, call
   `DevDataHarvester.getInstance().snapshotCurrentGui()` (or any extra
   captures), then `advance(next)`.
4. Wire the previous step's `nextWarp(...)` (or follow an existing
   pattern) to point at your new step.

When filling in later-phases GUI interactors (bank, anvil, enchanting,
calendar, skills, etc.) add a step that opens the menu via its
navigation command/NPC, waits, then closes it — the passive harvester
will record the full slot layout for the corresponding `*GUI.java`
finder to consume.
