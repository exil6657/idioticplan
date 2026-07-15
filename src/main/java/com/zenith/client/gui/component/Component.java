package com.zenith.client.gui.component;

/** Base class for all GUI components. Coordinates are in screen pixels. */
public abstract class Component {
    public float x, y, width, height;
    public boolean visible = true;
    public boolean hovered;
    public String id = "";

    public Component at(float x, float y) { this.x = x; this.y = y; return this; }
    public Component size(float w, float h) { this.width = w; this.height = h; return this; }

    public abstract void render(GuiDrawContext ctx, int mouseX, int mouseY, float tickDelta);

    public void mouseClicked(int mouseX, int mouseY, int button) {}
    public void mouseReleased(int mouseX, int mouseY, int button) {}
    public void mouseDragged(int mouseX, int mouseY, int button, double dx, double dy) {}
    public void keyPressed(int key, int scanCode, int mods) {}
    public void mouseScrolled(int mouseX, int mouseY, double delta) {}

    public boolean isMouseOver(int mx, int my) {
        return mx >= x && mx <= x+width && my >= y && my <= y+height;
    }
}
