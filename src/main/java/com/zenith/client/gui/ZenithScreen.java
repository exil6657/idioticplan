package com.zenith.client.gui;

import com.zenith.client.gui.component.Component;
import com.zenith.client.gui.component.GuiDrawContext;

import java.util.ArrayList;
import java.util.List;

/** Base class for all Zenith GUI screens (dashboard, HUD editor, settings). */
public abstract class ZenithScreen {
    protected final List<Component> children = new ArrayList<>();
    public String title = "";
    public boolean paused = true;
    public boolean closeOnEsc = true;

    public void add(Component c) { children.add(c); }

    public void init(int screenW, int screenH) {}
    public void render(GuiDrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        for (Component c : children) {
            if (!c.visible) continue;
            c.hovered = c.isMouseOver(mouseX, mouseY);
            c.render(ctx, mouseX, mouseY, tickDelta);
        }
    }
    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (Component c : children) if (c.visible && c.isMouseOver(mouseX, mouseY)) c.mouseClicked(mouseX, mouseY, button);
    }
    public void onClose() {}
}
