# Zenith Client — Mega Asset Prompt Pack

This document is the **single source of truth** for generating every visual,
audio, and localisation asset the mod needs. Copy the relevant section into
your image/speech/translation generator of choice (Midjourney, DALL·E,
Stable Diffusion, ElevenLabs, a human illustrator, etc.).

**Do NOT use procedural Pillow drawing** for final assets — everything that
ships in the release must be high-quality, hand-tuned art produced by an
advanced image generator or a pixel artist. The output of each section
should be saved to the exact file path listed under **Output file(s)**.

---

## 0. Global brand rules (apply to EVERY visual asset)

**Style direction**
- A premium, "mountain-peak at midnight" aesthetic — the word *zenith* means
  the highest point in the sky.
- **Dark-first.** UI defaults to a deep midnight-slate background. Assume
  everything sits on `#0B0E1D` unless stated otherwise.
- **Palette (Z-Palette v1) — use these exact hex codes wherever colour is
  mentioned:**

  | Role              | Hex       | Use on / note                                   |
  |-------------------|-----------|-------------------------------------------------|
  | Panel background  | `#0B0E1D` | Dashboard, HUD panels, toasts                   |
  | Elevated panel    | `#121630` | Hovered cards, expanded drawers                 |
  | Border            | `#22264A` | Subtle 1px hairline around panels               |
  | Primary text      | `#F0F4FF` | Crisp off-white (not pure `#FFFFFF` — that glares on dark BG) |
  | Secondary text    | `#9AA3C7` | Muted labels, captions, helper text             |
  | **Signature**     | `#7C52FF` | Electric violet — primary brand accent         |
  | Secondary accent  | `#22D3EE` | Cyan — used as the *secondary* dot/tick         |
  | Gold highlight    | `#F5C14B` | Summit/peak sun, profit numbers, flips          |
  | Success           | `#34D399` | Green (used sparingly — profit, connection OK)  |
  | Warning           | `#FBBF24` | Amber — caution, reaction tier                  |
  | Error / danger    | `#F87171` | Red — failsafe triggers, emergency stop         |

**Typography** (for any image that includes rendered text; in-game the mod
uses Minecraft's vanilla font, but logos/splash/illustrations use a chosen
display face):
- Display/wordmark: geometric grotesque, bold, wide tracking.
  Recommended fonts: **Montserrat ExtraBold**, **Inter Black**,
  **Poppins Black**, **Space Grotesk Bold**. All-caps "ZENITH".
- Body/caption: Inter Regular or similar neo-grotesque.
- **Never** use serif fonts, never script/handwriting, never neon/80s
  chrome. The brand feels "quietly expensive" — not flashy.

**Iconography language**
- 2 px stroke weight for any glyph at 64 px base size (scale linearly).
- Rounded stroke caps + rounded joins (2 px roundness).
- Icons sit **on** a 24×24 content box centred inside a 32×32 viewport
  for macro/category icons; 16×16 inside 24×24 for GUI widget icons.
- Icons must be readable at 16 px (100% Minecraft GUI scale). Prefer
  **silhouette + one accent colour** over detailed illustration for
  16/24 px sizes.
- No gradients on icon glyphs themselves; gradients are allowed on the
  panel backgrounds *behind* icons.

**Consistency rules**
- All assets share the same ambient lighting direction: top-left 30°,
  soft violet rim on the top-left edge of any raised element, soft
  cyan rim on the bottom-right edge.
- Drop shadows: tight, 90° straight-down, 16% opacity black, 4–8 px blur.
  Never cast coloured glows as shadows.
- Purple-to-cyan gradient is reserved for the brand mark's edge accents;
  do not reuse that gradient for random UI chrome.
- Gold is reserved for the summit sun / "peak reached" / profit
  numbers / completed flips — never use it as a generic accent.

**File format**
- All PNGs: 32-bit RGBA, 1×–4× named sizes where listed (base + @2x + @4x).
  Minecraft itself runs at whatever GUI scale the user has; ship the
  largest size and the engine scales down.
- Sounds: signed 16-bit PCM `.wav`, 44.1 kHz, stereo where noted, mono
  where noted, peak at −1 dBFS (never clip).
- Language files: strict JSON, UTF-8, LF line endings, US English
  (`en_us.json`) is the canonical source; every other locale is a
  translated copy. All keys use `snake_case` inside the
  `zenithclient.<module>.<string>` namespace.

---

## 1. Brand mark / logos

### 1.1  Wordmark logo (full)
- **Output file:** `assets/zenithclient/textures/logo/zenith_logo_full.png`
- **Sizes:** 64, 128, 256, 512 px tall (the width auto-fits) as
  `zenith_logo_full_<h>.png`; canonical file `zenith_logo_full.png` = 256 px tall.
- **Style:** horizontal lock-up.
  - Left: a geometric, faceted **"Z" whose top diagonal rises into a sharp
    mountain/summit triangle** (the literal zenith peak). A small gold
    disc (the sun) sits at the triangle apex with an 8 px diameter at
    128 px tall size, with a tiny cyan crescent tick arcing up and left
    from it (like the "new day" signal). A thin violet chevron rides the
    Z's diagonal.
  - Right of the mark: the word **"ZENITH"** in the chosen display font,
    bold, spaced +5, primary text colour. Below it in smaller violet
    caps: **"CLIENT"**, with a short violet underline terminating in a
    small cyan dot.
  - The whole lock-up sits on a transparent background (no panel) so it
    can be composited onto the splash screen / website / social.
- **Background:** transparent.
- **Do's:** sharp edges, minimal anti-alias halo, triangle must read as
  both the top of a Z and a mountain peak.
- **Don'ts:** 3D bevels, drop shadows on the wordmark, outer glow,
  Minecraft blocky font, italic, gradients inside the letters.

### 1.2  Compact logo (mark + ZENITH only)
- **Output file:** `assets/zenithclient/textures/logo/zenith_logo_compact.png`
  (+ `_64`, `_128`, `_256`, `_512` variants; canonical = 256 px tall).
- Same as 1.1 but without the "CLIENT" subtitle and underline — used for
  the title-bar button, Discord embed thumbnail, and dock icon wordmark.

### 1.3  Mark-only icon (app icon)
- **Output file:** `assets/zenithclient/textures/logo/zenith_icon_<size>.png`
- **Sizes:** 16, 64, 128, 512 px square;
  `assets/zenithclient/icon.png` = 128 px copy (used by Fabric mod menu).
- **Style:** The Z/summit glyph from §1.1, centred inside a **rounded
  square panel** (20 % corner radius) filled with the panel background
  colour `#0B0E1D`. A single 1 px hairline in `#22264A` trims the inner
  edge. A tiny violet dot at the top-left corner arc, a tiny cyan dot at
  the bottom-right corner arc (2 px diameter at 128 px). A tight soft
  drop shadow extends down 4–6 px (16% black, 4 px blur) so it reads on
  light launcher backgrounds.
- The mark itself should occupy **70 %** of the panel, perfectly centred
  (visually — the gold sun shifts the optical centre 1–2 px up).

### 1.4  Monochrome variants
- **Output files:**
  - `assets/zenithclient/textures/logo/zenith_icon_mono_white.png`
  - `assets/zenithclient/textures/logo/zenith_icon_mono_dark.png`
  - size variants `_16/_32/_64/_128/_512` for each; canonical = 128 px.
- **Style:** the same mark-only glyph, **no panel**, **no gold/cyan/violet
  accents**, filled with either solid near-white `#F0F4FF` or near-black
  `#0B0E1D`. Used for the title-bar, macOS dock monochrome mode, and
  print/embroidery.

### 1.5  Watermark (transparent wordmark for HUD)
- **Output file:** `assets/zenithclient/textures/logo/zenith_watermark.png`
- **Size:** ~240 × 80 px.
- **Style:** compact wordmark (§1.2) on a completely transparent background,
  rendered at 60 % opacity, no drop shadow. This is overlaid in the
  corner of the screen in-game — must be very subtle, readable but not
  distracting.

### 1.6  GitHub social preview
- **Output file:** `assets/zenithclient/textures/logo/zenith_social.png`
- **Size:** exactly **1280 × 640 px** (GitHub's required dimensions).
- **Composition:**
  - Background: deep midnight radial gradient, panel BG at edges to a
    slightly lifted `#121630` behind the left third where the mark sits,
    with a soft violet bloom behind the mark.
  - Left 40 %: the Z/summit glyph from §1.1, **large** (≈380 px tall),
    with the gold sun and cyan tick.
  - Right 55 %: "ZENITH" in the display font (~140 px tall, primary text),
    "CLIENT" in violet (~50 px) below it with the underline + cyan dot,
    and one short tagline below that in secondary text (~28 px):
    *"Hypixel SkyBlock automation — humanised, modular, undetectable."*
  - No panel border, no CTA button, no QR codes. Keep it clean.

### 1.7  Favicon set
- **Output files:** `zenith_favicon_16.png`, `zenith_favicon_32.png`,
  `zenith_favicon_48.png` (all in `textures/logo/`).
- The mark-only icon from §1.3 at those exact sizes. At 16 px, drop the
  gold sun's highlight dot and the cyan tick (they become noise).

---

## 2. GUI widgets (`textures/gui/`)

All widget PNGs are **9-slice compatible** — they need a 4 px transparent
border all around the outer edge so Java's `NineSlice` / vanilla widget
system can stretch them without distorting corners. Visual corner radius
should be 6 px at native 256 × GUI scale 1.

All widgets share a common spec:
- Resting state: panel BG fill `#0B0E1D`, 1 px `#22264A` border.
- Hover/active: elevated panel fill `#121630`, 1 px violet `#7C52FF` border.
- Disabled: 40 % opacity, greyed fill `#1A1E38`, border `#2A2E4F`.

| File | Description | Size | Notes |
|---|---|---|---|
| `button.png` | Default button background (9-slice) | 32 × 32 base | 6 px rounded corners, subtle vertical gradient bottom → top `#0E1228 → #141A3A` for a faint lift |
| `button_hover.png` | Hovered/pressed button | 32 × 32 | Fill `#1A1F42`, border `#7C52FF`, inner 1 px violet glow at 30 % opacity |
| `checkbox_off.png` | Unchecked tick-box | 20 × 20 | 4 px rounded square, hollow 2 px border `#22264A`, fill `#0B0E1D` |
| `checkbox_on.png` | Checked tick-box | 20 × 20 | Same as off but fill `#7C52FF`; a white tick mark (2 px stroke) inside, rounded check |
| `close_btn.png` | Window/dialog close (X) | 16 × 16 | Small circle `#1A1E38` with a white X; hover variant not needed (code recolours) |
| `minimize_btn.png` | Minimise (_) icon | 16 × 16 | Same circle style as close_btn with a horizontal bar |
| `drag_handle.png` | Panel drag dots | 24 × 8 | Six tiny 2 px dots arranged `: : :` in secondary text colour, transparent bg |
| `resize_handle.png` | Bottom-right resize grip | 16 × 16 | Three diagonal 2 px cyan dashes in the corner (classic grip), transparent bg |
| `dropdown_arrow.png` | Combo-box chevron | 12 × 8 | Downward-pointing chevron, 2 px stroke, secondary text |
| `hud_edit_overlay.png` | HUD editor border/grid tile | 32 × 32, tilable | 1 px magenta/pink dashed line at `#FF4FD8` 60 % opacity, transparent inside; used for selection outline |
| `hud_panel.png` | Default HUD panel background (9-slice) | 48 × 48 | 8 px corner radius, fill `#0B0E1D` at 88 % opacity, 1 px `#22264A` border, very subtle 4 px top-edge violet highlight at 25 % |
| `panel_bg_dark.png` | Dark dialog/card background (9-slice) | 48 × 48 | Same as hud_panel but 96 % opacity, no violet highlight |
| `panel_bg_light.png` | Light/elevated card (9-slice) | 48 × 48 | Fill `#121630` 96 %, 1 px `#2A2E55` border, 1 px top highlight white at 8 % |
| `progress_bg.png` | Progress-bar track | 32 × 12 | Capsule shape 6 px radius, fill `#1A1E38` |
| `progress_fill.png` | Progress-bar fill | 32 × 12 | Capsule 6 px radius, violet → cyan horizontal gradient `#7C52FF → #22D3EE` |
| `search_icon.png` | Search-field magnifying glass | 16 × 16 | 2 px stroke magnifier, handle angled 45° bottom-right, secondary text |
| `slider_track.png` | Slider rail | 32 × 8 | 4 px tall rounded track `#1A1E38`, aligned vertically centre |
| `slider_thumb.png` | Slider knob | 16 × 16 | Circle fill `#F0F4FF`, 1 px `#22264A` border, 4 px soft violet glow at 25 % when hovered (just bake the glow into the PNG) |
| `tab_active.png` | Active dashboard tab underline cap | 32 × 8 | 4 px tall rounded capsule filled with violet `#7C52FF`, horizontal |
| `tab_inactive.png` | Inactive tab underline | 32 × 8 | Same shape, filled with `#2A2E55` (muted) |
| `toast_bg.png` | Toast notification background (9-slice) | 64 × 32 | 8 px corner radius, fill `#121630` 96 %, 1 px `#7C52FF` left border (4 px wide violet stripe running the height), subtle glow |
| `toggle_off.png` | Toggle switch off | 40 × 20 | Capsule shape, fill `#1A1E38`, border `#2A2E55`, circle knob on left filled with `#5A6089` |
| `toggle_on.png` | Toggle switch on | 40 × 20 | Capsule filled with violet `#7C52FF`, circle knob on right filled with white `#F0F4FF`, subtle inner 1 px highlight |

---

## 3. Hotbar (`textures/hotbar/`)

### 3.1  Pill hotbar selector
- **Output file:** `pill_hotbar.png`
- **Size:** 182 × 26 px (enough to frame the vanilla 9-slot hotbar with rounded ends).
- **Style:** a thin, elongated **pill / capsule** outline (1.5 px stroke in
  `#7C52FF` 70 % opacity) that frames the selected hotbar slot. A faint
  violet inner glow (8 % opacity). No fill — the vanilla hotbar shows through.
  Used as a replacement for the vanilla blocky highlight.

(If more hotbar widgets are added later — e.g. a custom re-styled hotbar
background — append them here with the same naming convention.)

---

## 4. Category icons (`textures/icons/categories/`)

These appear on the dashboard sidebar/tabs and in the Brain View. Each
icon is a **24 × 24 glyph** centred on a **32 × 32 viewport**, transparent
background, 2 px stroke, rounded caps/joins, in secondary text colour by
default (code recolours them to violet when active). **No baked-in
backgrounds** — the dashboard panel provides the BG.

| File | Glyph description |
|---|---|
| `home.png` | Simple house outline with a small triangular roof peak aligned to suggest a summit (echoes the brand mark) |
| `flipping.png` | Two circular coins, one slightly behind the other, with a small up-arrow between them (profit) |
| `farming.png` | Stylised wheat/grass sprout — three curved blades, symmetrical, no dense detail |
| `mining.png` | Pickaxe head, facing up-right, 2 px stroke |
| `combat.png` | Crossed sword (straight blade, not Minecraft-pixelated) |
| `fishing.png` | Fishing rod tip with a curved line and a tiny hook |
| `foraging.png` | Axe head silhouette (single-bit) with a small leaf |
| `hunting.png` | Bow (simple crescent) with a drawn horizontal arrow |
| `dungeon.png` | Stone archway / doorway (top of a dungeon entrance, rectangular opening with rounded top) |
| `kuudra.png` | Stylised helmet / lava-warden mask — simple triangular eye slits, no busy detail |
| `rift.png` | Cracked/portal rift — an irregular vertical ellipse split by a zig-zag |
| `events.png` | Calendar page with a small star in the top-right corner |
| `museum.png` | Column/pillar (classical), capital + base, plain shaft |
| `failsafe.png` | Shield outline with a small exclamation mark inside |
| `settings.png` | Gear/cog, six teeth, clean, no inner hub circle (or a small 4 px hub) |
| `stats.png` | Bar chart — three vertical bars, short/medium/tall |
| `qol.png` | Checkmark inside a speech bubble |
| `discord.png` | Generic chat/voice bubble (NOT the Discord logo — avoid trademark issues) — two overlapping bubbles |
| `help.png` | Question mark inside a circle (2 px ring) |
| `history.png` | Clock face, single hand at 10 o'clock, no numbers |
| `render.png` | Eye outline (almond with a small pupil) |
| `security.png` | Padlock, closed shackle |
| `debug.png` | Wrench/screwdriver crossed, small size |
| `misc.png` | Three small dots in a horizontal ellipsis |
| `theme.png` | Half-black / half-white circle (yin-yang style but without the dots — just a vertical split) |

**Style reminder:** single colour (secondary text), 2 px stroke, rounded
caps, no fill, readable at 16 px.

---

## 5. Macro icons (`textures/icons/macro/<category>/`)

These are **32 × 32 pixel "badge" icons with a filled rounded-square
background** that sit next to each macro's name in the Macros tab and in
the Farming Stats HUD panel header (dynamic icon per macro). They replace
the old placeholder emoji glyphs.

**Spec:**
- 32 × 32 viewport; 6 px rounded square background filled with the
  elevated panel colour `#121630`, with a 1 px border `#22264A`.
- Inside the badge, a single 2 px white glyph (`#F0F4FF`) representing the
  macro, centred, readable at 20 px.
- Special accent (optional): the "currently running" macro adds a 2 px
  violet border; this is applied by code (don't bake the violet border
  into the PNG).

### 5.1  Farming (`macro/farming/`)
| File | Glyph |
|---|---|
| `wheat.png` | Wheat stalk with one grain head and two leaves |
| `carrot.png` | Carrot (orange filled shape, simplified) |
| `potato.png` | Potato (oval with 2–3 eye marks) |
| `melon.png` | Slice of melon (crescent with a rind edge) |
| `pumpkin.png` | Pumpkin with a short stem and carved-like vertical grooves but no face |
| `sugarcane.png` | Three vertical stalk segments with small leaves at joints |
| `cactus.png` | Cactus (vertical shape with two short arms) |
| `cocoa.png` | Cocoa pod (small rugby-ball shape on a stick) |
| `mushroom.png` | Small mushroom — round cap + stem |
| `wart.png` | Nether wart — three clustered wart bumps on a short stem |
| `flower.png` | Generic 5-petal flower with a small leaf |
| `pest.png` | Small bug/locust silhouette (simple) |
| `visitor.png` | Person silhouette at a door (simple head + shoulders) |
| `jacob.png` | Wheat crown / medal with a wheat motif |
| `garden_level.png` | Upward arrow over a small plot of rows |
| `greenhouse.png` | Greenhouse frame (rectangle with a peaked roof and glass panes indicated by a grid) |
| `builder.png` | Hammer and block silhouette |
| `composter.png` | Composter bin (box with a lid + rising leaf particles indicated as two dots) |
| `echo.png` | Mystic/echo gem — crystal cluster |

### 5.2  Mining (`macro/mining/`)
| File | Glyph |
|---|---|
| `cobblestone.png` | Stylised cobblestone chunk (irregular octagon with a crack) |
| `endstone.png` | Block with a speckled star/crescent mark |
| `hardstone.png` | Dense block with a pick mark/chip |
| `mithril.png` | Mithril ore — prismatic/cyan crystal embedded in stone |
| `gemstone.png` | Faceted gem (diamond-cut) in generic violet; recoloured by code per gem type |
| `obsidian.png` | Dark block with a sharp purple glint |
| `sand.png` | Wavy sand/dune lines (3 arcs) |
| `topaz.png` | Topaz amber gem (same diamond-cut, gold colour) |
| `powder.png` | Pile of powder (three small mounds) |
| `nucleus.png` | Core/atom (centre circle with three orbiting dashes) |
| `sludge.png` | Sewer/slime blob |
| `tunnel.png` | Tunnel arch (dark opening with tracks/rails) |
| `mineshaft.png` | Mine entrance with cross-brace beams |
| `forge.png` | Anvil with a spark |
| `excavator.png` | Digger/drill bit |
| `commission.png` | Clipboard with checkmark |
| `lobby_hop.png` | Portal/arrow between two doorways |

### 5.3  Combat (`macro/combat/`)
| File | Glyph |
|---|---|
| `zealot.png` | Ender-man style tall silhouette with eyes (two dots) |
| `dragon.png` | Dragon head in profile (simple horns, snout, wing-tip behind) |
| `magma_boss.png` | Magma cube with flames above |
| `blaze_slayer.png` | Blaze rod with a fiery halo (simple rods around a flame) |
| `eman_slayer.png` | Enderman/voidwalker — tall silhouette with a rift swirl |
| `rev_slayer.png` | Zombie (outstretched arm, head with X eye) |
| `sven_slayer.png` | Wolf head silhouette, fangs |
| `tara_slayer.png` | Spider silhouette (two segments, 8 legs simplified) |
| `vampire_slayer.png` | Cape + fang silhouette |
| `ghost.png` | Ghost (wavy bottom sheet + two eyes) |
| `goblin.png` | Small goblin with a dagger silhouette |
| `broodmother.png` | Spider with egg sac |
| `ashfang.png` | Wolf/ember spirit with flame crest |
| `bal.png` | Magma boss — fireball with a face (two eyes) |
| `bestiary.png` | Open book with a paw print |
| `graveyard.png` | Three crosses / tombstones in different sizes |
| `mythological.png` | Minotaur-like head with horns (simple) |
| `vanquisher.png` | Sword planted into a skull |
| `walker.png` | Zombie pigman-like silhouette (sword + pig head outline) |

### 5.4  Fishing (`macro/fishing/`)
| File | Glyph |
|---|---|
| `barn_fish.png` | Fish with a barn/rod behind it |
| `fish_hunt.png` | Fish with a hunting arrow |
| `lava_fish.png` | Fish with a flame |
| `location_fish.png` | Fish with a location pin |
| `magma_fish.png` | Lava fish with a lava blob tail |
| `quick_fish.png` | Fish with speed lines behind |
| `shark.png` | Shark fin above a wave |
| `trophy_fish.png` | Fish on a plaque / trophy pedestal |
| `worm_fish.png` | Worm/creature silhouette (serpentine) |

### 5.5  Foraging (`macro/foraging/`)
| File | Glyph |
|---|---|
| `park.png` | Tree silhouette with a bench |
| `hub_forage.png` | Tree with an axe |
| `mangrove.png` | Mangrove roots hanging down |
| `lush_lilac.png` | Lilac/flower cluster on a stem |
| `hotf.png` | Tree of life style canopies (Heart of the Mountain) |
| `berry.png` | Berry on a stem (single round with leaf) |
| `fig.png` | Fig (teardrop fruit with leaf) |
| `forage_ability.png` | Lightning/arrow up inside a leaf |
| `galatea.png` | Statue bust |

### 5.6  Hunting (`macro/hunting/`)
| File | Glyph |
|---|---|
| `crimson_hunt.png` | Sword with flames |
| `crystal_hunt.png` | Crystal shard with sparkle |
| `diving.png` | Diving helmet silhouette |
| `end_hunt.png` | End portal eye |
| `panda_cave.png` | Panda round head |
| `spider_hunt.png` | Spider with web |
| `trapper.png` | Trap/jaw snap |

### 5.7  Dungeon (`macro/dungeon/`)
| File | Glyph |
|---|---|
| `boss.png` | Skull with crown |
| `floor_grind.png` | Gear with a dungeon doorway |
| `kuudra.png` | Kuudra mask (same as categories/kuudra but on a badge) |
| `party_find.png` | Three connected person silhouettes |
| `puzzle.png` | Puzzle piece |
| `secret.png` | Chest with a question mark |
| `terminal.png` | Terminal/command prompt (cursor + brackets) |

### 5.8  Event (`macro/event/`)
| File | Glyph |
|---|---|
| `bingo.png` | Bingo card (5×5 grid with centre star) |
| `carnival.png` | Tent / balloon |
| `dark_auction.png` | Gavel silhouette |
| `fishing_festival.png` | Fish with a party hat |
| `jerry.png` | Present/gift box |
| `mining_fiesta.png` | Pickaxe with confetti |
| `new_year.png` | Firework burst |
| `spooky.png` | Pumpkin/candy bucket |
| `zoo.png` | Paw print |

### 5.9  Rift (`macro/rift/`)
| File | Glyph |
|---|---|
| `agarimoo.png` | Cow/aberration blob with eyes |
| `berberis.png` | Thorn shrub |
| `black_lagoon.png` | Lagoon pool with ripples |
| `burger.png` | Hamburger (stacked bun, patty, bun) |
| `colosseum.png` | Colosseum arches |
| `enigma_soul.png` | Soul wisp (floating flame) |
| `infested.png` | Bugs pouring from a crack |
| `leech.png` | Leech/worm with teeth |
| `living_cave.png` | Cave opening with an eye |
| `mirrorverse.png` | Mirrored Z / reflection split |
| `montezuma.png` | Aztec-style face / skull |
| `odonata.png` | Dragonfly silhouette |
| `oubliette.png` | Dungeon grate / trapdoor |
| `photon.png` | Light orb with rays |
| `rift_mob.png` | Generic twisted creature silhouette (default rift enemy) |
| `wilted_berberis.png` | Wilted/dried shrub |

### 5.10  Misc (`macro/misc/`)
| File | Glyph |
|---|---|
| `accessory.png` | Ring/band with gem |
| `alchemy.png` | Potion bottle with bubbles |
| `anti_afk.png` | Coffee cup with steam |
| `attributes.png` | Shard with spark/star |
| `carpentry.png` | Saw + plank |
| `cobble_gen.png` | Lava + water block meeting |
| `community.png` | Two connected speech bubbles |
| `cookie.png` | Booster cookie (bite taken) |
| `daily_task.png` | Sun with checklist |
| `enchant.png` | Book with sparkles |
| `essence.png` | Wisp/orb (core) |
| `experiment.png` | Flasks/beakers |
| `fairy_soul.png` | Fairy soul (small orb with wings) |
| `harp.png` | Harp strings / music note |
| `minion_collect.png` | Minion (small figure) with outstretched arm collecting an item |
| `minion_craft.png` | Minion with a hammer |
| `museum.png` | Pedestal with a spotlight |
| `pets.png` | Paw/heart tag |
| `plot_clean.png` | Broom sweeping a plot outline |
| `reforge.png` | Anvil with a shine |
| `reputation.png` | Trophy cup with a star |
| `rune.png` | Runic circle |
| `shard.png` | Crystal shard |
| `skyblock_level.png` | XP orb with an up arrow |

---

## 6. Animated / motion assets

The mod doesn't ship video files, but a few UI animations use **sprite
sheets** (horizontal strip, N frames, PNG). If a future phase adds more,
follow this pattern.

### 6.1  Loading spinner
- **Output file:** `assets/zenithclient/textures/gui/spinner.png`
- **Size:** 192 × 32 (24 px frame × 8 frames).
- **Style:** A 6-petal flower/asterisk shape in violet `#7C52FF`, rotating
  in 45° steps across the 8 frames. Transparent background, 0.8 s full loop
  when played at 10 fps.

### 6.2  Ripple / click feedback
- **Output file:** `assets/zenithclient/textures/gui/click_ripple.png`
- **Size:** 96 × 16 (6 frames). Expanding circle (frame 1 = 4 px diameter,
  frame 6 = 48 px), violet outline fading from 80 % to 0 % opacity.
  Used as a button-press confirmation overlay.

### 6.3  Failsafe alarm pulse (optional)
- **Output file:** `assets/zenithclient/textures/gui/alarm_pulse.png`
- **Size:** 64 × 16 (4 frames). Red warning border flashing — frame 1
  transparent, frame 2/3/4 with `#F87171` border at 30/60/30 % opacity.

---

## 7. Sound effects (`sounds/`)

All are **mono** (except where noted), signed 16-bit PCM WAV at 44.1 kHz,
peak ≤ −1 dBFS, 0.15–1.0 s in length. They must be original synthesis /
royalty-free Foley — **do not** use Mojang vanilla sounds and **do not**
use recognizable meme/pop-culture audio (brand invisibility).

Volume balance: a master volume slider scales them; peak loudness should
match vanilla UI sounds (~−16 LUFS integrated).

| File | Description | Sound design brief |
|---|---|---|
| `notification.wav` | Generic toast/notification ping | Soft high-low glass tap, similar to a subtle iOS notification. Two sine tones (880 Hz → 660 Hz), 80 ms attack, 200 ms tail, a small upward chime tail. Mono. |
| `flip_complete.wav` | Successful flip / profit gain | Coins clinking + a bright upward chime (three notes rising A→C#→E). Sounds like a small cash register "ka-ching" but modern/soft, no cartoon spring. 600 ms. |
| `failsafe_alert.wav` | Warning / failsafe triggered | Two low brass-like thumps (120 Hz), with a faint high-frequency alert whine (2 kHz triangular) rising over 300 ms, ending abruptly. Gains attention without being jarring. Stereo (subtle panning) so it stands out. 500 ms. |
| `etherwarp_chime.wav` | Etherwarp teleport success | Short crystalline "whoosh" into a high bell chime (1320 Hz sine with quick reverb), then a brief low thump as the player lands. Sounds like a clean, magic-y teleport without feeling cartoonish. 350 ms. Stereo. |

Future phases will add more (macro start/stop, GUI tab switch, toggle on/off,
AH purchase jingle, Bazaar fill, Jacob contest start, pest spawn, etc.) —
each new entry follows the same table format: trigger context, length,
timbre, peak loudness.

---

## 8. Language files (`lang/`)

**Canonical source:** `assets/zenithclient/lang/en_us.json` (American English
acts as the master key set). `en_gb.json` is the default loaded in game and
starts as a copy of `en_us.json` with British spellings ("colour",
"behaviour", "centre", "minimise", "travelling" etc.).

Other locales translate the English values; keys are never translated.

### 8.1  Key namespace and structure
Every key lives under `zenithclient.<section>.<name>`:

- `zenithclient.chat.*` — chat messages (prefix, info/warn/error/success formats).
- `zenithclient.command.*` — command responses (`.z ...` output).
- `zenithclient.dashboard.*` — all dashboard labels, buttons, tab titles.
- `zenithclient.hud.*` — HUD panel labels, tooltip strings.
- `zenithclient.failsafe.*` — failsafe trigger names, severity labels, reactions.
- `zenithclient.flipper.*` — flipper categories, states, coin formatting.
- `zenithclient.macro.*` — macro display names, state strings, error messages.
- `zenithclient.devdata.*` — dev-data tour/hud strings.
- `zenithclient.gui.*` — generic widget labels (Close, Save, Cancel, Reset).

### 8.2  Required en_us keys (minimum launch set)
Generate a complete `en_us.json` containing **all** of the following keys;
every other locale must translate the same key set.

```jsonc
{
  // ---------- prefix / chat ----------
  "zenithclient.chat.prefix":          "§8[§dzenith§8]§r",
  "zenithclient.chat.info":            "§7{}",
  "zenithclient.chat.warn":            "§e{}",
  "zenithclient.chat.error":           "§c{}",
  "zenithclient.chat.success":         "§a{}",

  // ---------- dashboard ----------
  "zenithclient.dashboard.title":       "Zenith Dashboard",
  "zenithclient.dashboard.phase":       "Phase 11 of 20",
  "zenithclient.dashboard.tab_home":    "Home",
  "zenithclient.dashboard.tab_flipper": "Flipper",
  "zenithclient.dashboard.tab_failsafe":"Failsafe",
  "zenithclient.dashboard.tab_macros":  "Macros",
  "zenithclient.dashboard.tab_settings":"Settings",
  "zenithclient.dashboard.close":       "Close",
  "zenithclient.dashboard.esc_hint":    "Press ESC to close",
  "zenithclient.dashboard.resume":      "Resume Failsafe",
  "zenithclient.dashboard.emergency":   "EMERGENCY STOP",
  "zenithclient.dashboard.start_flipper":"Start Flipper",
  "zenithclient.dashboard.stop_flipper": "Stop Flipper",
  "zenithclient.dashboard.clear_resume": "Clear & Resume",
  "zenithclient.dashboard.disconnect":   "Disconnect",
  "zenithclient.dashboard.stop_all":     "Stop All",
  "zenithclient.dashboard.start":        "Start",
  "zenithclient.dashboard.stop":         "Stop",
  "zenithclient.dashboard.running":      "● RUNNING",
  "zenithclient.dashboard.idle":         "○ idle",

  "zenithclient.dashboard.setting.hud":       "HUD visible",
  "zenithclient.dashboard.setting.brainview": "Brain View (debug)",
  "zenithclient.dashboard.setting.prefix":    "Chat prefix",
  "zenithclient.dashboard.setting.hud_editor":"Open HUD Editor",
  "zenithclient.dashboard.setting.reset_hud": "Reset HUD Layout",
  "zenithclient.dashboard.setting.devdata":   "Record dev data (→ OBSERVATIONS.md)",
  "zenithclient.dashboard.setting.devdata_flush":"Flush devdata .md now",
  "zenithclient.dashboard.setting.devdata_tour":"Run dev data tour",

  // ---------- toggles ----------
  "zenithclient.dashboard.flips.ah":     "AH BIN Flips",
  "zenithclient.dashboard.flips.bazaar": "Bazaar spreads",
  "zenithclient.dashboard.flips.craft":  "Craft flips",
  "zenithclient.dashboard.flips.npc":    "NPC flips",
  "zenithclient.dashboard.flips.breaks": "Scheduled breaks",

  // ---------- failsafe ----------
  "zenithclient.failsafe.sound": "Sound alert",
  "zenithclient.failsafe.toast": "Toast alert",
  "zenithclient.failsafe.severity.none":    "Clear",
  "zenithclient.failsafe.severity.notify":  "Notice",
  "zenithclient.failsafe.severity.pause":   "Paused",
  "zenithclient.failsafe.severity.wiggle":  "Wiggle",
  "zenithclient.failsafe.severity.combat":  "Combat",
  "zenithclient.failsafe.severity.obstruction":"Obstruction",
  "zenithclient.failsafe.severity.respawn": "Respawning",
  "zenithclient.failsafe.severity.repath":  "Repathing",
  "zenithclient.failsafe.severity.warp_is": "→ /is",
  "zenithclient.failsafe.severity.warp_hub":"→ /hub",
  "zenithclient.failsafe.severity.disconnect":"Disconnect",

  // ---------- HUD panels ----------
  "zenithclient.hud.safety":   "Safety",
  "zenithclient.hud.farming":  "{}",        // dynamic macro icon + name
  "zenithclient.hud.profit":   "Profit",
  "zenithclient.hud.breaks":   "Break",
  "zenithclient.hud.mayor":    "Mayor",
  "zenithclient.hud.macro":    "Macro",
  "zenithclient.hud.flips":    "Active Flips",
  "zenithclient.hud.budget":   "Budget",
  "zenithclient.hud.combat":   "{} Loot",
  "zenithclient.hud.session":  "Session",

  // ---------- macros (display names — these are what users see in lists) ----------
  "zenithclient.macro.idle.name":      "Idle",
  "zenithclient.macro.devdata.name":   "Dev Data Tour",
  "zenithclient.macro.melon.name":     "Melon Farming",
  "zenithclient.macro.pumpkin.name":   "Pumpkin Farming",

  // ---------- command responses ----------
  "zenithclient.command.devdata.enabled":  "Dev data harvester ENABLED. Writing to {}",
  "zenithclient.command.devdata.disabled": "Dev data harvester DISABLED (file preserved at {}).",
  "zenithclient.command.devdata.status":   "DevData: enabled={}, guis={}, chat={}, uptime={}m",
  "zenithclient.command.devdata.flushed":  "devdata flushed → {}",
  "zenithclient.command.devdata.note_ok":  "note appended to devdata file.",
  "zenithclient.command.devdata.tour":     "Dev data tour started — it will warp around automatically.",
  "zenithclient.command.devdata.tour_stop":"Dev data tour stopped.",
  "zenithclient.command.devdata.path":     "DevData file: {}",

  // ---------- generic ----------
  "zenithclient.gui.save":   "Save",
  "zenithclient.gui.cancel": "Cancel",
  "zenithclient.gui.reset":  "Reset",
  "zenithclient.gui.close":  "Close",
  "zenithclient.gui.search": "Search…",
  "zenithclient.gui.on":     "On",
  "zenithclient.gui.off":    "Off"
}
```

### 8.3  Locales to ship at launch
| File | Language | Notes |
|---|---|---|
| `en_us.json` | American English | Master source — always up to date first |
| `en_gb.json` | British English | Same keys, en_GB spellings (colour, behaviour, centre, minimise/travelling/queueing); default locale the mod loads |
| `de_de.json` | German — Germany | |
| `es_es.json` | Spanish — Spain | |
| `fr_fr.json` | French — France | |
| `it_it.json` | Italian — Italy | |
| `ja_jp.json` | Japanese | Use full-width CJK punctuation where appropriate |
| `ko_kr.json` | Korean | |
| `nl_nl.json` | Dutch — Netherlands | |
| `pl_pl.json` | Polish | |
| `pt_br.json` | Portuguese — Brazil | |
| `ru_ru.json` | Russian | Cyrillic |
| `tr_tr.json` | Turkish | |
| `zh_cn.json` | Chinese (Simplified, China) | Simplified Han |
| `zh_tw.json` | Chinese (Traditional, Taiwan) | Traditional Han |

For each non-English locale the generator should start from a copy of
`en_us.json` and translate the **values** only — never the keys. Google
Translate / DeepL quality is **not** acceptable for release; flag any
locale whose translation confidence is below "professional human" quality
as a stub (`en_us` values with a `// TODO: translate` comment is
acceptable for early phases but not 1.0).

### 8.4  Placeholder / formatting tokens
All placeholders use SLF4J-style `{}` (no `%s`, no `{0}`), consistent
with `ZenithChat`. Numbers (coins, XP, times) are formatted by Java at
runtime — strings like `"coins: {}"` should not embed hardcoded numbers.

---

## 9. Splash / title screen (future Phase 18)

When we add a custom splash screen (Phase 18), the prompt below is the
spec; do not generate yet, but keep it here so there's one source of truth.

- **Output file:** `assets/zenithclient/textures/gui/splash_background.png`
- **Size:** 1920 × 1080 px, tilable/scalable to any screen (we render a
  4K master 3840 × 2160 and downscale).
- **Style:** abstract mountain silhouette at the bottom (same geometry as
  the brand mark's summit triangle), deep midnight sky, a faint violet
  aurora glow on the left, a cyan shooting star on the right, the gold
  sun just peeking over the summit peak. The ZENITH CLIENT wordmark sits
  in the lower third.
- The background must be dark enough that white login/menu text over the
  top is fully readable (luminance ≤ 0.15 in the text region).

---

## 10. Asset generation guidance for image models

If feeding these prompts into Midjourney / SDXL / DALL·E / Flux, append
this negative prompt suffix to any image prompt that needs to look like
in-game pixel/UI art:

> **Negative:** Minecraft blocky voxels, photorealism, 3D render with
> heavy bevel, neon, cyberpunk clichés, busy backgrounds, script/serif
> fonts, watermarks, signatures, text in languages other than English,
> JPEG artifacts, dithering banding, Minecraft vanilla texture pack
> aesthetic (unless specifically requested). For icon/UI sizes add:
> "single isolated glyph on a transparent background, 2 px stroke, no
> drop shadow".

For **pixel-art** requests (e.g. custom block/item textures added later)
swap the style keyword to "pixel art, 16 × 16 sprite, limited 64-colour
palette matching the Z-Palette, anti-aliased at 2× scale, clean outlines,
consistent light direction top-left" — but note: most Zenith UI assets are
vector-smooth, not pixel-art. Pixel-art is reserved for block/item
replacements that need to blend with vanilla.

For **audio generation** (ElevenLabs SFX, sfxr, custom synthesis): use
keywords "subtle UI sound, modern, soft attack, short tail, no
reverberant space, dry mix, mastered to -1 dBFS peak".

---

## 11. Changelog of additions

When new assets are added during later phases, append them in the
appropriate section with the file path and a short description. Keep
this file as the living master — any time code references a texture or
sound path, that path must be documented here before the asset is
considered "done".
