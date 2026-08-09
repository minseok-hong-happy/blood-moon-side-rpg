package com.example.bloodmoonnightfall;

/** Single source of truth for the boss silhouette and its attached combat UI. */
final class BossPresentationMetrics {
    static final float SCALE = 1.30f;

    private static final float BASE_SPRITE_WIDTH = 220f;
    private static final float BASE_SPRITE_HEIGHT = 186f;
    private static final float HEALTH_BAR_GAP = 18f;

    private BossPresentationMetrics() {
    }

    static float spriteWidth() {
        return BASE_SPRITE_WIDTH * SCALE;
    }

    static float spriteHeight() {
        return BASE_SPRITE_HEIGHT * SCALE;
    }

    static float torsoY(float groundY) {
        return groundY - spriteHeight() * 0.50f;
    }

    static float healthBarWidth() {
        return 216f;
    }

    static float healthBarY(float groundY) {
        return groundY - spriteHeight() - HEALTH_BAR_GAP;
    }

    static float readabilityGlowRadius() {
        return 128f;
    }

    static float shadowHalfWidth() {
        return 62f;
    }

    static float phaseAuraRadius(float pulse) {
        return 126f + pulse * 14f;
    }

    static float telegraphRadius() {
        return 112f;
    }

    static float enemySpacing(int firstKind, int secondKind) {
        return firstKind == RpgRules.ENEMY_BOSS || secondKind == RpgRules.ENEMY_BOSS
                ? 82f : 44f;
    }

    static float heroSpacing(int enemyKind) {
        return enemyKind == RpgRules.ENEMY_BOSS ? 74f : 58f;
    }
}
