# Flipping Engine

Zenith's flipper combines Bazaar spreads, AH BIN sniping, NPC arbitrage, craft
flips, and AH-craft relisting into a single state-machine-driven loop. The
engine is conservative by default — rate-limited (Coflnet: 2 req/s; Moulberry:
1/10 s), budget-aware, failsafe-gated — and inserts human-looking scheduled
breaks.

The subsystem build order below is the master sequence (per user spec):

1. Market scanner + API clients
2. Tax calculator
3. Item selector + Budget manager
4. Bazaar interactor (GUI automation)
5. Order manager
6. Profit tracker + History
7. Craft flip engine
8. NPC flip engine
9. AH craft flip engine
10. Break scheduler
11. Mayor system

## Architecture

```
FlipEngine (ticked by ClientTickDispatcher)
 ├─ [1] MarketScanner         polls Coflnet (3 s), Moulberry (5 min),
 │                            BazaarAPI (45 s), MayorAPI (60 s)
 ├─ [2] TaxCalculator         AH (1/2/8%), Bazaar (5% buy / 1.25% sell),
 │                            listing fees, craft rounding
 ├─ [3] ItemFilter + Budget   presets (lowball / high-volume / big-ticket),
 │                            purse+bank+inventory worth, loss ceiling
 ├─ [4] BazaarInteractor      GUI click state machine (create buy/sell
 │    + AuctionHouseExecutor  orders, claim, collect) — Phase 10
 │    + AuctionHouseGUI       (AH) and Phase 10 follow-up (Bazaar)
 ├─ [5] OrderManager          lifecycle queue: PROPOSED → BUYING → HOLDING →
 │                            LISTING → LISTED → SOLD/EXPIRED/FAILED
 ├─ [6] ProfitTracker         session/rolling profit, P/H, flip history,
 │    + AHSalesTracker        BigFlip notifications, 64-sale success window
 ├─ [7] CraftFlipEngine       RecipeDatabase → BIN sell price, profit vs mat cost
 ├─ [8] NPCFlipEngine         NPCDatabase seed → BIN/Bazaar arbitrage
 ├─ [9] AHCraftFlipEngine     buy raw mats on AH → craft → relist on AH
 ├─ [10] BreakScheduler       ~45/5 min Gaussian breaks + IdleBehavior
 │     + IdleBehavior         (slow camera lookarounds while paused)
 └─ [11] MayorSystem          Derpy/Jerry/Diana/etc. → price modifiers,
                              macro advisories, perk-aware flips
```

## Data flow

1. **Price sources (MarketScanner + API clients — #1):**
   - Moulberry `lowestbin.json` (every 5 min) → `BINCache`
   - Coflnet `/api/v1/bazaar` (every 45 s) → `BazaarCache`
   - Coflnet `/api/v1/auctions` + `/api/v1/flip` (3 s poll) → candidate stream
   - NEU `items.json` / `recipes.json` → `ItemDatabaseCache` / `RecipeCache`
   - Coflnet `/api/v1/mayor` (every 60 s) → `MayorSystem`
2. **Tax (#2)** applied before any profit decision so candidates don't look
   profitable on paper but lose money after AH/Bazaar fees.
3. **ItemFilter + Budget (#3)** reject items outside the active preset or beyond
   `maxCoinsPerFlip` / `bankReserve` / `sessionLossCeiling`.
4. **Orders (#5)** enqueue PROPOSED candidates keyed by expected profit.
5. **GUI interactors (#4)** — `BazaarInteractor` and `AuctionHouseExecutor` —
   drive clicks through `GUIClickExecutor` + `GUISlotFinder` (master rule §1),
   with humanised `DelayManager` gaps (rule §5).
6. **ProfitTracker + History (#6)** record successes/failures and fire
   `FlipCompleteEvent` / `BigFlipCompleteEvent` for the HUD and Discord.
7. **Craft / NPC / AH-craft engines (#7–#9)** run on slower cadences (2 min) and
   enqueue their own candidates back through the same OrderManager.
8. **BreakScheduler (#10)** pauses new orders on schedule; `MayorSystem` (#11)
   toggles multipliers/blacklists (e.g. Derpy 50% mob coins → disable NPC flips;
   Diana → suspend mining, enable hunting).

## Budget & risk controls

- `maxCoinsPerFlip` — individual trade cap (default 5 M).
- `bankReserve` — floor below which no new orders are placed (default 1 M).
- `sessionLossCeiling` — halts trading if session profit drops below −500 k.
- `maxOpenOrders` — caps concurrent listed holdings (default 6).
- `minProfitCoins` / `minProfitPercent` — double threshold to reject thin margins.
- Break scheduler inserts 5-min pauses every ~45 min (Gaussian 40–55 min).
- Failsafe activation immediately fails all orders.
- Mayor perk modifiers dynamically flip filters on/off.

## GUI interactors (Phase 10)

- **AuctionHouseExecutor** — drives the AH browser/sign-search/confirm/create
  flow; page enum covers BROWSER / SEARCH_SIGN / RESULTS / CONFIRM_BUY / MANAGE /
  COLLECT / CREATE / CHOOSE_ITEM. All slot lookups via `AuctionHouseGUI` using
  `GUIItemMatcher.nameContains/.loreContains` (rule §1).
- **BazaarInteractor** (to come) — opens `/bz`, selects buy/sell order, fills
  price & quantity via sign handler, confirms, collects fills. Uses the same
  `SignInputHandler` / `GUIClickExecutor` / `DelayManager` primitives.

## Commands

- `.z flip start` — initialise & start the engine.
- `.z flip stop` — halt (cancels pending buy orders; active listings remain).
- `.z flip status` — running state, orders, budget, queue.
- `.z flip profit` — session profit / flips / active orders.
- `.z flip reset` — zero session counters.

## HUD integration

See `docs/HUD.md` (when filled) for the macro/flipper HUD tracker panels
(Session Profit, P/H, itemised loot, skill XP/hr, BPS/Crops per min, uptime,
Yaw/Pitch debug, Jacob Contest timer, etc.) — panel layout mirrors the
three reference screenshots provided by the user (farming stats, Ghost Profit
Tracker, Pumpkin inventory-worth / Skills / Jacob's Contest).
