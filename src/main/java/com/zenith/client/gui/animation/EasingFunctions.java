package com.zenith.client.gui.animation;

/** Standard easing functions, all take t in [0,1] and return eased value in [0,1]. */
public final class EasingFunctions {

    private EasingFunctions() {}

    public static float ease(EasingType type, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return switch (type) {
            case LINEAR -> t;
            case EASE_IN -> t * t * t;
            case EASE_OUT -> 1f - (float) Math.pow(1 - t, 3);
            case EASE_IN_OUT -> t < 0.5f ? 4f*t*t*t : 1f - (float) Math.pow(-2*t+2, 3)/2f;
            case ELASTIC_OUT -> {
                if (t == 0f || t == 1f) yield t;
                float c4 = (2f * (float) Math.PI) / 3f;
                yield (float) Math.pow(2f, -10f*t) * (float) Math.sin((t*10f - 0.75f) * c4) + 1f;
            }
            case BACK_OUT -> {
                float c1 = 1.70158f, c3 = c1 + 1f;
                yield 1f + c3*(t-1f)*(t-1f)*(t-1f) + c1*(t-1f)*(t-1f);
            }
            case BOUNCE_OUT -> {
                float n1 = 7.5625f, d1 = 2.75f;
                if (t < 1f/d1) yield n1*t*t;
                else if (t < 2f/d1) { t-=1.5f/d1; yield n1*t*t + 0.75f; }
                else if (t < 2.5f/d1) { t-=2.25f/d1; yield n1*t*t + 0.9375f; }
                else { t-=2.625f/d1; yield n1*t*t + 0.984375f; }
            }
        };
    }
}
