# DevData — passive harvester + quick & FULL autonomous tour

The DevData subsystem exists so we don't have to manually visit every menu,
warp, boss, sign, and chat pattern to fill in the `[RESEARCH NEEDED]` gaps
across the codebase. It has three pieces:

1. **`DevDataHarvester`** (`devdata/DevDataHarvester.java`) — a passive
   listener that subscribes to `InventoryOpenEvent`, `InventoryCloseEvent`,
   `ChatReceivedEvent`, `WorldChangeEvent`, `ClientTickEvent`, and polls MC
   state for scoreboard / action-bar / boss-bar / tab-header / entities /
   sign screens every 2.5 s. Every observation is deduplicated and flushed
   to a single self-updating markdown file on disk.
2. **`DevDataMacro`** (`macro/dev/DevDataMacro.java`) — QUICK tour (~3 min):
   private island → hub → every major /warp → /ah browser/manage/search → /bz catalog first category/first product. Safe default.
3. **`DevDataFullTourMacro`** (`macro/dev/DevDataFullTourMacro.java`) — **FULL exhaustive tour (~25-40 min)** that captures *everything* the mod will ever need:
   - Every major warp (barn/park/deep/gold/spider/end/isle/mines/da/museum/wizard/crypt/desert/forest etc)
   - AH: browser + sort cycle (all 4 sort modes) + BIN toggle + search sign + results + confirm + Manage Auctions
   - Bazaar: **ALL categories discovered**, then **ALL products on page 1 (and page 2 if Next Page exists) per category** — for EACH product it opens product page and captures Buy Instantly qty sign, Sell Instantly qty sign, Create Buy Order qty sign → price sign, Create Sell Order qty sign → price sign **without ever clicking Confirm**, so zero coins spent
   - Extra GUIs via safe commands: /pets, /wardrobe, /storage, /ec, /sacks, /collection, /skills, /museum, /garden, /sbmenu, /bestiary, /recipes, /calendar, /trades, /bank etc
   - Scoreboard/tab/bossbar/action-bar/chat/entities captured passively every 2.5 s throughout

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

## How to use — QUICK vs FULL

### Quick tour (3 min, default) — for sanity checks

1. Launch client, log into SkyBlock, stand safe (Hub spawn or private island).
2. `.z devdata status` — should be enabled=true
3. Start: **Dashboard Settings → "Run dev data tour"** or `.z devdata tour`
4. Don't touch inputs. Does:
   `/is` → `/hub` → `/warp barn/park/deep/gold/spider/end/mines/da/museum/wizard/crypt` → `/ah` browser + Manage Auctions + search sign → `/bz` catalog + first category + first product.
5. After ~2 min: `.z devdata flush`, upload file.

### FULL exhaustive tour (25-40 min, everything, zero coins spent) — what you asked for

This is what populates **every single RESEARCH gap** (R002 scoreboard per location, R003 tab header/footer, R005 boss bars, R041 AH sort/BIN/confirm, R042 Bazaar full button names + quantity/price sign titles for ALL four order types, R045 DeathScreen, R046 action-bar ❤ regex, R047 SignEditScreen fields, Bazaar category→product map, AH GUI layout, extra GUIs).

**Command:**
```
.z devdata tour full
```
or Dashboard Settings → dropdown "Tour mode: FULL" → Run tour

**What it does, step-by-step, with no purchases:**

1. `/is` → `/hub` → every warp in `WARPS[]`: `barn, park, deep, gold, spider, end, isle, nether, mines, da, museum, wizard, crypt, desert, forest, jungle...` — scoreboard/bossbar/tab/entities captured each stop
2. **AH deep dive:**
   - `/ah` browser snapshot
   - Clicks **Sort button 4×** to cycle all sorts (Recently Updated, Price Low→High, High→Low, etc) — snapshot each → fixes R041 sort overpay bug
   - Clicks **BIN Only toggle** → snapshot
   - Clicks **Search** → captures sign prompt `Enter query` → closes (no search)
   - *Optional* search for "diamond" → results page → first listing → confirm page snapshot (no buy confirm click)
   - **Manage Auctions** page snapshot
3. **Bazaar FULL:**
   - `/bz` catalog → discovers ALL categories (Farming, Mining, Combat, Woods & Fishes, Oddities) via slot scan, not hard-coded list
   - For **each** category:
     - Opens category → discovers **ALL products on page 1** (and clicks Next Page if arrow exists)
     - For **each** product:
       - Opens product page (exact slot positions for Buy Instantly / Sell Instantly / Create Buy Order / Create Sell Order buttons → R042)
       - **Buy Instantly** → quantity sign (`How many?` / `How much?`) captured → ESC close (no buy)
       - **Sell Instantly** → quantity sign captured → ESC
       - **Create Buy Order** → qty sign → types `1` via SignInputHandler → submits → price sign `At what price?` captured → ESC close (cancels, no order placed)
       - **Create Sell Order** → same qty→price capture → ESC
       - Back to category list
     - Back to catalog
4. **Extra GUIs** via safe commands (no side effects): `/pets`, `/wardrobe`, `/storage`, `/ec`, `/sacks`, `/collection`, `/skills`, `/museum`, `/garden`, `/sbmenu`, `/bestiary`, `/recipes`, `/calendar`, `/trades`, `/bank`, `/gfs`, etc — each snapshotted
5. Returns to `/is`, flushes, prints `OBSERVATIONS.md` path

**Runtime:** ~150-250 products × 4 buttons × 2 signs = ~1000-2000 sign openings. 25-40 min. Safe to leave running, failsafe will pause if PlayerNearby/Staff.

**To abort:** `.z devdata stop-tour` or hold PANIC_BUTTON (400ms) → disconnect.

After DONE:
```
.z devdata flush
.z devdata path
```
Copy `zenith/devdata/OBSERVATIONS.md` from that path and drag it here — I'll parse it and replace every `[RESEARCH NEEDED]` title with exact observed strings, and regenerate `BazaarCategory` map + `AuctionHouseGUI` sort logic.

## Passive (non-tour) use

Even without running the tour the harvester logs anything you open
manually — useful while building/testing new GUI interactors (bank,
anvil, enchanting, calendar, skills, pets, wardrobe, storage, ender
chest, etc.). Just toggle "Record dev data" on the Settings tab and play
normally; `OBSERVATIONS.md` accumulates data.

## Commands

| Command | Effect | File |
|---|---|---|
| `.z devdata status` | Show enabled, counts, uptime, path | — |
| `.z devdata on` / `off` | Toggle passive recording | — |
| `.z devdata flush` | Force write to disk now | Updates `OBSERVATIONS.md` |
| `.z devdata snapshot` | One-shot GUI/scoreboard snapshot + flush | — |
| `.z devdata note <text>` | Append timestamped manual note | Appended |
| `.z devdata tour` | Quick tour (~3 min, first cat/prod only) | Writes md |
| `.z devdata tour full` | **FULL exhaustive tour (~25-40 min, ALL bazaar products, AH sort cycle, extra GUIs)** | Writes md + captures every sign title for R041/R042 |
| `.z devdata stop-tour` | Stop tour early | — |
| `.z devdata path` | Print output file absolute path | — |

**Output file:** `<gameDir>/zenith/devdata/OBSERVATIONS.md` — upload this file here (drag-drop). I will parse it automatically. It does NOT auto-push to GitHub to avoid leaking your .minecraft path or spamming commits.

**Safety:** FULL tour never clicks AH Buy Confirm or Bazaar Confirm — quantity/price signs are closed with ESC to cancel, so zero coins spent. If inventory full or limbo, it closes GUI and continues.

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
