package com.zenith.client.gui.component;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.theme.ThemeManager;

public class Label extends Component {
    public String text;
    public Color4f color;
    public boolean shadow = true;
    public Label(String text) { this.text = text; this.height = 10; this.width = 80; }
    public Label(String text, Color4f c) { this(text); this.color = c; }
    @Override public void render(GuiDrawContext ctx, int mx, int my, float td) {
        Color4f c = color != null ? color : ThemeManager.getInstance().current().textPrimary;
        ctx.drawString(text, x, y, c, shadow);
    }
}
