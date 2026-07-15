# Zenith Client — Research Log

Research items from the master document (Part 3 §1). Status is updated as each item is completed.

## R001 — Minecraft 26.1.2 Fabric Build System [CRITICAL]
- **Status:** COMPLETE
- **Findings:**
  - Fabric Loom 1.15 is required; it runs in a new UN-OBFUSCATED mode because
    Minecraft 26.1 is the first release to ship unobfuscated (no intermediary
    mappings, no remapJar, no Yarn support; Mojang official names are used
    directly in code).
  - Gradle 9.4.0; Gradle JVM must be **Java 25** minimum.
  - Minimum Fabric Loader: 0.18.4 (using 0.18.9).
  - Fabric API: 0.145.4+26.1.2 (ID `fabric-api`; bare `fabric` ID removed).
  - Mod code is compiled to Java 21 class file version (`options.release = 21`)
    per master spec; Gradle itself runs on JDK 25.
  - Use plain `implementation`/`compileOnly`/`jar` (not `modImplementation`/
    `remapJar`) in Loom 1.15 unobfuscated mode.
  - Rendering is still OpenGL/Blaze3D in 26.1; Vulkan migration expected in
    26.2 (ZenithRenderer/MC26Renderer target Blaze3D, not raw GL).
  - `HudRenderCallback` was removed in favour of `HudElementRegistry`;
    `ColorProviderRegistry` simplified to `BlockColorRegistry`; ItemStack
    construction now requires `ItemStackTemplate` until a world is loaded.
- **Sources:** https://fabricmc.net/2026/03/14/261.html,
  https://modrinth.com/mod/fabric-api/version/0.145.4+26.1.2
- **Blocks:** build.gradle (complete), MC26Renderer.java (notes added),
  all Mixin class stubs (complete, no-op for Phase 1).

## R002 — Scoreboard Format Per Location [HIGH]
- **Status:** PENDING
- **Sources:** In-game testing per location, hypixel-skyblock.fandom.com, NEU/SkyHanni reference.
- **Blocks:** ScoreboardParser.java, LocationValidator.java, PurseParser.java.

## R003 — Tab List Format Per Context [HIGH]
- **Status:** PENDING
- **Blocks:** TabListParser.java, CoopActivityTracker.java, MayorTracker.java.

## R004 — Chat Message Formats [HIGH]
- **Status:** PENDING
- **Blocks:** ChatParser.java, all macro chat listeners.

## R005 — Action Bar Format [HIGH]
- **Status:** PENDING
- **Blocks:** ActionBarParser.java, SkillXPTracker.java, BobberDetector.java.

## R006 — Hypixel Lobby NPC Locations [HIGH]
- **Status:** PENDING
- **Blocks:** LobbyNavigator.java, BankNPCNavigator.java, MuseumGUINavigator.java, NPCNavigator.java.

## R007 — Limbo Detection [HIGH]
- **Status:** PENDING
- **Blocks:** LimboDetector.java, LimboRecoveryEngine.java.

## R008 — Ender Chest GUI [HIGH]
- **Status:** PENDING
- **Blocks:** EnderChestGUI.java, EnderChestScanner.java, EnderChestManager.java.

## R009 — Banking System [HIGH]
- **Status:** PENDING
- **Blocks:** BankGUIInteractor.java, PersonalBankManager.java, CoopBankManager.java.

## R010 — Community Upgrades [HIGH]
- **Status:** PENDING
- **Blocks:** CommunityUpgradeMacro.java, CommunityUpgradeDatabase.java, CommunityUpgradeTimer.java.

## R011 — Museum System [HIGH]
- **Status:** PENDING
- **Blocks:** MuseumGUINavigator.java, MuseumTierSystem.java, MuseumMilestoneDatabase.java.

## R012 — Jacob's Contest System [HIGH]
- **Status:** PENDING
- **Blocks:** JacobContestMacro.java, JacobContestTracker.java.

## R013 — Heart of the Mountain (HOTM) Perk Tree [HIGH]
- **Status:** PENDING
- **Blocks:** HOTMTree.java, HOTMProgressionPlanner.java, HOTMPerkAllocator.java, AbilityManager.java.

## R014 — Heart of the Forest (HOTF) [HIGH]
- **Status:** PENDING
- **Blocks:** HOTFTree.java, HOTFProgressionPlanner.java.

## R015 — Farming Meta & Farm Designs [HIGH]
- **Status:** PENDING
- **Blocks:** FarmDesignDatabase.java, all crop macros, FarmingSpeedCalculator.java.

## R016 — Pest System [HIGH]
- **Status:** PENDING
- **Blocks:** PestFarmMacro.java, PestType.java, PestSpawnOptimizer.java, SprayScheduler.java.

## R017 — Greenhouse Genetics System [HIGH]
- **Status:** PENDING (verify feature exists first)
- **Blocks:** GeneticsEngine.java, BreedingChart.java, GeneticTraitType.java.

## R018 — Visitor System [MEDIUM]
- **Status:** PENDING
- **Blocks:** VisitorMacro.java, VisitorItemParser.java, VisitorDetector.java.

## R019 — Trophy Fish All Types [HIGH]
- **Status:** PENDING
- **Blocks:** TrophyFishDatabase.java, TrophyFishTracker.java, TrophyFishRouter.java.

## R020 — Fishing Progression [HIGH]
- **Status:** PENDING
- **Blocks:** FishingGearAdvisor.java, FishingProgressionPlanner.java, BaitManager.java.

## R021 — All Slayer Mechanics (6 types) [HIGH]
- **Status:** PENDING
- **Blocks:** All SlayerMacro classes, slayer data JSON files.

## R022 — Dungeon Mechanics [MEDIUM]
- **Status:** PENDING
- **Blocks:** All dungeon classes, RoomDatabase.json, SecretLocationDatabase.json.

## R023 — Kuudra Mechanics [MEDIUM]
- **Status:** PENDING
- **Blocks:** All Kuudra classes, AttributeDatabase.java.

## R024 — Rift Mechanics (23 sub-topics) [HIGH]
- **Status:** PENDING
- **Blocks:** All Rift macro classes, EnigmaSoulDatabase.json, RiftNPCDatabase.java.

## R025 — Item Abilities Database [HIGH]
- **Status:** PENDING
- **Blocks:** ItemAbilityDatabase.java, AbilityLearner.java, CooldownTracker.java.

## R026 — Optimal Gear Progression Paths [HIGH]
- **Status:** PENDING
- **Blocks:** GearProgressionPath.java, all GearMetaDatabase classes.

## R027 — Optimal Pet Database [HIGH]
- **Status:** PENDING
- **Blocks:** OptimalPetDatabase.java, PetSwapManager.java, PetLevelingMacro.java.

## R028 — Reforge System [HIGH]
- **Status:** PENDING
- **Blocks:** OptimalReforgeDatabase.java, TalismanReforgeOptimizer.java, ReforgeBlacksmithGUI.java.

## R029 — Essence System [MEDIUM]
- **Status:** PENDING
- **Blocks:** EssenceDatabase.java, EssenceShopGUI.java, EssencePriorityPlanner.java.

## R030 — Crimson Faction System [MEDIUM]
- **Status:** PENDING
- **Blocks:** CrimsonRepMacro.java, ReputationStrategy.java, CrimsonFactionShopManager.java.

## R031 — Accessory Priority List [HIGH]
- **Status:** PENDING
- **Blocks:** AccessoryDatabase.java, AccessoryPriorityCalculator.java, AccessoryProgressTracker.java.

## R032 — Carnival Games [MEDIUM]
- **Status:** PENDING
- **Blocks:** All carnival game classes.

## R033 — Bingo Card System [MEDIUM]
- **Status:** PENDING
- **Blocks:** BingoMacro.java, BingoCardParser.java, BingoObjectiveType.java.

## R034 — Dark Auction [MEDIUM]
- **Status:** PENDING
- **Blocks:** DarkAuctionMacro.java, DarkAuctionDetector.java.

## R035 — Minion System [HIGH]
- **Status:** PENDING
- **Blocks:** MinionRecipeDatabase.java, MinionCostOptimizer.java, MinionOptimizer.java.

## R036 — Coflnet API Documentation [CRITICAL]
- **Status:** PENDING
- **Blocks:** All Coflnet API client classes.

## R037 — NEU Repository Structure [CRITICAL]
- **Status:** PENDING
- **Blocks:** NEURepoClient.java, NEUItemFetcher.java, NEURecipeFetcher.java.

## R038 — SkyBlock Level XP Sources [MEDIUM]
- **Status:** PENDING
- **Blocks:** SBXPSourceDatabase.java, SBXPEfficiencyCalculator.java.

## R039 — Enchantment System BiS [HIGH]
- **Status:** PENDING
- **Blocks:** OptimalEnchantDatabase.java, AnvilOrderOptimizer.java, BookCombiner.java.

## R040 — Drop Rates Database [HIGH]
- **Status:** PENDING
- **Blocks:** DropRateDatabase.java, GrindVsBuyAnalyzer.java.

## R041 — Auction House GUI layout [COMPLETE]
- **Status:** VERIFIED (wiki.hypixel.net 2026-07)
- **Findings:**
  - `/ah` opens the Auction Browser (Booster Cookie required for remote access).
  - Browser page top bar: Gold Block (Refresh BINs), Golden Carrot (Auction Stats),
    Golden Horse Armor ("Manage Auctions"), Barrier (Close), ↻ Refresh, Empty Map (Auction Stats),
    Arrow (Back).
  - "Manage Auctions" page contains a "Create Auction" golden horse armor AND a "Create BIN Auction"
    gold ingot next to an arrow; BIN mode is a separate toggle item.
  - Clicking Create BIN → Choose Item (inventory screen title "Choose Item") → click item →
    Create BIN screen with gold bar (price sign), clock (duration), etc.
  - After typing price into the sign and clicking Done, the gold "Create" button lists the BIN.
  - Confirmation screen title is "Confirm" with a "Buy Item" stained-glass button.
  - Search opens an oak sign; lore for BIN auction items contains "Buy it now: <price> coins".
- **Corrections applied:** Added findCreateBinButton / findBinToggleOnCreate / clickCreateBin;
  executor clicks Create BIN Auction gold ingot not the regular Create Auction horse armor.
- **[RESEARCH NEEDED]** Exact display name and lore text for the BIN price gold bar on the
  "Create BIN Auction" screen (currently matching lore "Buy it now"/"Price" — verify in-game).
- **Sources:** https://hypixelskyblock.minecraft.wiki/w/Auction_House

## R042 — Bazaar GUI layout [PARTIAL]
- **Status:** PARTIAL
- **Findings:**
  - `/bz` opens Bazaar main menu (categories Farming/Mining/Combat/Woods&Fishes/Oddities).
  - Product page: "Buy Instantly", "Sell Instantly", "Create Buy Order", "Create Sell Order" buttons.
  - Instant Buy: sign prompt "How many do you want?" (quantity) → confirm screen "Buy X for Y coins".
  - Instant Sell left-click sells ALL matching items in inventory; right-click selects quantity.
  - Custom price sign prompt: "At what price per unit?" with presets (match top offer, +0.1, 5% spread, custom).
- **[RESEARCH NEEDED]** Exact item display names/lore for Buy Instantly / Sell Instantly /
  Buy Order / Sell Order buttons; sign-screen titles; confirmation-screen titles (we guess
  "Buy X instantly?" / "Sell X instantly?" and "How much do you want" / "At what price").
  See `BazaarGUI` findBuyInstantly / findSellInstantly which currently use nameContains.
- **Sources:** https://hypixel-skyblock.fandom.com/wiki/Bazaar

## R043 — Warp commands [COMPLETE]
- **Status:** VERIFIED
- **Findings (wiki.hypixel.net Travel Scrolls):**
  - /warp barn (The Barn — farming), /warp park (Birch Park — foraging),
    /warp deep (Deep Caverns — NOT deepcaverns), /warp gold (Gold Mine),
    /warp spider (Spider's Den), /warp end (The End), /warp isle (Crimson Isle — NOT nether),
    /warp mines (Dwarven Mines), /warp da (Dark Auction), /warp crypt/museum/wizard etc.
- **Corrections applied:** SkyblockNavigator uses /warp deep, /warp isle, /warp mines.
- **Sources:** https://hypixelskyblock.minecraft.wiki/w/Travel_Scrolls

## R044 — Limbo [PARTIAL]
- **Status:** PARTIAL
- **Findings:** Limbo is entered on AFK kick, build 0 server, or lobby crash. Title bar says
  "You are in Limbo!" in red bold. No blocks render. Use `/lobby` to return to main lobby,
  then `/play skyblock` to rejoin SkyBlock. `/is` does NOT work from Limbo.
- **Corrections applied:** SkyblockNavigator.escapeLimbo() sends /lobby; caller must follow
  up with toSkyblock() after delay (Phase 13+ travel engine).
- **Sources:** Reddit/hypixel.net threads; confirm exact title text in-game.

## R045 — Death/respawn in SkyBlock [PARTIAL]
- **Status:** PARTIAL
- **Findings:** In most SkyBlock areas death respawns you at the area spawn or your island
  with a coins penalty and no vanilla DeathScreen; the "You died!" title appears with a
  "Respawning..." countdown in chat. Auto-respawn (clicking immediately) is not a ban risk.
- **[RESEARCH NEEDED]** Whether DeathScreen actually appears in SkyBlock (likely no; need to
  verify so RespawnAction doesn't click a non-existent button).
- **Sources:** Hypixel SkyBlock death mechanics wiki.

## R046 — Hypixel custom health system [PARTIAL]
- **Status:** PARTIAL
- **Findings:** HP shown on scoreboard/action bar is effective HP (base HP + defense +
  absorption + fairy souls). Vanilla `player.getHealth()` returns 0–20 and is mostly misleading;
  PlayerHealthMonitor should read from scoreboard/action bar for real HP.
- **[RESEARCH NEEDED]** Exact action-bar regex for HP/Defense/Mana.
- **Sources:** NEU / SkyHanni source.

## R047 — SignEditScreen field layout [PARTIAL]
- **Status:** PARTIAL
- **Findings:** In 1.20+ SignEditScreen holds two SignText objects (`frontText`/`backText` in Yarn;
  Mojang names unknown for 26.1 — possibly `signText` + `front`/`back`). Each SignText has
  `Text[] messages` and `Text[] filteredMessages`. Older MC used a `List<Component> signText`
  or `String[]`. SignInputHandler now reflectively walks signText/frontText/backText/message
  fields and sets line 0 via either List.set, array assignment, or SignText.messages[].
- **[RESEARCH NEEDED]** Exact Mojang field names for 26.1 SignEditScreen.
- SignInputHandler.findField/superclass walk should be robust enough to find the field
  regardless of naming, but onDone() invocation should be verified in-game.
