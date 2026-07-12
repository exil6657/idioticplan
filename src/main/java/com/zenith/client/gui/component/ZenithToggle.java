package com.zenith.client.gui.component;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.theme.ThemeManager;

import java.util.function.Consumer;

/** Small toggle switch (used in settings panels). */
public class ZenithToggle extends Component {
    public boolean value;
    public String label;
    public Consumer<Boolean> onToggle;

    public ZenithToggle(String label, boolean initial) {
        this.label = label; this.value = initial; this.width = 120; this.height = 16;
    }
    public ZenithToggle onToggle(Consumer<Boolean> cb) { this.onToggle = cb; return this; }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var t = ThemeManager.getInstance().current();
        ctx.drawString(label, x, y+4, t.textPrimary, true);
        float tx = x + width - 24, ty = y+2;
        ctx.fillRect(tx, ty, 22, 12, new Color4f(0.2f,0.2f,0.25f,1f));
        Color4f on = new Color4f(0.2f,0.8f,0.4f,1f);
        Color4f off = new Color4f(0.4f,0.2f,0.2f,1f);
        ctx.fillRect(value ? tx+10 : tx, ty+1, 12, 10, value ? on : off);
    }

    @Override
    public void mouseClicked(int mx, int my, int b) {
        if (b == 0 && isMouseOver(mx, my)) {
            value = !value;
            if (onToggle != null) onToggle.accept(value);
        }
    }
}
