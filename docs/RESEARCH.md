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
