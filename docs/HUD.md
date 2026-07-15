# HUD Tracker Panels

Zenith's in-game HUD is a set of draggable, theme-aware panels (see `HudPanel`
+ `HudEditor`). The user provided three screenshots of **other clients'** HUDs
as design REFERENCES for features to include — they are NOT a literal spec to
reproduce panel-for-panel:

| Reference | Style elements borrowed |
|-----------|--------------------------|
| Farming panel (watermelon icon) | Header with **current-activity icon** (dynamic — swaps to match the active macro: 🍉 for watermelon/melon, 🎃 for pumpkin, 🥔 for potato, ⛏ for mining, 🎣 for fishing, etc.), activity counter, rates (crops/min, blocks/s, XP/h, coins/h with source label), skill level + progress bar, Yaw/Pitch debug lines at bottom. |
| Ghost Profit Tracker (combat) | Itemised drop list with counts × coin value, right-aligned coloured coin column, Kills/Combo/XP Gained/Avg Magic Find footer, Session Profit / Profit Per Hour / Total Uptime always visible. |
| Pumpkin inventory / skills / Jacob's Contest | Session block with average BPS and counter (e.g. Pests), Inventory Worth (NPC) top-items breakdown, Skills block (Roman + numeric level + progress %), Jacob's Contest block that only appears while a contest is active. |

## Panel suite

| Panel id | Purpose | Populated by |
|----------|---------|--------------|
| `FarmingStatsPanel` | Active-macro stats header; icon swaps to whatever the user is macroing. Counters / rates / XP/h / skill bar / Yaw·Pitch. | Farming macro (Phase 13) + skill tracker + ZenithEyes debug. |
| `MiningStatsPanel` | Mining-specific rates (blocks/s, powder/h, gemstones/h, mining XP/h, Yaw/Pitch, route progress). | Mining macro (Phase 14). |
| `CombatStatsPanel` (GhostProfitPanel) | Drop list, Kills/Combo/XP/MF, Session Profit, P/H, Uptime. Works for ghosts, endermen, dragons, etc. | Combat macro (Phase 15). |
| `FishingStatsPanel` | Catches/h, sea creature kills, loot list, XP/h, bobber Yaw/Pitch. | Fishing macro (Phase 16). |
| `SessionPanel` | Inventory Worth (NPC) top stacks + purse, Earned/h, Total profit, active skill level/progress, Jacob Contest block (farming only). | Cross-macro session stats + Jacob tracker. |
| `ProfitPanel` | Compact single-line profit summary (flips + macros combined). | ProfitTracker. |
| `SafetyPanel` | Failsafe dot + label (green/amber/orange/red). | FailsafeManager. |
| `ActiveFlipsPanel` | Currently listed/buying flips (AH + Bazaar). | FlipEngine. |

## Farming / active-macro panel layout (dynamic icon)

```
-{icon} {Activity} ────────────────────────────
  {icon} {Counter label}:  {value}
        {next-milestone text}
  {coin icon} Coins/h:     {value} ({source})
  {icon} Blocks/s:         {value}
  {lantern icon} {Skill} Level:
        ▮▮▮▮▮▮▮▮▯▯▯ {percent}%
  {xp icon} {Skill} XP/h:  {value}
  Yaw:   {yaw}
  Pitch: {pitch}
```

- The `{icon}` next to the title swaps to match the current macro (🍉 melon,
  🎃 pumpkin, 🥔 potato, 🥕 carrot, 🌾 wheat, 🍄 mushroom, 🍫 cocoa, 🌵 cactus,
  ⛏ mining, 🎣 fishing, 🗡 combat, etc.) — NOT hard-coded to watermelon.
- `{source}` on the Coins/h line shows where the calculation is coming from
  (Bazaar sell, NPC sell, AH BIN, etc.).
- Yaw/Pitch debug lines at the bottom are orange and always shown (debug aid
  for rotation tuning).
- Skill bar is drawn with 20 filled/empty block chars, width-relative to the
  panel.

## Combat / profit panel layout

```
{title} (e.g. "Ghost Profit Tracker")
  {count}×  {drop name}         {value colour coded}
  ...
  Kills: {kills}
  Kills Since {rare} Drop: {n}
  Max Kill Combo: {combo}
  {skill} XP Gained: {xp}
  Average Magic Find: {mf}
  Bestiary Kills: {n or MAX}
  Session Profit: {n} coins
  Profit Per Hour: {n} coins
  Total Uptime: {h}h{m}m{s}s
```

- Drops are sorted by value desc; coin column is right-aligned with colour
  (white ≤ 1M, green > 1M, gold > 10M, red > 100M).
- Count column uses thousands separators.
- Footer always shows Session Profit, P/H, Uptime.

## Session panel layout

```
{Macro name} ({uptime})
{Skill family}
Average BPS: {bps}
Pests: {n}              (farming only; hidden for other skills)
────────────────────────
Inventory Worth ({source})
  {top items...}
  👛 Purse: ${n}
Inventory Worth: ${n}
Earned Per Hour: ${n}
Total Profit:    ${n}
────────────────────────
Skills
  {Skill} Level: {roman} ({numeric})
  Progress:      {percent}%
────────────────────────
Jacob's Contest  (only while a contest is active)
  {medal}
  Ends in {mm:ss}
  Total Score: {n}
  Total 'til next rank: {n}
```

## Data providers

| Panel              | Provider               | Refresh    |
|--------------------|------------------------|------------|
| Session Profit     | ProfitTracker          | 1 s        |
| Drop list          | DropTracker (loot bus) | per event  |
| Coins/XP per hour  | RollingRateTracker     | 1 s        |
| Uptime             | MacroSession           | tick       |
| BPS / Crops/min    | BlockBreakTracker      | 200 ms EMA |
| Skill level/prog   | SkillAPI / scoreboard  | 2 s        |
| Yaw/Pitch          | ZenithEyes.debugData() | tick       |
| Jacob's Contest    | JacobContestTracker    | chat event |
| Inventory worth    | InventoryValuer        | 1 s        |
| Pests / counter    | ChatPatternEngine      | chat event |
| Magic Find         | BuffTracker            | 1 s        |

## Rules

- Master rule §1 (no hard-coded slot indices) applies to reading GUI state for
  panels (e.g. Jacob's Contest preview) just as it does to clicks.
- Number formatting uses comma thousands separators (UK locale) to match the
  reference screenshots.
- Panels use `Theme.current().textPrimary/.textSecondary/.accent` for the
  bulk of colours; gold `0xFFAA00` for earnings, panel-specific gold/orange
  for the Yaw/Pitch debug lines.
- Panel positions persist in `hud_layout.json` (already registered).
- All panels can be toggled via `.z hud toggle <name>` and from the
  Dashboard HUD tab.
- Macro panels are generic — the same `CombatStatsPanel` is reused for
  ghosts, endermen, dragons, etc., by changing the title string and
  registering a drop-list provider. No per-boss hard-coded panels.
