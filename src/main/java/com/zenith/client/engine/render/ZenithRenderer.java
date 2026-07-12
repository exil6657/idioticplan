package com.zenith.client.engine.render;

import com.zenith.client.ZenithClient;

/**
 * Master render system.
 *
 * <p>Phase 3: API surface only; concrete Blaze3D / OpenGL rendering is wired in
 * Phase 5 once HUD/GUI is on the roadmap. The renderer owns the RenderContext
 * and delegates to ESP/HUD modules through the ModuleManager render hooks.</p>
 */
public final class ZenithRenderer {

    private static ZenithRenderer instance;

    private boolean initialized;

    private ZenithRenderer() {}

    public static ZenithRenderer getInstance() {
        if (instance == null) instance = new ZenithRenderer();
        return instance;
    }

    public void init() {
        initialized = true;
        ZenithClient.LOGGER.info("[ZenithRenderer] Initialized (Phase 3: API only; real rendering in Phase 5).");
    }

    public boolean isInitialized() { return initialized; }

    /** Begin a render frame. Called from MixinGameRenderer before world render. */
    public void beginFrame(RenderContext ctx) {
        // Phase 5: set up PoseStack + projection + draw queues.
    }

    public void endFrame() {}
}
