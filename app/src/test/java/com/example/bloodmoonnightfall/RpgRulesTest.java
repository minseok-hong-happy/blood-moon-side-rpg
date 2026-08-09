package com.example.bloodmoonnightfall;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class RpgRulesTest {
    @Test
    public void experienceCurveAlwaysIncreases() {
        int previous = 0;
        for (int level = 1; level <= RpgRules.LEVEL_CAP; level++) {
            int next = RpgRules.xpForNextLevel(level);
            assertTrue(next > previous);
            previous = next;
        }
    }

    @Test
    public void growthStatsIncreaseHeroPower() {
        assertTrue(RpgRules.heroMaxHealth(10, 4) > RpgRules.heroMaxHealth(1, 0));
        assertTrue(RpgRules.heroMaxBlood(10, 4, 8) > RpgRules.heroMaxBlood(1, 0, 0));
        assertTrue(RpgRules.heroAttackPower(10, 4, 8) > RpgRules.heroAttackPower(1, 0, 0));
    }

    @Test
    public void comboFinisherIsStrongest() {
        int attack = 30;
        assertTrue(RpgRules.meleeDamage(attack, 2) > RpgRules.meleeDamage(attack, 1));
        assertTrue(RpgRules.meleeDamage(attack, 1) > RpgRules.meleeDamage(attack, 0));
    }

    @Test
    public void automaticBloodArtsScaleWithGrowth() {
        int attack = 30;
        assertTrue(RpgRules.rushDamage(attack, 20) > RpgRules.rushDamage(attack, 3));
        assertTrue(RpgRules.bladeRainDamage(attack, 20)
                > RpgRules.bladeRainDamage(attack, 6));
        assertTrue(RpgRules.rushDamage(attack, 3) > attack);
        assertTrue(RpgRules.bladeRainDamage(attack, 6) > attack);
    }

    @Test
    public void armorNeverCreatesZeroDamageFromAHit() {
        assertEquals(0, RpgRules.mitigateDamage(0, 300));
        assertTrue(RpgRules.mitigateDamage(5, 300) >= 1);
        assertTrue(RpgRules.mitigateDamage(20, 20) < 20);
    }

    @Test
    public void wavesProvideDenseEnemyGroupsAndBossEscorts() {
        assertTrue(RpgRules.waveEnemyCount(0, 1) >= 8);
        assertTrue(RpgRules.waveEnemyCount(2, 4) >= 18);
        assertEquals(5, RpgRules.waveEnemyCount(2, RpgRules.WAVES_PER_REGION));
    }

    @Test
    public void lateEnemiesAreStrongerAndMoreRewarding() {
        assertTrue(RpgRules.enemyMaxHealth(RpgRules.ENEMY_THRALL, 2, 4, 10, 0)
                > RpgRules.enemyMaxHealth(RpgRules.ENEMY_THRALL, 0, 1, 1, 0));
        assertTrue(RpgRules.enemyDamage(RpgRules.ENEMY_HUNTER, 2, 4, 10, 0, false)
                > RpgRules.enemyDamage(RpgRules.ENEMY_HUNTER, 0, 1, 1, 0, false));
        assertTrue(RpgRules.xpReward(RpgRules.ENEMY_BOSS, 2, 5, 1)
                > RpgRules.xpReward(RpgRules.ENEMY_THRALL, 0, 1, 0));
    }

    @Test
    public void equipmentPowerIsBounded() {
        assertEquals(300, RpgRules.equipmentPower(2, 5, 60, 3, 1000));
        assertTrue(RpgRules.equipmentPower(0, 1, 1, 0, 0) >= 1);
    }

    @Test
    public void offlineElapsedTimeIsCappedAtEightHours() {
        long now = 1_000_000L;
        assertEquals(RpgRules.OFFLINE_REWARD_CAP_SECONDS,
                RpgRules.offlineElapsedSeconds(now - 24L * 60L * 60L, now));
    }

    @Test
    public void offlineElapsedTimeRejectsMissingAndBackwardClocks() {
        assertEquals(0, RpgRules.offlineElapsedSeconds(0L, 100L));
        assertEquals(0, RpgRules.offlineElapsedSeconds(200L, 100L));
    }

    @Test
    public void offlineRewardsRequireOneFullMinute() {
        assertEquals(0, RpgRules.offlineGoldReward(59, 1, 0, 1));
        assertEquals(0, RpgRules.offlineXpReward(59, 1, 0, 1));
    }

    @Test
    public void offlineRewardsScaleWithAdventureProgress() {
        int elapsed = 60 * 60;
        assertTrue(RpgRules.offlineGoldReward(elapsed, 30, 2, 5)
                > RpgRules.offlineGoldReward(elapsed, 1, 0, 1));
        assertTrue(RpgRules.offlineXpReward(elapsed, 30, 2, 5)
                > RpgRules.offlineXpReward(elapsed, 1, 0, 1));
    }
}
