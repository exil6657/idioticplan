# Flipping Engine — Phase 9

Zenith's flipper combines AH BIN sniping, Bazaar spreads, NPC arbitrage, and
craft flips into a single state-machine-driven loop. The engine is conservative
by default — rate-limited (Coflnet: 2 req/s; Moulberry: 1/10 s), budget-aware,
and failsafe-gated — and inserts human-looking scheduled breaks.

## Architecture

```
FlipEngine (ticked by ClientTickDispatcher)
 ├─ MarketScanner        polls Coflnet every 4 s, runs SpreadCalculator
 ├─ BazaarFlipEngine     scans BazaarCache every 50 s for instant spreads
 ├─ NPCFlipEngine        compares NPC sell prices to market every 2 min
 ├─ CraftFlipEngine      RecipeDatabase → BIN sell price every 2 min
 ├─ AHCraftFlipEngine    stub (Phase 12 crafting macros)
 ├─ OrderManager         lifecycle queue (PROPOSED → ... → COMPLETED/FAILED)
 ├─ AuctionHouseInteractor  GUI click state-machine stub (Phase 10 fills)
 ├─ UndercutDetector     relist when someone undercuts us
 ├─ FillMonitor          detects sold listings from AH GUI
 ├─ RelistStrategy       0.5% undercut w/ break-even floor
 ├─ BreakScheduler       ~45/5 min AFK breaks (Gaussian jitter)
 ├─ IdleBehavior         slow camera lookarounds during breaks
 ├─ BudgetManager        purse + reserve gating
 ├─ ItemFilter/Manager   presets (lowball, high-volume, big-ticket)
 ├─ ProfitTracker        session stats, Flip/BigFlip events
 └─ AHSalesTracker       rolling 64-sale window, success rate
```

## Data flow

1. **Price sources:**
   - Moulberry `lowestbin.json` (every 5 min) → `BINCache`
   - Coflnet `/api/v1/bazaar` (every 45 s) → `BazaarCache`
   - NEU `items.json` / `recipes.json` → `ItemDatabaseCache` / `RecipeCache`
2. **Scanners** read from these caches (no per-tick HTTP) and enqueue `FlipCandidate`s
   into a priority queue keyed by expected profit.
3. **FlipEngine.tick()** consumes up to 2 candidates per tick, runs them through
   the active `ItemFilter` and `BudgetManager.canBuy`, then creates orders.
4. **AuctionHouseInteractor** (Phase 10) walks the AH GUI to buy/relist; Phase 9
   just tracks order state.
5. **ProfitTracker** records successes/failures and fires `FlipCompleteEvent` /
   `BigFlipCompleteEvent` for the HUD and Discord integration.

## Budget & risk controls

- `maxCoinsPerFlip` — individual trade cap (default 5 M).
- `bankReserve` — floor below which no new orders are placed (default 1 M).
- `sessionLossCeiling` — halts trading if session profit drops below −500 k.
- `maxOpenOrders` — caps concurrent listed holdings (default 6).
- `minProfitCoins` / `minProfitPercent` — double threshold to reject thin margins.
- Break scheduler inserts 5-min pauses every ~45 min of play.
- Failsafe activation immediately fails all orders.

## Commands

- `.z flip start` — initialise & start the engine.
- `.z flip stop` — halt (cancels pending buy orders; active listings remain).
- `.z flip status` — running state, orders, budget, queue.
- `.z flip profit` — session profit / flips / active orders.
- `.z flip reset` — zero session counters.

## Open items (later phases)

- AuctionHouseInteractor GUI click wiring (Phase 10).
- Bazaar buy/sell order placement GUI wiring (Phase 10).
- Book-apply flips (Phase 12 enchant macros).
- Museum demand weighting.
- Bid sniping (AH_BID strategy with end-time-priority).
- Real-time Coflnet websocket (vs current 3 s poll).
