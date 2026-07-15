package com.zenith.client.core.module.settings;

import com.zenith.client.core.util.ColorUtils;

/** ARGB colour setting (packed int). */
public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, String description, int defaultValueARGB) {
        super(name, description, defaultValueARGB);
    }

    public int getRed()   { return ColorUtils.red(getValue()); }
    public int getGreen() { return ColorUtils.green(getValue()); }
    public int getBlue()  { return ColorUtils.blue(getValue()); }
    public int getAlpha() { return ColorUtils.alpha(getValue()); }

    public void setARGB(int r, int g, int b, int a) {
        setValue(ColorUtils.toARGB(r, g, b, a));
    }

    @Override public Object serialize() { return String.format("#%08X", getValue()); }
    @Override public void deserialize(Object raw) {
        if (raw instanceof String s) setValue(ColorUtils.parseHex(s));
        else if (raw instanceof Number n) setValue(n.intValue());
    }
    @Override public void reset() { setValue(0xFFFFFFFF); }
}
