package com.example.bloodmoonnightfall;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.godotengine.godot.plugin.UsedByGodot;
import org.json.JSONObject;
import org.junit.Test;

public final class GodotProgressPluginTest {
    @Test
    public void runtimeReadyMarkerRemainsExportedToGodot() throws Exception {
        assertNotNull(GodotProgressPlugin.class.getDeclaredMethod("markGameReady")
                .getAnnotation(UsedByGodot.class));
    }

    @Test
    public void jsonBridgeKeepsEveryPermanentProgressField() throws Exception {
        RpgProgress original = RpgProgress.fresh();
        original.revision = 42L;
        original.level = 18;
        original.xp = 2_502;
        original.gold = 5_878;
        original.region = 1;
        original.wave = 5;
        original.kills = 453;
        original.bossKills = 9;
        original.chapterClears = 2;
        original.vitalityLevel = 6;
        original.mightLevel = 7;
        original.bloodLevel = 8;
        original.recoveryLevel = 5;
        original.spearLevel = 4;
        original.siphonLevel = 3;
        original.novaLevel = 2;
        original.weaponPower = 33;
        original.armorPower = 28;
        original.relicPower = 25;
        original.inventory[0] = 3_021;
        original.inventory[1] = 12_034;
        original.inventory[2] = 23_055;

        JSONObject json = GodotProgressPlugin.toJson(original);
        RpgProgress restored = GodotProgressPlugin.fromJson(json);

        assertEquals(original.revision, restored.revision);
        assertEquals(original.level, restored.level);
        assertEquals(original.xp, restored.xp);
        assertEquals(original.gold, restored.gold);
        assertEquals(original.region, restored.region);
        assertEquals(original.wave, restored.wave);
        assertEquals(original.kills, restored.kills);
        assertEquals(original.bossKills, restored.bossKills);
        assertEquals(original.chapterClears, restored.chapterClears);
        assertEquals(original.vitalityLevel, restored.vitalityLevel);
        assertEquals(original.mightLevel, restored.mightLevel);
        assertEquals(original.bloodLevel, restored.bloodLevel);
        assertEquals(original.recoveryLevel, restored.recoveryLevel);
        assertEquals(original.spearLevel, restored.spearLevel);
        assertEquals(original.siphonLevel, restored.siphonLevel);
        assertEquals(original.novaLevel, restored.novaLevel);
        assertEquals(original.weaponPower, restored.weaponPower);
        assertEquals(original.armorPower, restored.armorPower);
        assertEquals(original.relicPower, restored.relicPower);
        assertArrayEquals(original.inventory, restored.inventory);
    }

    @Test
    public void jsonBridgeDefaultsMissingFieldsToFreshProgress() {
        RpgProgress restored = GodotProgressPlugin.fromJson(new JSONObject());

        assertEquals(1, restored.level);
        assertEquals(1, restored.wave);
        assertEquals(1, restored.spearLevel);
        assertEquals(3, restored.weaponPower);
        assertEquals(2, restored.armorPower);
        assertEquals(1, restored.relicPower);
    }
}
