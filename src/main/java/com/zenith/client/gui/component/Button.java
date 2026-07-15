package com.zenith.client.gui.component;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.animation.AnimatedValue;
import com.zenith.client.gui.animation.EasingType;
import com.zenith.client.gui.theme.ThemeManager;

import java.util.function.Consumer;

public class Button extends Component {
    public String label;
    public Consumer<Button> onClick;
    private final AnimatedValue hoverFade = new AnimatedValue(0f);

    public Button(String label) { this.label = label; this.height = 18; this.width = 80; }
    public Button(String label, Consumer<Button> onClick) { this(label); this.onClick = onClick; }

    @Override public void render(GuiDrawContext ctx, int mx, int my, float td) {
        hoverFade.animateTo(hovered ? 1f : 0f, 150, EasingType.EASE_OUT);
        float a = hoverFade.get();
        var t = ThemeManager.getInstance().current();
        Color4f bg = new Color4f(
            t.panel.r()*(1-a) + t.accent.r()*a,
            t.panel.g()*(1-a) + t.accent.g()*a,
            t.panel.b()*(1-a) + t.accent.b()*a, t.panel.a());
        ctx.drawRoundedRect(x, y, width, height, 3f, bg);
        float tw = ctx.stringWidth(label);
        ctx.drawString(label, x + (width-tw)/2f, y + (height-8)/2f, t.textPrimary, true);
    }
    @Override public void mouseClicked(int mx, int my, int b) {
        if (b == 0 && onClick != null) onClick.accept(this);
    }
}
