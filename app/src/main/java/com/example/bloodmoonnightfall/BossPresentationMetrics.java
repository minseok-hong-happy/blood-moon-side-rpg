package com.example.bloodmoonnightfall;

/** Single source of truth for the boss silhouette and its attached combat UI. */
final class BossPresentationMetrics {
    private static final float[] SPRITE_WIDTHS = {318f, 344f, 326f};
    private static final float[] SPRITE_HEIGHTS = {286f, 292f, 296f};
    private static final float[] HEALTH_BAR_WIDTHS = {232f, 252f, 240f};
    private static final float[] READABILITY_GLOW_RADII = {142f, 154f, 148f};
    private static final float[] SHADOW_HALF_WIDTHS = {68f, 76f, 72f};
    private static final float HEALTH_BAR_GAP = 28f;

    private BossPresentationMetrics() {
    }

    static float spriteWidth() {
        return spriteWidth(0);
    }

    static float spriteWidth(int variant) {
        return SPRITE_WIDTHS[safeVariant(variant)];
    }

    static float spriteHeight() {
        return spriteHeight(0);
    }

    static float spriteHeight(int variant) {
        return SPRITE_HEIGHTS[safeVariant(variant)];
    }

    static float torsoY(float groundY) {
        return torsoY(groundY, 0);
    }

    static float torsoY(float groundY, int variant) {
        return groundY - spriteHeight(variant) * 0.50f;
    }

    static float healthBarWidth() {
        return healthBarWidth(0);
    }

    static float healthBarWidth(int variant) {
        return HEALTH_BAR_WIDTHS[safeVariant(variant)];
    }

    static float healthBarY(float groundY) {
        return healthBarY(groundY, 0);
    }

    static float healthBarY(float groundY, int variant) {
        return groundY - spriteHeight(variant) - HEALTH_BAR_GAP;
    }

    static float readabilityGlowRadius() {
        return readabilityGlowRadius(0);
    }

    static float readabilityGlowRadius(int variant) {
        return READABILITY_GLOW_RADII[safeVariant(variant)];
    }

    static float shadowHalfWidth() {
        return shadowHalfWidth(0);
    }

    static float shadowHalfWidth(int variant) {
        return SHADOW_HALF_WIDTHS[safeVariant(variant)];
    }

    static float phaseAuraRadius(float pulse) {
        return phaseAuraRadius(0, pulse);
    }

    static float phaseAuraRadius(int variant, float pulse) {
        return readabilityGlowRadius(variant) - 12f + pulse * 16f;
    }

    static float telegraphRadius() {
        return telegraphRadius(0);
    }

    static float telegraphRadius(int variant) {
        return 118f + safeVariant(variant) * 4f;
    }

    static float enemySpacing(int firstKind, int secondKind) {
        return firstKind == RpgRules.ENEMY_BOSS || secondKind == RpgRules.ENEMY_BOSS
                ? 82f : 44f;
    }

    static float heroSpacing(int enemyKind) {
        return enemyKind == RpgRules.ENEMY_BOSS ? 74f : 58f;
    }

    private static int safeVariant(int variant) {
        return Math.max(0, Math.min(SPRITE_WIDTHS.length - 1, variant));
    }
}
