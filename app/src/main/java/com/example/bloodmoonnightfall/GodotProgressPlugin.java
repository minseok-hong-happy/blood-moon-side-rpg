package com.example.bloodmoonnightfall;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Keeps the existing alternating/checksummed Android save format while the game renderer moves
 * to Godot. The singleton name is exposed to GDScript as {@code VaylornProgress}.
 */
public final class GodotProgressPlugin extends GodotPlugin {
    private final RpgProgressStore store;

    public GodotProgressPlugin(Godot godot) {
        super(godot);
        store = new RpgProgressStore(getContext());
    }

    @Override
    public String getPluginName() {
        return "VaylornProgress";
    }

    @UsedByGodot
    public String loadProgressJson() {
        RpgProgress progress = store.load();
        if (progress == null) {
            return "";
        }
        try {
            return toJson(progress).toString();
        } catch (JSONException ignored) {
            return "";
        }
    }

    @UsedByGodot
    public boolean storeProgressJson(String payload) {
        if (payload == null || payload.length() > 8_192) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(payload);
            RpgProgress progress = fromJson(json);
            progress.prepareForStore();
            store.store(progress);
            return true;
        } catch (JSONException | RuntimeException ignored) {
            return false;
        }
    }

    @UsedByGodot
    public long lastActiveEpochSeconds() {
        return store.lastActiveEpochSeconds();
    }

    @UsedByGodot
    public long nowEpochSeconds() {
        return System.currentTimeMillis() / 1_000L;
    }

    @UsedByGodot
    public void clearProgress() {
        store.clear();
    }

    static JSONObject toJson(RpgProgress value) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("revision", value.revision);
        result.put("level", value.level);
        result.put("xp", value.xp);
        result.put("gold", value.gold);
        result.put("region", value.region);
        result.put("wave", value.wave);
        result.put("kills", value.kills);
        result.put("boss_kills", value.bossKills);
        result.put("chapter_clears", value.chapterClears);
        result.put("vitality_level", value.vitalityLevel);
        result.put("might_level", value.mightLevel);
        result.put("blood_level", value.bloodLevel);
        result.put("recovery_level", value.recoveryLevel);
        result.put("spear_level", value.spearLevel);
        result.put("siphon_level", value.siphonLevel);
        result.put("nova_level", value.novaLevel);
        result.put("weapon_power", value.weaponPower);
        result.put("armor_power", value.armorPower);
        result.put("relic_power", value.relicPower);
        JSONArray inventory = new JSONArray();
        for (int item : value.inventory) {
            inventory.put(item);
        }
        result.put("inventory", inventory);
        return result;
    }

    static RpgProgress fromJson(JSONObject json) {
        RpgProgress result = RpgProgress.fresh();
        result.revision = json.optLong("revision", 0L);
        result.level = json.optInt("level", result.level);
        result.xp = json.optInt("xp", 0);
        result.gold = json.optInt("gold", 0);
        result.region = json.optInt("region", 0);
        result.wave = json.optInt("wave", 1);
        result.kills = json.optInt("kills", 0);
        result.bossKills = json.optInt("boss_kills", 0);
        result.chapterClears = json.optInt("chapter_clears", 0);
        result.vitalityLevel = json.optInt("vitality_level", 0);
        result.mightLevel = json.optInt("might_level", 0);
        result.bloodLevel = json.optInt("blood_level", 0);
        result.recoveryLevel = json.optInt("recovery_level", 0);
        result.spearLevel = json.optInt("spear_level", 1);
        result.siphonLevel = json.optInt("siphon_level", 0);
        result.novaLevel = json.optInt("nova_level", 0);
        result.weaponPower = json.optInt("weapon_power", 3);
        result.armorPower = json.optInt("armor_power", 2);
        result.relicPower = json.optInt("relic_power", 1);
        JSONArray inventory = json.optJSONArray("inventory");
        if (inventory != null) {
            for (int index = 0; index < Math.min(inventory.length(), result.inventory.length);
                    index++) {
                result.inventory[index] = inventory.optInt(index, 0);
            }
        }
        return result;
    }
}
