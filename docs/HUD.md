# HUD Tracker Panels

Zenith's in-game HUD is a set of draggable, theme-aware panels (see
`HudPanel` + `HudEditor`). The macro and flipper tracker panels mirror the
three reference screenshots provided by the user:

---

## 1. Farming / skill stats panel (watermelon-style)

```
-Farming ──────────────────────────────────────────────────
  🍉  Cultivating:   5,301,761
      158,507,073 until ranked!
  🍉  Crops/min:     73,480
  🧈  Coins/h:       216,905,600 (Bazaar)
  🍉  Blocks/s:      19.8
  🏮  Farming Level:
      ▮▮▮▮▮▮▮▮▯▯▯▯▯▯▯▯▯▯▯▯ 4.14%
  🥬  Farming XP/h:  798,336
  Yaw:   89.98
  Pitch: -58.98
```

- Source data: `FarmingMacroStats`, `CounterSession`, `XPTracker`,
  `ZenithEyes` rotation debug.
- `Cultivating` counter reads the player's SkyBlock stat (chat + scoreboard),
  with "until ranked!" gap to next medal threshold.
- `Blocks/s` is instantaneous + smoothed (EMA 5 s).
- `Coins/h` pulls from `ProfitTracker` (can show "(Bazaar)" or "(NPC)" source).
- XP bar is 20-char width, filled by (lvlProgress %).
- Yaw/Pitch appended at the bottom in orange to mirror the user's reference.

## 2. Ghost / combat profit panel

```
Ghost Profit Tracker
  98×    Sorrow                   26.2M
  66×    Plasma                   16.5M
  4,869× Dropped Coins             9M
  480×   Ghost Shard              6.4M
  13,280×♻ Flawed Sapphire Gem    4.9M
  ...
  2×     Ghostly Boots           248k
  Kills: 0
  Ghosts Since Sorrow: 0
  Max Kill Combo: 3,497
  Combat XP Gained: 4,421,594
  Average Magic Find: 439.4
  Bestiary Kills: MAX
  Session Profit: 81,965,994 coins
  Profit Per Hour: 54,706,666 coins
  Total Uptime: 1h 29m 53s
```

- Generic "itemised drop list" panel — works for any macro (ghosts,
  enderman, dragon, mining powder, etc.).
- Drops sorted by value desc; right-aligned coloured coin column.
- Drop counts with thousands separators.
- Footer always shows `Session Profit`, `Profit Per Hour`, `Total Uptime`.
- `Kills / Combo / XP Gained / Avg Magic Find` fed from combat tracker.

## 3. Session / inventory / skill / contest panel (pumpkin-style)

```
Pumpkin (2h12m)
Farming
Average BPS: 19,66
Pests: 0
────────────────────────────────────
Inventory Worth (NPC)
  🟨 Polished Pumpkin:           $13M
  🟫 Enchanted Pumpkin:         $5.9M
  🍄 Enchanted Red Mushroom:    $4.6M
  🎃 Squash:                    $4.4M
  ⚜  Others:                    $13M
  👛 Purse:                     $16M
Inventory Worth: $37M
Earned Per Hour: $15M
Total Profit:    $53M
────────────────────────────────────
Skills
  Farming Level: LII (52)
  Progress:      100,00%
────────────────────────────────────
Jacob's Contest
  🥈 Silver
  Ends in 10:54
  Total Score: 53k
  Total 'til next rank: 44k
```

- Header shows current macro/crop + uptime.
- `Inventory Worth (NPC)` block lists top-N item stacks in inventory by NPC
  value, plus Purse line.
- `Skills` block shows level (Roman + numeric) + progress %.
- `Jacob's Contest` block appears only when a contest is active
  (`JacobContestTracker`).

---

## Data providers

| Panel             | Provider               | Refresh    |
|-------------------|------------------------|------------|
| Session Profit    | ProfitTracker          | 1 s        |
| Drop list         | DropTracker (loot bus) | per event  |
| Coins/XP per hour | RollingRateTracker     | 1 s        |
| Uptime            | MacroSession           | tick       |
| BPS / Crops/min   | BlockBreakTracker      | 200 ms EMA |
| Skill level/prog  | SkillAPI / scoreboard  | 2 s        |
| Yaw/Pitch         | ZenithEyes.debugData() | tick       |
| Jacob's Contest   | JacobContestTracker    | chat event |
| Inventory worth   | InventoryValuer        | 1 s        |
| Pests             | ChatPatternEngine      | chat event |
| Magic Find        | BuffTracker            | 1 s        |

## Rules

- Master rule §1 (no hardcoded slot indices) applies to *reading* GUI state
  for panels (e.g. Jacob's Contest preview) just as it does to clicks.
- Number formatting uses comma thousands separators (UK locale) to match the
  reference screenshots, not dots.
- Panels use `Theme.current().textPrimary/.textSecondary/.accent` — no
  hardcoded colours except gold for "earnings" lines (0xFFAA00) and the
  progress-bar fill colour (accent).
- Panel positions persist in `hud_layout.json` (already registered).
- All panels can be toggled on/off via `.z hud toggle <name>` and the
  Dashboard HUD tab.
