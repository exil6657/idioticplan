package com.zenith.client.gui;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.animation.AnimationEngine;
import com.zenith.client.gui.component.GuiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Bridges a {@link ZenithScreen} to vanilla's {@link Screen}. Zenith screens are
 * not themselves Screen subclasses (so the component framework can be unit
 * tested headlessly), so the wrapper handles input forwarding, mouse events,
 * and rendering via our {@link GuiDrawContext}.
 */
public class ZenithScreenWrapper extends Screen {

    private static ZenithScreenWrapper current;

    public static void open(ZenithScreen zs) {
        // Must be called on the main/render thread.
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            var w = new ZenithScreenWrapper(zs);
            current = w;
            mc.setScreen(w);
        });
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ZenithScreenWrapper) mc.execute(mc::setScreen);
        current = null;
    }

    public static boolean isOpen() { return current != null; }
    public static ZenithScreen currentZenith() { return current != null ? current.zenith : null; }

    private final ZenithScreen zenith;
    private long openedAt;

    private ZenithScreenWrapper(ZenithScreen zs) {
        super(Component.literal(zs.title == null ? "" : zs.title));
        this.zenith = zs;
    }

    @Override
    protected void init() {
        super.init();
        zenith.children.clear();
        zenith.init(width, height);
        openedAt = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        // Darken background via a translucent fill — avoid renderBackground(...) because
        // 26.1's signature has shifted between pre-releases.
        GuiDrawContext bgCtx = new GuiDrawContext(g, width, height, partial);
        bgCtx.fillRect(0, 0, width, height, new Color4f(0,0,0,0.55f));
        GuiDrawContext ctx = bgCtx;

        // If the screen provides a render method, let it draw; else fallback to children.
        AnimationEngine.getInstance().frame();
        zenith.render(ctx, mx, my, partial);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        zenith.mouseClicked((int) mx, (int) my, button);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void onClose() {
        zenith.onClose();
        current = null;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
