package com.zenith.client.gui.component;

import com.zenith.client.engine.render.Color4f;
import com.zenith.client.gui.animation.AnimatedValue;
import com.zenith.client.gui.animation.EasingType;
import com.zenith.client.gui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Horizontal tab bar used in the dashboard and panels. */
public class ZenithTabBar extends Component {

    public static final class Tab {
        public String id;
        public String label;
        public Tab(String id, String label) { this.id = id; this.label = label; }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private String activeId;
    private Consumer<String> onSwitch;
    private final AnimatedValue underline = new AnimatedValue(0f);
    private float tabW;

    public ZenithTabBar addTab(String id, String label) { tabs.add(new Tab(id, label)); if (activeId == null) activeId = id; return this; }
    public ZenithTabBar onSwitch(Consumer<String> cb) { this.onSwitch = cb; return this; }
    public String active() { return activeId; }

    public void setActive(String id) {
        this.activeId = id;
    }

    @Override
    public void render(GuiDrawContext ctx, int mx, int my, float td) {
        var t = ThemeManager.getInstance().current();
        tabW = width / Math.max(1, tabs.size());
        // Background strip
        ctx.fillRect(x, y, width, height - 2, new Color4f(0.1f,0.1f,0.12f,0.6f));
        // Tabs
        for (int i = 0; i < tabs.size(); i++) {
            Tab tb = tabs.get(i);
            float tx = x + i*tabW;
            boolean hovered = mx >= tx && mx <= tx+tabW && my >= y && my <= y+height;
            Color4f textCol = tb.id.equals(activeId) ? t.accent : (hovered ? t.textPrimary : t.textSecondary);
            float w = ctx.stringWidth(tb.label);
            ctx.drawString(tb.label, tx + (tabW-w)/2f, y + (height-10)/2f - 1, textCol, true);
        }
        // Underline for active
        int idx = 0;
        for (int i = 0; i < tabs.size(); i++) if (tabs.get(i).id.equals(activeId)) idx = i;
        float target = idx * tabW;
        underline.animateTo(target, 200, EasingType.EASE_OUT);
        float ux = x + underline.get();
        ctx.fillRect(ux+8, y + height-2, tabW-16, 2, t.accent);
    }

    @Override
    public void mouseClicked(int mx, int my, int b) {
        if (b != 0) return;
        for (int i = 0; i < tabs.size(); i++) {
            float tx = x + i*tabW;
            if (mx >= tx && mx <= tx+tabW && my >= y && my <= y+height) {
                activeId = tabs.get(i).id;
                if (onSwitch != null) onSwitch.accept(activeId);
                break;
            }
        }
    }
}
