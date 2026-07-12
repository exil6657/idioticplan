package com.zenith.client.gui.hud;

import com.zenith.client.core.event.annotation.SubscribeEvent;
import com.zenith.client.core.event.events.RenderHudEvent;
import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.panels.ModuleListPanel;
import com.zenith.client.gui.hud.panels.WatermarkPanel;
import com.zenith.client.gui.hud.editor.HudEditor;
import com.zenith.client.gui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manager for HUD panels. Panels register themselves here and are rendered
 * via the RenderHudEvent. The HUD editor interacts with this manager to
 * drag/reposition panels.
 */
public final class HudManager {
    private static final HudManager INSTANCE = new HudManager();
    private final List<HudPanel> panels = new ArrayList<>();
    private boolean initialized;

    public static HudManager getInstance() { return INSTANCE; }

    public void init() {
        panels.clear();
        register(new WatermarkPanel());
        register(new ModuleListPanel());
        HudLayoutManager.getInstance().init();
        applyLayout();
        if (!initialized) {
            com.zenith.client.core.event.ZenithEventBus.getInstance().register(this);
            initialized = true;
        }
    }

    public void register(HudPanel p) { panels.add(p); }

    public Collection<HudPanel> allPanels() { return panels; }

    @SubscribeEvent
    public void onRender(RenderHudEvent event) {
        GuiDrawContext ctx = new GuiDrawContext(event.graphics, event.screenWidth, event.screenHeight, event.tickDelta);
        for (HudPanel p : panels) {
            if (!p.visible) continue;
            p.render(ctx, 0, 0, event.tickDelta);
        }
        HudEditor.getInstance().render(ctx);
    }

    /** Apply persisted layout positions. */
    public void applyLayout() {
        var layout = HudLayoutManager.getInstance().layout();
        int w = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
        for (HudPanel p : panels) {
            var e = layout.panels.get(p.panelId);
            if (e != null) {
                p.x = e.offsetX; p.y = e.offsetY;
                p.width = e.width > 0 ? e.width : p.getDefaultWidth();
                p.height = e.height > 0 ? e.height : p.getDefaultHeight();
                p.visible = e.visible;
            } else {
                // Default positions
                p.width = p.getDefaultWidth();
                p.height = p.getDefaultHeight();
                if (p instanceof WatermarkPanel) { p.x = 4; p.y = 4; }
                else if (p instanceof ModuleListPanel) { p.x = w - 8 - p.width; p.y = 16; }
            }
        }
    }
}
