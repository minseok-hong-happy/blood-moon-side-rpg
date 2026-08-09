package com.example.bloodmoonnightfall;

/** Pure timing and hierarchy rules for the painted four-frame contact effects. */
public final class ImpactAnimationRules {
    public static final int FRAME_COUNT = 4;
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_HEAVY = 1;
    public static final int STYLE_FINISHER = 2;

    private ImpactAnimationRules() {
    }

    public static float duration(int style) {
        int safe = RpgRules.clamp(style, STYLE_NORMAL, STYLE_FINISHER);
        return safe == STYLE_FINISHER ? 0.48f : safe == STYLE_HEAVY ? 0.36f : 0.27f;
    }

    public static float size(int style) {
        int safe = RpgRules.clamp(style, STYLE_NORMAL, STYLE_FINISHER);
        return safe == STYLE_FINISHER ? 310f : safe == STYLE_HEAVY ? 220f : 138f;
    }

    /** Holds the contact frame before moving into debris and afterimage frames. */
    public static float framePosition(float progress) {
        float safe = RpgRules.clamp(progress, 0f, 1f);
        if (safe < 0.08f) {
            return 0f;
        }
        if (safe < 0.18f) {
            return smooth((safe - 0.08f) / 0.10f);
        }
        if (safe < 0.38f) {
            return 1f;
        }
        if (safe < 0.62f) {
            return 1f + smooth((safe - 0.38f) / 0.24f);
        }
        if (safe < 0.76f) {
            return 2f;
        }
        if (safe < 0.90f) {
            return 2f + smooth((safe - 0.76f) / 0.14f);
        }
        return 3f;
    }

    public static int firstFrame(float progress) {
        return Math.min(FRAME_COUNT - 1, (int) Math.floor(framePosition(progress)));
    }

    public static int secondFrame(float progress) {
        return Math.min(FRAME_COUNT - 1, firstFrame(progress) + 1);
    }

    public static float frameBlend(float progress) {
        float position = framePosition(progress);
        return position - (float) Math.floor(position);
    }

    public static float visualScale(float progress) {
        float safe = RpgRules.clamp(progress, 0f, 1f);
        if (safe < 0.18f) {
            return 0.72f + smooth(safe / 0.18f) * 0.40f;
        }
        return 1.12f - smooth((safe - 0.18f) / 0.82f) * 0.08f;
    }

    public static float alpha(float progress) {
        float safe = RpgRules.clamp(progress, 0f, 1f);
        return safe < 0.70f ? 1f : 1f - smooth((safe - 0.70f) / 0.30f);
    }

    private static float smooth(float value) {
        float safe = RpgRules.clamp(value, 0f, 1f);
        return safe * safe * (3f - 2f * safe);
    }
}
