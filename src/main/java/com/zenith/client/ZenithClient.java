package com.zenith.client;

import com.zenith.client.api.ApiManager;
import com.zenith.client.config.ConfigManager;
import com.zenith.client.command.CommandManager;
import com.zenith.client.devdata.DevDataHarvester;
import com.zenith.client.core.ClientTickDispatcher;
import com.zenith.client.core.chat.ChatPatternEngine;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.module.ModuleManager;
import com.zenith.client.core.protection.BitsProtection;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.failsafe.FailsafeManager;
import com.zenith.client.flipping.FlipEngine;
import com.zenith.client.gui.GuiEngine;
import com.zenith.client.gui.hud.HudManager;
import com.zenith.client.keybind.KeybindManager;
import com.zenith.client.macro.MacroManager;
import com.zenith.client.macro.dev.DevDataFullTourMacro;
import com.zenith.client.macro.dev.DevDataMacro;
import com.zenith.client.macro.farming.MelonMacro;
import com.zenith.client.macro.farming.PumpkinMacro;
import com.zenith.client.macro.test.IdleMacro;
import com.zenith.client.world.WorldHook;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entrypoint for Zenith Client.
 *
 * <p><b>Current Phase 10 deliverable:</b> GUI dashboard (primary control surface),
 * dot-command tab completion, Auction House GUI interactor/executor state machine,
 * on top of the Phase 3 engines (ZenithEyes rotation, ZenithPath A* / etherwarp
 * pathfinding, InputEngine). Remaining phases fill in macros, crafting,
 * farming/mining/combat, fishing/foraging, events, Discord, ProGuard per the
 * 20-phase roadmap.</p>
 *
 * @author Exil
 * @see ZenithClientInfo
 */
public class ZenithClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZenithClientInfo.MOD_NAME);

    private static ZenithClient instance;

    public static ZenithClient getInstance() { return instance; }

    @Override
    public void onInitializeClient() {
        instance = this;

        LOGGER.info("==============================================================");
        LOGGER.info("  Zenith Client v{} loaded successfully.", ZenithClientInfo.VERSION);
        LOGGER.info("  Target:     Minecraft {}", ZenithClientInfo.MC_VERSION);
        LOGGER.info("  Developer:  {}", ZenithClientInfo.DEVELOPER);
        LOGGER.info("  Phase:     10 of 20 — GUI Dashboard + tab completion + AH foundation.");
        LOGGER.info("==============================================================");

        // 1. Config
        ConfigManager.getInstance().init();
        // 5. Event bus
        ZenithEventBus.getInstance().init();
        // 6. Keybind manager
        KeybindManager.getInstance().init();
        // 7. Chat
        ZenithChat.getInstance().info("Zenith Client v{} initialised.", ZenithClientInfo.VERSION);
        // 8. Commands
        CommandManager.getInstance().registerAll();
        // 9. Modules
        ModuleManager.getInstance().registerAll();

        // Phase 3: engines.
        ZenithEyes.getInstance().init();
        ZenithPath.getInstance().init();
        InputEngine.getInstance().init();

        // Phase 4: GUI engine (animation, component framework, dashboard screen).
        GuiEngine.getInstance().init();
        HudManager.getInstance().init();

        // Phase 6: Chat pattern engine fires events from incoming chat; world adapter.
        ChatPatternEngine.getInstance().init();
        WorldHook.init();

        // Phase 7: Failsafe system — 20 detectors, mistake-simulation + combat/
        // obstruction/respawn/repath reaction engine, severity ladder (NOTIFY →
        // WIGGLE_REACT → COMBAT → REMOVE_OBSTRUCTION → INSTANT_RESPAWN → REPATH
        // → WARP_ISLAND (/is) → WARP_HUB → DISCONNECT). Low health fights back,
        // blocked paths break the obstruction, death respawns instantly and
        // teleports re-pathfind to the macro anchor rather than stopping.
        FailsafeManager.getInstance().init();

        // Phase 11: Macro framework — register test + initial farming modules.
        // Real modules fleshed out in Phases 13-19.
        MacroManager.getInstance().register(IdleMacro.INSTANCE);
        MacroManager.getInstance().register(MelonMacro.INSTANCE);
        MacroManager.getInstance().register(PumpkinMacro.INSTANCE);
        MacroManager.getInstance().register(DevDataMacro.INSTANCE);
        MacroManager.getInstance().register(DevDataFullTourMacro.INSTANCE);

        // Phase 8: API/data layer — NEU items/recipes/constants, Moulberry lowestbin,
        // Coflnet bazaar/mayor/flips, rate limits, scrapers, wiki, update checker.
        ApiManager.getInstance().init();

        // Phase 9: Flipping engine — MarketScanner, AH/Bazaar/NPC/Craft strategies,
        // orders, budget manager, break scheduler, profit tracker.
        FlipEngine.getInstance().init();

        // Passive developer data harvester — listens to events and writes
        // structured markdown observations to <gameDir>/zenith/devdata/OBSERVATIONS.md.
        // Toggle via .z devdata on|off or the Settings dashboard tab.
        DevDataHarvester.getInstance().init();

        // Register tick dispatch (must be after event bus init).
        ClientTickDispatcher.register();

        // Protection (bits)
        BitsProtection.getInstance().init();

        ZenithChat.getInstance().success("Phase 10 initialised (dashboard GUI + dot-command tab completion + AuctionHouseExecutor GUI state machine).");
        LOGGER.info("[Init] Phase 10 up (commands={}, modules={}).",
                CommandManager.getInstance().getAll().size(),
                ModuleManager.getInstance().count());
    }
}
