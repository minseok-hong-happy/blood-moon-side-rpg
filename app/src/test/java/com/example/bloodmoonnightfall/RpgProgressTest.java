package com.example.bloodmoonnightfall;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.CRC32;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public final class RpgProgressTest {
    @Test
    public void roundTripKeepsAllGrowthFields() {
        RpgProgress source = RpgProgress.fresh();
        source.revision = 27;
        source.level = 11;
        source.xp = 412;
        source.gold = 1880;
        source.region = 2;
        source.wave = 4;
        source.kills = 153;
        source.bossKills = 5;
        source.chapterClears = 1;
        source.vitalityLevel = 4;
        source.mightLevel = 5;
        source.bloodLevel = 3;
        source.recoveryLevel = 2;
        source.spearLevel = 4;
        source.siphonLevel = 3;
        source.novaLevel = 2;
        source.weaponPower = 19;
        source.armorPower = 16;
        source.relicPower = 14;

        RpgProgress decoded = RpgProgress.decode(source.encode());

        assertEquals(source.encode(), decoded.encode());
        assertEquals(153, decoded.kills);
        assertEquals(19, decoded.weaponPower);
    }

    @Test
    public void tamperedOrMalformedSavesAreRejected() {
        String encoded = RpgProgress.fresh().encode();
        assertNull(RpgProgress.decode(encoded.replace("|1|", "|9|")));
        assertNull(RpgProgress.decode("R3|broken"));
        assertNull(RpgProgress.decode(null));
    }

    @Test
    public void outOfRangeDataIsRejectedEvenWithValidChecksum() {
        RpgProgress source = RpgProgress.fresh();
        source.region = 99;
        assertNull(RpgProgress.decode(source.encode()));
    }

    @Test
    public void unlockNormalizationNeverRelocksEarnedSkills() {
        RpgProgress source = RpgProgress.fresh();
        source.level = 7;
        source.siphonLevel = 0;
        source.novaLevel = 0;
        source.normalizeUnlocks();
        assertEquals(1, source.siphonLevel);
        assertEquals(1, source.novaLevel);
    }

    @Test
    public void newestValidPrefersHigherRevisionAndSurvivesMissingSlot() {
        RpgProgress first = RpgProgress.fresh();
        first.revision = 3;
        RpgProgress second = RpgProgress.fresh();
        second.revision = 8;
        assertSame(second, RpgProgressStore.newestValid(first, second));
        assertSame(first, RpgProgressStore.newestValid(first, null));
        assertTrue(RpgProgressStore.newestValid(null, null) == null);
    }

    @Test
    public void versionTwoDuelSaveMigratesPermanentGrowth() {
        String older = legacyDuel(2, 1, 1, 2, 1, 0, 2, 0);
        String newer = legacyDuel(7, 3, 4, 5, 3, 2, 6, 1);

        RpgProgress migrated = RpgProgressStore.migrateLegacyDuel(older, newer);

        assertEquals(11, migrated.level);
        assertEquals(1, migrated.region);
        assertEquals(4, migrated.wave);
        assertEquals(4, migrated.vitalityLevel);
        assertEquals(5, migrated.mightLevel);
        assertEquals(36, migrated.kills);
        assertEquals(1, migrated.chapterClears);
        assertTrue(migrated.siphonLevel >= 1);
        assertTrue(migrated.novaLevel >= 1);
    }

    @Test
    public void versionOneWorldSaveMigratesExplorationAndEquipment() {
        String world = legacyWorld(9, "checkpoint.ashwood", 140, 136,
                "sigil.chapel,sigil.altar", "chest.belfry,chest.canopy",
                "enemy.1,enemy.2,enemy.3,enemy.4", false);

        RpgProgress migrated = RpgProgressStore.migrateLegacyWorld(null, world);

        assertEquals(1, migrated.region);
        assertEquals(2, migrated.wave);
        assertEquals(4, migrated.kills);
        assertEquals(2, migrated.vitalityLevel);
        assertEquals(2, migrated.bloodLevel);
        assertTrue(migrated.gold > 0);
        assertTrue(migrated.weaponPower > 0);
    }

    @Test
    public void corruptLegacySaveIsNeverImported() {
        String duel = legacyDuel(4, 2, 2, 2, 2, 2, 3, 0);
        String world = legacyWorld(4, "checkpoint.keep", 140, 130,
                "sigil.chapel", "chest.belfry", "enemy.1", false);
        assertNull(RpgProgressStore.migrateLegacyDuel(duel + "x", null));
        assertNull(RpgProgressStore.migrateLegacyWorld(world.replace("checkpoint", "broken"), null));
    }

    @Test
    public void storagePreparationBoundsLongRunningCounters() {
        RpgProgress progress = RpgProgress.fresh();
        progress.level = 99;
        progress.xp = Integer.MAX_VALUE;
        progress.gold = Integer.MAX_VALUE;
        progress.kills = Integer.MAX_VALUE;
        progress.weaponPower = 999;
        progress.spearLevel = 99;

        progress.prepareForStore();

        assertEquals(RpgRules.LEVEL_CAP, progress.level);
        assertEquals(0, progress.xp);
        assertEquals(100_000_000, progress.gold);
        assertEquals(100_000_000, progress.kills);
        assertEquals(300, progress.weaponPower);
        assertEquals(12, progress.spearLevel);
        assertTrue(RpgProgress.decode(progress.encode()) != null);
    }

    private static String legacyDuel(long revision, int duelIndex, int health, int damage,
                                     int blood, int regen, int victories, int clears) {
        String payload = "D2|" + revision + "|" + duelIndex + "|" + health + "|"
                + damage + "|" + blood + "|" + regen + "|" + victories + "|" + clears;
        CRC32 crc = new CRC32();
        crc.update(payload.getBytes(StandardCharsets.UTF_8));
        return payload + "#" + String.format(Locale.US, "%08X", crc.getValue());
    }

    private static String legacyWorld(long revision, String checkpoint, int maxHealth,
                                      int maxBlood, String sigils, String chests,
                                      String enemies, boolean bossDefeated) {
        String payload = "1|" + revision + "|" + checkpoint
                + "|3200.00|590.00|100|" + maxHealth + "|55|" + maxBlood
                + "|" + sigils + "|" + chests + "|" + enemies
                + "|" + (bossDefeated ? "1" : "0");
        CRC32 crc = new CRC32();
        crc.update(payload.getBytes(StandardCharsets.UTF_8));
        return payload + "#" + Long.toHexString(crc.getValue());
    }
}
