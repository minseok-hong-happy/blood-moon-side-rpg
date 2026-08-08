package com.example.bloodmoonnightfall;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.CRC32;

/** Alternating save slots keep a previous checkpoint if a write is interrupted. */
public final class RpgProgressStore {
    private static final String PREFS = "nightfall_growth_progress_v3";
    private static final String SLOT_A = "adventure_a";
    private static final String SLOT_B = "adventure_b";
    private static final String MIGRATION_DONE = "legacy_migration_done";
    private static final String LEGACY_DUEL_PREFS = "nightfall_duel_progress_v2";
    private static final String LEGACY_WORLD_PREFS = "nightfall_save_v1";

    private final SharedPreferences preferences;
    private final SharedPreferences legacyDuelPreferences;
    private final SharedPreferences legacyWorldPreferences;

    public RpgProgressStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        legacyDuelPreferences = context.getSharedPreferences(LEGACY_DUEL_PREFS,
                Context.MODE_PRIVATE);
        legacyWorldPreferences = context.getSharedPreferences(LEGACY_WORLD_PREFS,
                Context.MODE_PRIVATE);
    }

    public RpgProgress load() {
        RpgProgress current = loadCurrentSlots();
        if (current != null) {
            current.normalizeUnlocks();
            return current;
        }
        if (preferences.getBoolean(MIGRATION_DONE, false)) {
            return null;
        }
        RpgProgress migrated = migrateLegacyDuel(
                legacyDuelPreferences.getString("campaign_a", null),
                legacyDuelPreferences.getString("campaign_b", null));
        if (migrated == null) {
            migrated = migrateLegacyWorld(
                    legacyWorldPreferences.getString("slot_a", null),
                    legacyWorldPreferences.getString("slot_b", null));
        }
        SharedPreferences.Editor editor = preferences.edit().putBoolean(MIGRATION_DONE, true);
        if (migrated != null) {
            migrated.revision = 1L;
            editor.putString(SLOT_B, migrated.encode());
        }
        editor.apply();
        return migrated;
    }

    public boolean hasSave() {
        return load() != null;
    }

    public void store(RpgProgress progress) {
        if (progress == null) {
            return;
        }
        progress.prepareForStore();
        RpgProgress current = loadCurrentSlots();
        progress.revision = current == null ? Math.max(1L, progress.revision + 1L)
                : Math.max(current.revision + 1L, progress.revision + 1L);
        progress.normalizeUnlocks();
        String target = (progress.revision & 1L) == 0L ? SLOT_A : SLOT_B;
        preferences.edit()
                .putBoolean(MIGRATION_DONE, true)
                .putString(target, progress.encode())
                .apply();
    }

    /** Explicit reset also creates a tombstone so an older save cannot be imported again. */
    public void clear() {
        preferences.edit()
                .remove(SLOT_A)
                .remove(SLOT_B)
                .putBoolean(MIGRATION_DONE, true)
                .apply();
    }

    private RpgProgress loadCurrentSlots() {
        return newestValid(
                RpgProgress.decode(preferences.getString(SLOT_A, null)),
                RpgProgress.decode(preferences.getString(SLOT_B, null)));
    }

    static RpgProgress newestValid(RpgProgress first, RpgProgress second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.revision >= second.revision ? first : second;
    }

    static RpgProgress migrateLegacyDuel(String firstEncoded, String secondEncoded) {
        LegacyDuel first = LegacyDuel.decode(firstEncoded);
        LegacyDuel second = LegacyDuel.decode(secondEncoded);
        LegacyDuel legacy = first == null ? second
                : second == null || first.revision >= second.revision ? first : second;
        if (legacy == null) {
            return null;
        }
        RpgProgress result = RpgProgress.fresh();
        result.level = RpgRules.clamp(1 + legacy.victories + legacy.campaignClears * 4,
                1, RpgRules.LEVEL_CAP);
        result.gold = legacy.victories * 110
                + (legacy.health + legacy.damage + legacy.blood + legacy.regen) * 65;
        result.region = RpgRules.clamp(legacy.duelIndex / 2, 0, 2);
        result.wave = RpgRules.clamp(1 + legacy.duelIndex, 1, 5);
        result.kills = legacy.victories * 6;
        result.bossKills = legacy.campaignClears;
        result.chapterClears = legacy.campaignClears;
        result.vitalityLevel = legacy.health;
        result.mightLevel = legacy.damage;
        result.bloodLevel = legacy.blood;
        result.recoveryLevel = legacy.regen;
        result.weaponPower = legacy.damage * 2;
        result.armorPower = legacy.health * 2;
        result.relicPower = legacy.blood * 2;
        result.normalizeUnlocks();
        return result;
    }

    static RpgProgress migrateLegacyWorld(String firstEncoded, String secondEncoded) {
        LegacyWorld first = LegacyWorld.decode(firstEncoded);
        LegacyWorld second = LegacyWorld.decode(secondEncoded);
        LegacyWorld legacy = first == null ? second
                : second == null || first.revision >= second.revision ? first : second;
        if (legacy == null) {
            return null;
        }
        RpgProgress result = RpgProgress.fresh();
        int exploration = legacy.sigils + legacy.chests + legacy.defeatedEnemies / 3;
        result.level = RpgRules.clamp(1 + exploration + (legacy.bossDefeated ? 3 : 0),
                1, RpgRules.LEVEL_CAP);
        result.gold = legacy.chests * 100 + legacy.sigils * 65
                + legacy.defeatedEnemies * 14 + (legacy.bossDefeated ? 240 : 0);
        result.region = legacy.checkpointId.contains("keep") ? 2
                : legacy.checkpointId.contains("ashwood") ? 1 : 0;
        result.wave = RpgRules.clamp(1 + legacy.defeatedEnemies / 3, 1, 5);
        if (legacy.bossDefeated) {
            result.region = 0;
            result.wave = 1;
            result.bossKills = 1;
            result.chapterClears = 1;
        }
        result.kills = legacy.defeatedEnemies;
        result.vitalityLevel = RpgRules.clamp((legacy.maxHealth - 100) / 20, 0,
                RpgRules.UPGRADE_CAP);
        result.mightLevel = RpgRules.clamp(legacy.sigils, 0, RpgRules.UPGRADE_CAP);
        result.bloodLevel = RpgRules.clamp((legacy.maxBlood - 100) / 18, 0,
                RpgRules.UPGRADE_CAP);
        result.recoveryLevel = RpgRules.clamp(legacy.chests, 0, RpgRules.UPGRADE_CAP);
        result.weaponPower = RpgRules.clamp(legacy.sigils * 4 + legacy.defeatedEnemies / 2,
                0, 300);
        result.armorPower = RpgRules.clamp(result.vitalityLevel * 3 + legacy.chests * 2,
                0, 300);
        result.relicPower = RpgRules.clamp(result.bloodLevel * 3 + legacy.sigils * 2,
                0, 300);
        result.normalizeUnlocks();
        return result;
    }

    private static final class LegacyDuel {
        long revision;
        int duelIndex;
        int health;
        int damage;
        int blood;
        int regen;
        int victories;
        int campaignClears;

        static LegacyDuel decode(String encoded) {
            if (encoded == null || encoded.length() > 320) {
                return null;
            }
            int separator = encoded.lastIndexOf('#');
            if (separator <= 0 || separator == encoded.length() - 1) {
                return null;
            }
            String payload = encoded.substring(0, separator);
            CRC32 crc = new CRC32();
            crc.update(payload.getBytes(StandardCharsets.UTF_8));
            if (!String.format(Locale.US, "%08X", crc.getValue())
                    .equals(encoded.substring(separator + 1))) {
                return null;
            }
            String[] values = payload.split("\\|", -1);
            if (values.length != 9 || !"D2".equals(values[0])) {
                return null;
            }
            try {
                LegacyDuel result = new LegacyDuel();
                result.revision = Long.parseLong(values[1]);
                result.duelIndex = Integer.parseInt(values[2]);
                result.health = Integer.parseInt(values[3]);
                result.damage = Integer.parseInt(values[4]);
                result.blood = Integer.parseInt(values[5]);
                result.regen = Integer.parseInt(values[6]);
                result.victories = Integer.parseInt(values[7]);
                result.campaignClears = Integer.parseInt(values[8]);
                if (result.revision < 0 || result.duelIndex < 0 || result.duelIndex > 3
                        || result.health < 0 || result.health > 20
                        || result.damage < 0 || result.damage > 20
                        || result.blood < 0 || result.blood > 20
                        || result.regen < 0 || result.regen > 20
                        || result.victories < 0 || result.victories > 1_000_000
                        || result.campaignClears < 0 || result.campaignClears > 100_000) {
                    return null;
                }
                return result;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private static final class LegacyWorld {
        long revision;
        String checkpointId;
        int maxHealth;
        int maxBlood;
        int sigils;
        int chests;
        int defeatedEnemies;
        boolean bossDefeated;

        static LegacyWorld decode(String encoded) {
            if (encoded == null || encoded.length() > 16_384) {
                return null;
            }
            int separator = encoded.lastIndexOf('#');
            if (separator <= 0 || separator == encoded.length() - 1) {
                return null;
            }
            String payload = encoded.substring(0, separator);
            CRC32 crc = new CRC32();
            crc.update(payload.getBytes(StandardCharsets.UTF_8));
            if (!Long.toHexString(crc.getValue())
                    .equalsIgnoreCase(encoded.substring(separator + 1))) {
                return null;
            }
            String[] fields = payload.split("\\|", -1);
            if (fields.length != 13 || !"1".equals(fields[0])) {
                return null;
            }
            try {
                LegacyWorld result = new LegacyWorld();
                result.revision = Long.parseLong(fields[1]);
                result.checkpointId = fields[2];
                Float.parseFloat(fields[3]);
                Float.parseFloat(fields[4]);
                Integer.parseInt(fields[5]);
                result.maxHealth = Integer.parseInt(fields[6]);
                Integer.parseInt(fields[7]);
                result.maxBlood = Integer.parseInt(fields[8]);
                result.sigils = countIds(fields[9]);
                result.chests = countIds(fields[10]);
                result.defeatedEnemies = countIds(fields[11]);
                result.bossDefeated = "1".equals(fields[12]);
                if (result.revision < 0 || result.checkpointId.length() > 80
                        || result.maxHealth < 60 || result.maxHealth > 250
                        || result.maxBlood < 50 || result.maxBlood > 250
                        || (!"0".equals(fields[12]) && !"1".equals(fields[12]))) {
                    return null;
                }
                return result;
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        private static int countIds(String csv) {
            if (csv == null || csv.isEmpty()) {
                return 0;
            }
            Set<String> unique = new HashSet<>();
            String[] values = csv.split(",");
            for (String value : values) {
                if (!value.isEmpty() && value.length() <= 80 && unique.size() < 256) {
                    unique.add(value);
                }
            }
            return unique.size();
        }
    }
}
