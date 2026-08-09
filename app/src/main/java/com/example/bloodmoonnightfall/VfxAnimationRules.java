package com.example.bloodmoonnightfall;

/** Pure timing rules for the four-frame painted blood-art animations. */
public final class VfxAnimationRules {
    public static final int FRAME_COUNT = 4;

    private VfxAnimationRules() {
    }

    /**
     * Returns a monotonic frame position from 0 to 3.
     * The peak-impact frame is deliberately held so fast skills remain readable.
     */
    public static float framePosition(float progress) {
        float safe = RpgRules.clamp(progress, 0f, 1f);
        if (safe < 0.10f) {
            return 0f;
        }
        if (safe < 0.24f) {
            return smooth((safe - 0.10f) / 0.14f);
        }
        if (safe < 0.40f) {
            return 1f + smooth((safe - 0.24f) / 0.16f);
        }
        if (safe < 0.64f) {
            return 2f;
        }
        if (safe < 0.82f) {
            return 2f + smooth((safe - 0.64f) / 0.18f);
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

    private static float smooth(float value) {
        float safe = RpgRules.clamp(value, 0f, 1f);
        return safe * safe * (3f - 2f * safe);
    }
}
