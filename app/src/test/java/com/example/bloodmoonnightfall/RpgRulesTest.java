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
        assertTrue(RpgRules.bloodChainDamage(attack, 20)
                > RpgRules.bloodChainDamage(attack, 9));
        assertTrue(RpgRules.crimsonPillarDamage(attack, 20)
                > RpgRules.crimsonPillarDamage(attack, 12));
        assertTrue(RpgRules.eclipseDamage(attack, 20)
                > RpgRules.eclipseDamage(attack, 15));
        assertTrue(RpgRules.eclipseDamage(attack, 15)
                > RpgRules.crimsonPillarDamage(attack, 15));
    }

    @Test
    public void armorNeverCreatesZeroDamageFromAHit() {
        assertEquals(0, RpgRules.mitigateDamage(0, 300));
        assertTrue(RpgRules.mitigateDamage(5, 300) >= 1);
        assertTrue(RpgRules.mitigateDamage(20, 20) < 20);
    }

    @Test
    public void wavesProvideDenseEnemyGroupsAndBossEscorts() {
        assertEquals(32, RpgRules.waveEnemyCount(0, 1));
        assertEquals(60, RpgRules.waveEnemyCount(2, 4));
        assertEquals(26, RpgRules.waveEnemyCount(2, RpgRules.WAVES_PER_REGION));
    }

    @Test
    public void reinforcementsArriveInGroupsWhenTheFieldIsSparse() {
        assertEquals(4, RpgRules.reinforcementBatchSize(0, 20, 12));
        assertEquals(4, RpgRules.reinforcementBatchSize(4, 20, 12));
        assertEquals(2, RpgRules.reinforcementBatchSize(7, 20, 12));
        assertEquals(1, RpgRules.reinforcementBatchSize(11, 20, 12));
        assertEquals(0, RpgRules.reinforcementBatchSize(12, 20, 12));
        assertEquals(2, RpgRules.reinforcementBatchSize(1, 2, 10));
    }

    @Test
    public void eachLongNormalWaveHasOneMidpointElite() {
        int total = RpgRules.waveEnemyCount(1, 3);
        int elites = 0;
        for (int serial = 0; serial < total; serial++) {
            if (RpgRules.isEliteSpawn(3, serial, total)) {
                elites++;
            }
        }
        assertEquals(1, elites);
        assertTrue(!RpgRules.isEliteSpawn(1, total / 2, total));
        assertTrue(!RpgRules.isEliteSpawn(RpgRules.WAVES_PER_REGION,
                total / 2, total));
    }

    @Test
    public void eliteAndHuntChainRewardsAreMeaningful() {
        assertTrue(RpgRules.eliteHealth(100) > 200);
        assertTrue(RpgRules.eliteDamage(10) > 10);
        assertTrue(RpgRules.eliteReward(10) > 20);
        assertEquals(0, RpgRules.huntChainBonusGold(4, 12));
        assertTrue(RpgRules.huntChainBonusGold(5, 12) >= 20);
        assertTrue(RpgRules.huntChainBonusGold(10, 12)
                > RpgRules.huntChainBonusGold(5, 12));
    }

    @Test
    public void everyRegionOwnsADifferentBossVariant() {
        assertEquals(0, RpgRules.bossVariantForRegion(0));
        assertEquals(1, RpgRules.bossVariantForRegion(1));
        assertEquals(2, RpgRules.bossVariantForRegion(2));
    }

    @Test
    public void bossesKeepACommandingSilhouetteAndClearAttachedUi() {
        float groundY = 866f;
        for (int variant = 0; variant < 3; variant++) {
            float width = BossPresentationMetrics.spriteWidth(variant);
            float height = BossPresentationMetrics.spriteHeight(variant);
            float spriteTop = groundY - height + 13f;
            assertTrue(width >= 318f);
            assertTrue(height >= 286f);
            assertTrue(BossPresentationMetrics.healthBarWidth(variant) < width);
            assertTrue(BossPresentationMetrics.healthBarY(groundY, variant)
                    <= spriteTop - 40f);
            assertTrue(BossPresentationMetrics.readabilityGlowRadius(variant) >= 142f);
        }
        assertTrue(BossPresentationMetrics.spriteWidth(1)
                > BossPresentationMetrics.spriteWidth(0));
        assertTrue(BossPresentationMetrics.spriteHeight(2)
                > BossPresentationMetrics.spriteHeight(0));
        assertTrue(BossPresentationMetrics.enemySpacing(RpgRules.ENEMY_BOSS,
                RpgRules.ENEMY_THRALL)
                > BossPresentationMetrics.enemySpacing(RpgRules.ENEMY_THRALL,
                RpgRules.ENEMY_HUNTER));
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

    @Test
    public void combatRecenteringStartsOnlyPastTheCenterLine() {
        assertEquals(0f, RpgRules.combatRecenteringShift(340f, 1f / 60f), 0.001f);
        assertTrue(RpgRules.combatRecenteringShift(410f, 1f / 60f) > 0f);
    }

    @Test
    public void combatRecenteringIsFrameRateBounded() {
        float shift = RpgRules.combatRecenteringShift(620f, 1f / 60f);
        assertTrue(shift <= 620f / 60f + 0.001f);
        assertTrue(620f - shift >= RpgRules.COMBAT_ANCHOR_X);
    }

    @Test
    public void portraitControlRowsNeverOverlap() {
        assertTrue(GameUiLayout.autoToManualRowGap()
                >= GameUiLayout.MIN_CONTROL_ROW_GAP);
        assertTrue(GameUiLayout.portraitControlsAreSeparatedAndVisible());
    }

    @Test
    public void bloodArtAnimationAdvancesAndHoldsPeakFrame() {
        float previous = -1f;
        for (int sample = 0; sample <= 100; sample++) {
            float position = VfxAnimationRules.framePosition(sample / 100f);
            assertTrue(position >= previous);
            previous = position;
        }
        assertEquals(2f, VfxAnimationRules.framePosition(0.50f), 0.001f);
        assertEquals(3, VfxAnimationRules.firstFrame(1f));
        assertEquals(3, VfxAnimationRules.secondFrame(1f));
    }
}
