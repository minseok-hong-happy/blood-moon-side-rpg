package com.example.bloodmoonnightfall;

/** Pure progression and combat formulas shared by the game view and JVM tests. */
public final class RpgRules {
    public static final float ARENA_LEFT = 54f;
    public static final float ARENA_RIGHT = 666f;
    public static final int REGION_COUNT = 3;
    public static final int WAVES_PER_REGION = 5;
    public static final int LEVEL_CAP = 60;
    public static final int UPGRADE_CAP = 30;
    public static final int OFFLINE_REWARD_CAP_SECONDS = 8 * 60 * 60;

    public static final int ENEMY_THRALL = 0;
    public static final int ENEMY_HUNTER = 1;
    public static final int ENEMY_WRAITH = 2;
    public static final int ENEMY_BOSS = 3;

    private RpgRules() {
    }

    public static int xpForNextLevel(int level) {
        int safe = clamp(level, 1, LEVEL_CAP);
        return 70 + safe * 42 + safe * safe * 6;
    }

    public static int statUpgradeCost(int currentLevel) {
        int safe = clamp(currentLevel, 0, UPGRADE_CAP);
        return 65 + safe * 48 + safe * safe * 9;
    }

    public static int skillUpgradeCost(int currentLevel) {
        int safe = clamp(currentLevel, 0, 12);
        return 105 + safe * 90 + safe * safe * 15;
    }

    public static int heroMaxHealth(int level, int vitalityLevel) {
        return 118 + (clamp(level, 1, LEVEL_CAP) - 1) * 9
                + clamp(vitalityLevel, 0, UPGRADE_CAP) * 24;
    }

    public static int heroMaxBlood(int level, int bloodLevel, int relicPower) {
        return 100 + (clamp(level, 1, LEVEL_CAP) - 1) * 3
                + clamp(bloodLevel, 0, UPGRADE_CAP) * 15
                + clamp(relicPower, 0, 300) * 2;
    }

    public static int heroAttackPower(int level, int mightLevel, int weaponPower) {
        return 17 + (clamp(level, 1, LEVEL_CAP) - 1) * 2
                + clamp(mightLevel, 0, UPGRADE_CAP) * 5
                + clamp(weaponPower, 0, 300);
    }

    public static int meleeDamage(int attackPower, int comboIndex) {
        int safeCombo = clamp(comboIndex, 0, 2);
        float[] multiplier = {1f, 1.18f, 1.55f};
        return Math.max(1, Math.round(Math.max(1, attackPower) * multiplier[safeCombo]));
    }

    public static int spearDamage(int attackPower, int skillLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.45f + clamp(skillLevel, 1, 12) * 0.13f)));
    }

    public static int siphonDamage(int attackPower, int skillLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.18f + clamp(skillLevel, 1, 12) * 0.11f)));
    }

    public static int novaDamage(int attackPower, int skillLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.55f + clamp(skillLevel, 1, 12) * 0.16f)));
    }

    public static int rushDamage(int attackPower, int heroLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.28f + clamp(heroLevel, 3, LEVEL_CAP) * 0.018f)));
    }

    public static int bladeRainDamage(int attackPower, int heroLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.08f + clamp(heroLevel, 6, LEVEL_CAP) * 0.014f)));
    }

    public static int bloodChainDamage(int attackPower, int heroLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.18f + clamp(heroLevel, 9, LEVEL_CAP) * 0.016f)));
    }

    public static int crimsonPillarDamage(int attackPower, int heroLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (1.48f + clamp(heroLevel, 12, LEVEL_CAP) * 0.019f)));
    }

    public static int eclipseDamage(int attackPower, int heroLevel) {
        return Math.max(1, Math.round(Math.max(1, attackPower)
                * (2.15f + clamp(heroLevel, 15, LEVEL_CAP) * 0.024f)));
    }

    public static float recoveryPerSecond(int recoveryLevel) {
        return 0.65f + clamp(recoveryLevel, 0, UPGRADE_CAP) * 0.22f;
    }

    public static int mitigateDamage(int rawDamage, int armorPower) {
        int safeRaw = Math.max(0, rawDamage);
        int safeArmor = clamp(armorPower, 0, 300);
        return Math.max(safeRaw == 0 ? 0 : 1,
                Math.round(safeRaw * (100f / (100f + safeArmor * 4f))));
    }

    public static int waveEnemyCount(int region, int wave) {
        int safeRegion = clamp(region, 0, REGION_COUNT - 1);
        int safeWave = clamp(wave, 1, WAVES_PER_REGION);
        return safeWave == WAVES_PER_REGION ? 3 + safeRegion
                : 6 + safeWave * 2 + safeRegion * 2;
    }

    public static int enemyMaxHealth(int kind, int region, int wave, int heroLevel,
                                     int chapterClears) {
        int[] bases = {54, 72, 86, 320};
        int safeKind = clamp(kind, ENEMY_THRALL, ENEMY_BOSS);
        float scale = 1f + clamp(region, 0, REGION_COUNT - 1) * 0.48f
                + (clamp(wave, 1, WAVES_PER_REGION) - 1) * 0.14f
                + (clamp(heroLevel, 1, LEVEL_CAP) - 1) * 0.045f
                + clamp(chapterClears, 0, 1000) * 0.28f;
        return Math.max(1, Math.round(bases[safeKind] * scale));
    }

    public static int enemyDamage(int kind, int region, int wave, int heroLevel,
                                  int chapterClears, boolean heavy) {
        int[] bases = {7, 9, 11, 16};
        int safeKind = clamp(kind, ENEMY_THRALL, ENEMY_BOSS);
        float scale = 1f + clamp(region, 0, REGION_COUNT - 1) * 0.24f
                + (clamp(wave, 1, WAVES_PER_REGION) - 1) * 0.07f
                + (clamp(heroLevel, 1, LEVEL_CAP) - 1) * 0.018f
                + clamp(chapterClears, 0, 1000) * 0.13f;
        if (heavy) {
            scale *= 1.55f;
        }
        return Math.max(1, Math.round(bases[safeKind] * scale));
    }

    public static int xpReward(int kind, int region, int wave, int chapterClears) {
        int safeKind = clamp(kind, ENEMY_THRALL, ENEMY_BOSS);
        int base = safeKind == ENEMY_BOSS ? 165 : 18 + safeKind * 5;
        return Math.max(1, Math.round(base * (1f + clamp(region, 0, 2) * 0.32f
                + (clamp(wave, 1, 5) - 1) * 0.08f
                + clamp(chapterClears, 0, 1000) * 0.12f)));
    }

    public static int goldReward(int kind, int region, int wave, int chapterClears) {
        int safeKind = clamp(kind, ENEMY_THRALL, ENEMY_BOSS);
        int base = safeKind == ENEMY_BOSS ? 145 : 12 + safeKind * 4;
        return Math.max(1, Math.round(base * (1f + clamp(region, 0, 2) * 0.28f
                + (clamp(wave, 1, 5) - 1) * 0.06f
                + clamp(chapterClears, 0, 1000) * 0.10f)));
    }

    public static int equipmentPower(int region, int wave, int heroLevel,
                                     int rarity, int chapterClears) {
        int safeRarity = clamp(rarity, 0, 3);
        return clamp(2 + region * 5 + wave * 2 + heroLevel / 2
                + safeRarity * (4 + region * 2) + chapterClears * 3, 1, 300);
    }

    public static int offlineElapsedSeconds(long lastActiveEpochSeconds,
                                            long nowEpochSeconds) {
        if (lastActiveEpochSeconds <= 0L || nowEpochSeconds <= lastActiveEpochSeconds) {
            return 0;
        }
        long elapsed = Math.min(nowEpochSeconds - lastActiveEpochSeconds,
                OFFLINE_REWARD_CAP_SECONDS);
        return (int) elapsed;
    }

    public static int offlineGoldReward(int elapsedSeconds, int level, int region, int wave) {
        int minutes = clamp(elapsedSeconds, 0, OFFLINE_REWARD_CAP_SECONDS) / 60;
        long perMinute = 10L + clamp(level, 1, LEVEL_CAP) * 2L
                + clamp(region, 0, REGION_COUNT - 1) * 8L
                + clamp(wave, 1, WAVES_PER_REGION) * 3L;
        return (int) Math.min(2_000_000L, minutes * perMinute);
    }

    public static int offlineXpReward(int elapsedSeconds, int level, int region, int wave) {
        int minutes = clamp(elapsedSeconds, 0, OFFLINE_REWARD_CAP_SECONDS) / 60;
        long perMinute = 3L + clamp(level, 1, LEVEL_CAP) / 2L
                + clamp(region, 0, REGION_COUNT - 1) * 3L
                + clamp(wave, 1, WAVES_PER_REGION);
        return (int) Math.min(1_000_000L, minutes * perMinute);
    }

    public static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
