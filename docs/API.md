# Zenith API / Data layer — Phase 8

## Overview

Zenith talks to three public third-party APIs to stay in sync with SkyBlock
prices, items, recipes, mayor, and bazaar data:

1. **NEU Repo** (`raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates`) —
   items.json, recipes.json, constants.json. Long TTL (6 hours). Provides the
   canonical item-id → name/tier/npc-sell price/recipe map.
2. **Moulberry** (`moulberry.codes/lowestbin.json`) — simple id → lowest BIN
   price map. Polled every 5 minutes.
3. **Coflnet** (`api.coflnet.com`) — live bazaar (45 s), active auctions,
   7-day price history, mayor/timer (60 s), flip feed (3 s poll).

All HTTP goes through `api/HttpClient.java`, a thin JDK-11 `java.net.http`
wrapper that uses the shared `ThreadUtils.ioPool()` and an 8-second timeout.
No OkHttp/Apache dependency is pulled in.

## Rate limiting

A simple token-bucket `RateLimiter` is configured by `RateLimitConfig` at
startup:

| Bucket           | Rate            |
|------------------|-----------------|
| `coflnet.flips`  | 1 / 1.5 s       |
| `coflnet.prices` | 2 / s           |
| `neu.repo`       | 1 / 60 s        |
| `neu.items`      | 1 / 60 s        |
| `moulberry.bin`  | 1 / 10 s        |
| `update.check`   | 1 / 3600 s      |

`tryAcquire(bucket)` returns immediately; callers either back off (returning
null / empty list) or schedule a retry later. We never block the render/tick
thread on a rate limit.

## Caches

All caches live under `config/zenith/cache/<namespace>/` as JSON and are
pre-seeded from disk on startup (best effort; network failures don't prevent
boot). The in-memory maps are `ConcurrentHashMap`/`ConcurrentSkipListMap` and
may be read from any thread.

| Cache             | Namespace  | TTL       | Populated by           |
|-------------------|------------|-----------|------------------------|
| ItemDatabaseCache | items      | 6 h       | NEUItemFetcher         |
| RecipeCache       | (in-mem)   | N/A       | NEURecipeFetcher       |
| BINCache          | (in-mem)   | N/A (refreshed every 5 min) | Moulberry + Coflnet auctions |
| BazaarCache       | (in-mem)   | N/A (refreshed every 45 s)  | CoflnetBazaarAPI       |

Scrapers (`api/scraper/*`) listen to chat/scoreboard/tab/GUI events and are
intentionally lightweight — they just register on the bus in Phase 8 so later
phases can subscribe to their data.

## Update checker

`UpdateChecker.start()` schedules a periodic poll of
`api.github.com/repos/exil6657/idioticplan/releases/latest` every 6 h. If the
latest tag differs from `ZenithClientInfo.VERSION`, it posts a chat toast.
Failures are silently ignored (no stacktrace spam for offline play).

## Rules

- **No blocking on the main thread** — every HTTP call returns a
  `CompletableFuture` via the I/O pool; only cache reads and `isAvailable()`-
  style queries happen on the tick thread.
- **No Hypixel API key required** — we never call api.hypixel.net; everything
  comes from third-party public endpoints or in-game data.
- **No automatic chat messages to servers** — APIs are read-only; we never
  send anything to third-party endpoints except a standard User-Agent header.
- **Graceful degradation** — if all endpoints are unreachable, the mod still
  boots; BIN/bazaar/item lookups simply return empty/unknown.
