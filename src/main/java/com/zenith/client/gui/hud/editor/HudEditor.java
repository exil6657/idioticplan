package com.zenith.client.gui.hud.editor;

import com.zenith.client.gui.component.GuiDrawContext;
import com.zenith.client.gui.hud.HudManager;
import com.zenith.client.gui.hud.HudPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD Editor controller. Toggled via {@code .z hud edit} or keybind. When active,
 * panels can be dragged with the mouse; layout is persisted via {@code HudLayoutManager}.
 */
public final class HudEditor {
    private static final HudEditor INSTANCE = new HudEditor();
    private boolean active;
    private final DragController drag = new DragController();
    private final HudEditOverlay overlay = new HudEditOverlay();

    public static HudEditor getInstance() { return INSTANCE; }

    public void toggle() { active = !active; }
    public boolean isActive() { return active; }

    public void mouseClicked(int mx, int my, int button) {
        if (!active || button != 0) return;
        for (HudPanel p : HudManager.getInstance().allPanels()) {
            if (p.isMouseOver(mx, my)) { drag.startDrag(p, mx, my); return; }
        }
    }
    public void mouseDragged(int mx, int my) { if (active) drag.drag(mx, my); }
    public void mouseReleased(int mx, int my, int button) {
        if (active && button == 0) {
            drag.drag(mx, my);
            drag.stopDrag();
            HudManager.getInstance().applyLayout();
            HudLayoutSaver.save();
        }
    }
    public void keyPressed(int key) { /* ESC closes handled by InputEngine */ }

    public void render(GuiDrawContext ctx) {
        if (!active) return;
        List<HudPanel> panels = new ArrayList<>();
        for (var p : HudManager.getInstance().allPanels()) panels.add(p);
        overlay.render(ctx, panels);
        // Snap after render drag
        for (var p : panels) SnapEngine.applySnap(p, ctx.screenWidth, ctx.screenHeight);
    }
}
