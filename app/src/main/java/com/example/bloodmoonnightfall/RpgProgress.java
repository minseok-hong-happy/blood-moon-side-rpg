package com.example.bloodmoonnightfall;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.CRC32;

/** Versioned, checksummed permanent RPG progress. Invalid or torn data is rejected. */
public final class RpgProgress {
    private static final String VERSION = "R3";

    public long revision;
    public int level;
    public int xp;
    public int gold;
    public int region;
    public int wave;
    public int kills;
    public int bossKills;
    public int chapterClears;
    public int vitalityLevel;
    public int mightLevel;
    public int bloodLevel;
    public int recoveryLevel;
    public int spearLevel;
    public int siphonLevel;
    public int novaLevel;
    public int weaponPower;
    public int armorPower;
    public int relicPower;

    public static RpgProgress fresh() {
        RpgProgress result = new RpgProgress();
        result.level = 1;
        result.wave = 1;
        result.spearLevel = 1;
        return result;
    }

    public RpgProgress copy() {
        RpgProgress result = new RpgProgress();
        result.revision = revision;
        result.level = level;
        result.xp = xp;
        result.gold = gold;
        result.region = region;
        result.wave = wave;
        result.kills = kills;
        result.bossKills = bossKills;
        result.chapterClears = chapterClears;
        result.vitalityLevel = vitalityLevel;
        result.mightLevel = mightLevel;
        result.bloodLevel = bloodLevel;
        result.recoveryLevel = recoveryLevel;
        result.spearLevel = spearLevel;
        result.siphonLevel = siphonLevel;
        result.novaLevel = novaLevel;
        result.weaponPower = weaponPower;
        result.armorPower = armorPower;
        result.relicPower = relicPower;
        return result;
    }

    public void normalizeUnlocks() {
        if (spearLevel < 1) {
            spearLevel = 1;
        }
        if (level >= 4 && siphonLevel < 1) {
            siphonLevel = 1;
        }
        if (level >= 7 && novaLevel < 1) {
            novaLevel = 1;
        }
    }

    /** Keeps extremely long-running sessions inside the persisted schema bounds. */
    public void prepareForStore() {
        revision = Math.max(0L, revision);
        level = RpgRules.clamp(level, 1, RpgRules.LEVEL_CAP);
        xp = level >= RpgRules.LEVEL_CAP ? 0 : RpgRules.clamp(xp, 0, 9_999_999);
        gold = RpgRules.clamp(gold, 0, 100_000_000);
        region = RpgRules.clamp(region, 0, RpgRules.REGION_COUNT - 1);
        wave = RpgRules.clamp(wave, 1, RpgRules.WAVES_PER_REGION);
        kills = RpgRules.clamp(kills, 0, 100_000_000);
        bossKills = RpgRules.clamp(bossKills, 0, 10_000_000);
        chapterClears = RpgRules.clamp(chapterClears, 0, 100_000);
        vitalityLevel = RpgRules.clamp(vitalityLevel, 0, RpgRules.UPGRADE_CAP);
        mightLevel = RpgRules.clamp(mightLevel, 0, RpgRules.UPGRADE_CAP);
        bloodLevel = RpgRules.clamp(bloodLevel, 0, RpgRules.UPGRADE_CAP);
        recoveryLevel = RpgRules.clamp(recoveryLevel, 0, RpgRules.UPGRADE_CAP);
        spearLevel = RpgRules.clamp(spearLevel, 0, 12);
        siphonLevel = RpgRules.clamp(siphonLevel, 0, 12);
        novaLevel = RpgRules.clamp(novaLevel, 0, 12);
        weaponPower = RpgRules.clamp(weaponPower, 0, 300);
        armorPower = RpgRules.clamp(armorPower, 0, 300);
        relicPower = RpgRules.clamp(relicPower, 0, 300);
        normalizeUnlocks();
    }

    public String encode() {
        String payload = VERSION + "|" + revision + "|" + level + "|" + xp + "|" + gold
                + "|" + region + "|" + wave + "|" + kills + "|" + bossKills
                + "|" + chapterClears + "|" + vitalityLevel + "|" + mightLevel
                + "|" + bloodLevel + "|" + recoveryLevel + "|" + spearLevel
                + "|" + siphonLevel + "|" + novaLevel + "|" + weaponPower
                + "|" + armorPower + "|" + relicPower;
        CRC32 crc = new CRC32();
        crc.update(payload.getBytes(StandardCharsets.UTF_8));
        return payload + "#" + String.format(Locale.US, "%08X", crc.getValue());
    }

    public static RpgProgress decode(String encoded) {
        if (encoded == null || encoded.length() > 640) {
            return null;
        }
        int separator = encoded.lastIndexOf('#');
        if (separator <= 0 || separator == encoded.length() - 1) {
            return null;
        }
        String payload = encoded.substring(0, separator);
        CRC32 crc = new CRC32();
        crc.update(payload.getBytes(StandardCharsets.UTF_8));
        String expected = String.format(Locale.US, "%08X", crc.getValue());
        if (!expected.equals(encoded.substring(separator + 1))) {
            return null;
        }
        String[] values = payload.split("\\|", -1);
        if (values.length != 20 || !VERSION.equals(values[0])) {
            return null;
        }
        try {
            RpgProgress result = new RpgProgress();
            result.revision = Long.parseLong(values[1]);
            result.level = Integer.parseInt(values[2]);
            result.xp = Integer.parseInt(values[3]);
            result.gold = Integer.parseInt(values[4]);
            result.region = Integer.parseInt(values[5]);
            result.wave = Integer.parseInt(values[6]);
            result.kills = Integer.parseInt(values[7]);
            result.bossKills = Integer.parseInt(values[8]);
            result.chapterClears = Integer.parseInt(values[9]);
            result.vitalityLevel = Integer.parseInt(values[10]);
            result.mightLevel = Integer.parseInt(values[11]);
            result.bloodLevel = Integer.parseInt(values[12]);
            result.recoveryLevel = Integer.parseInt(values[13]);
            result.spearLevel = Integer.parseInt(values[14]);
            result.siphonLevel = Integer.parseInt(values[15]);
            result.novaLevel = Integer.parseInt(values[16]);
            result.weaponPower = Integer.parseInt(values[17]);
            result.armorPower = Integer.parseInt(values[18]);
            result.relicPower = Integer.parseInt(values[19]);
            return result.isValid() ? result : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isValid() {
        return revision >= 0L
                && level >= 1 && level <= RpgRules.LEVEL_CAP
                && xp >= 0 && xp <= 10_000_000
                && gold >= 0 && gold <= 100_000_000
                && region >= 0 && region < RpgRules.REGION_COUNT
                && wave >= 1 && wave <= RpgRules.WAVES_PER_REGION
                && kills >= 0 && kills <= 100_000_000
                && bossKills >= 0 && bossKills <= 10_000_000
                && chapterClears >= 0 && chapterClears <= 100_000
                && inStatRange(vitalityLevel)
                && inStatRange(mightLevel)
                && inStatRange(bloodLevel)
                && inStatRange(recoveryLevel)
                && inSkillRange(spearLevel)
                && inSkillRange(siphonLevel)
                && inSkillRange(novaLevel)
                && inEquipmentRange(weaponPower)
                && inEquipmentRange(armorPower)
                && inEquipmentRange(relicPower);
    }

    private static boolean inStatRange(int value) {
        return value >= 0 && value <= RpgRules.UPGRADE_CAP;
    }

    private static boolean inSkillRange(int value) {
        return value >= 0 && value <= 12;
    }

    private static boolean inEquipmentRange(int value) {
        return value >= 0 && value <= 300;
    }
}
