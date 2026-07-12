package com.zenith.client.engine.render;

/** Lightweight RGBA float colour record. */
public record Color4f(float r, float g, float b, float a) {
    public static Color4f WHITE = new Color4f(1,1,1,1);
    public static Color4f BLACK = new Color4f(0,0,0,1);
    public static Color4f TRANSPARENT = new Color4f(0,0,0,0);

    public Color4f multiply(float f) { return new Color4f(r*f, g*f, b*f, a); }
    public Color4f withAlpha(float alpha) { return new Color4f(r,g,b,alpha); }
    public Color4f withRed(float nr) { return new Color4f(nr,g,b,a); }
    public Color4f withGreen(float ng) { return new Color4f(r,ng,b,a); }
    public Color4f withBlue(float nb) { return new Color4f(r,g,nb,a); }

    public int toARGB() {
        int a = (int)(this.a * 255) & 0xFF;
        int r = (int)(this.r * 255) & 0xFF;
        int g = (int)(this.g * 255) & 0xFF;
        int b = (int)(this.b * 255) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
