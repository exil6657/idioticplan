package com.zenith.client;

import com.zenith.client.config.ConfigManager;
import com.zenith.client.command.CommandManager;
import com.zenith.client.core.ClientTickDispatcher;
import com.zenith.client.core.chat.ZenithChat;
import com.zenith.client.core.event.ZenithEventBus;
import com.zenith.client.core.module.ModuleManager;
import com.zenith.client.core.protection.BitsProtection;
import com.zenith.client.engine.eyes.ZenithEyes;
import com.zenith.client.engine.input.InputEngine;
import com.zenith.client.engine.path.ZenithPath;
import com.zenith.client.gui.GuiEngine;
import com.zenith.client.keybind.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entrypoint for Zenith Client.
 *
 * <p><b>Phase 3 deliverable:</b> core framework + advanced ZenithEyes rotation
 * engine + ZenithPath A*/etherwarp pathfinding + InputEngine wired. Remaining
 * phases fill in mixins, macros, GUI, failsafes, etc. per the 20-phase roadmap.</p>
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
        LOGGER.info("  Phase:      5 of 20 — HUD System & Brain View.");
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

        // Register tick dispatch (must be after event bus init).
        ClientTickDispatcher.register();

        // Protection (bits)
        BitsProtection.getInstance().init();

        ZenithChat.getInstance().success("Phase 4 initialised (mixins live + GUI engine).");
        LOGGER.info("[Init] Phase 4 up (commands={}, modules={}).",
                CommandManager.getInstance().getAll().size(),
                ModuleManager.getInstance().count());
    }
}
