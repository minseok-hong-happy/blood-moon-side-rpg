class_name GameRules
extends RefCounted

const ARENA_LEFT := 54.0
const ARENA_RIGHT := 666.0
const REGION_COUNT := 3
const WAVES_PER_REGION := 5
const LEVEL_CAP := 60
const UPGRADE_CAP := 30
const OFFLINE_REWARD_CAP_SECONDS := 8 * 60 * 60

const ENEMY_THRALL := 0
const ENEMY_HUNTER := 1
const ENEMY_WRAITH := 2
const ENEMY_BOSS := 3


static func fresh_progress() -> Dictionary:
	return {
		"revision": 0,
		"level": 1,
		"xp": 0,
		"gold": 0,
		"region": 0,
		"wave": 1,
		"kills": 0,
		"boss_kills": 0,
		"chapter_clears": 0,
		"vitality_level": 0,
		"might_level": 0,
		"blood_level": 0,
		"recovery_level": 0,
		"spear_level": 1,
		"siphon_level": 0,
		"nova_level": 0,
		"weapon_power": 3,
		"armor_power": 2,
		"relic_power": 1,
		"inventory": [0, 0, 0, 0, 0, 0],
	}


static func normalize_progress(value: Dictionary) -> Dictionary:
	var result := fresh_progress()
	for key in result.keys():
		if value.has(key):
			result[key] = value[key]
	result.level = clampi(int(result.level), 1, LEVEL_CAP)
	result.xp = clampi(int(result.xp), 0, 9_999_999)
	result.gold = clampi(int(result.gold), 0, 100_000_000)
	result.region = clampi(int(result.region), 0, REGION_COUNT - 1)
	result.wave = clampi(int(result.wave), 1, WAVES_PER_REGION)
	result.kills = clampi(int(result.kills), 0, 100_000_000)
	result.boss_kills = clampi(int(result.boss_kills), 0, 10_000_000)
	result.chapter_clears = clampi(int(result.chapter_clears), 0, 100_000)
	for key in ["vitality_level", "might_level", "blood_level", "recovery_level"]:
		result[key] = clampi(int(result[key]), 0, UPGRADE_CAP)
	for key in ["spear_level", "siphon_level", "nova_level"]:
		result[key] = clampi(int(result[key]), 0, 12)
	for key in ["weapon_power", "armor_power", "relic_power"]:
		result[key] = clampi(int(result[key]), 0, 300)
	if result.level >= 1:
		result.spear_level = maxi(1, int(result.spear_level))
	if result.level >= 4:
		result.siphon_level = maxi(1, int(result.siphon_level))
	if result.level >= 7:
		result.nova_level = maxi(1, int(result.nova_level))
	var inventory: Array = result.inventory if result.inventory is Array else []
	while inventory.size() < 6:
		inventory.append(0)
	result.inventory = inventory.slice(0, 6)
	return result


static func xp_for_next_level(level: int) -> int:
	var safe := clampi(level, 1, LEVEL_CAP)
	return 70 + safe * 42 + safe * safe * 6


static func stat_upgrade_cost(current_level: int) -> int:
	var safe := clampi(current_level, 0, UPGRADE_CAP)
	return 65 + safe * 48 + safe * safe * 9


static func skill_upgrade_cost(current_level: int) -> int:
	var safe := clampi(current_level, 0, 12)
	return 105 + safe * 90 + safe * safe * 15


static func hero_max_health(level: int, vitality_level: int) -> int:
	return 118 + (clampi(level, 1, LEVEL_CAP) - 1) * 9 \
			+ clampi(vitality_level, 0, UPGRADE_CAP) * 24


static func hero_max_blood(level: int, blood_level: int, relic_power: int) -> int:
	return 100 + (clampi(level, 1, LEVEL_CAP) - 1) * 3 \
			+ clampi(blood_level, 0, UPGRADE_CAP) * 15 \
			+ clampi(relic_power, 0, 300) * 2


static func hero_attack_power(level: int, might_level: int, weapon_power: int) -> int:
	return 17 + (clampi(level, 1, LEVEL_CAP) - 1) * 2 \
			+ clampi(might_level, 0, UPGRADE_CAP) * 5 \
			+ clampi(weapon_power, 0, 300)


static func melee_damage(attack_power: int, combo_index: int) -> int:
	var multipliers := [1.0, 1.18, 1.55]
	return maxi(1, int(round(maxi(1, attack_power) * multipliers[clampi(combo_index, 0, 2)])))


static func skill_damage(skill_index: int, attack_power: int, hero_level: int,
		skill_level: int) -> int:
	var level := clampi(skill_level, 1, 12)
	var multiplier := 1.0
	match skill_index:
		0: multiplier = 1.45 + level * 0.13
		1: multiplier = 1.18 + level * 0.11
		2: multiplier = 1.55 + level * 0.16
		3: multiplier = 1.28 + clampi(hero_level, 3, LEVEL_CAP) * 0.018
		4: multiplier = 1.08 + clampi(hero_level, 6, LEVEL_CAP) * 0.014
		5: multiplier = 1.18 + clampi(hero_level, 9, LEVEL_CAP) * 0.016
		6: multiplier = 1.48 + clampi(hero_level, 12, LEVEL_CAP) * 0.019
		7: multiplier = 2.15 + clampi(hero_level, 15, LEVEL_CAP) * 0.024
	return maxi(1, int(round(maxi(1, attack_power) * multiplier)))


static func recovery_per_second(recovery_level: int) -> float:
	return 0.65 + clampi(recovery_level, 0, UPGRADE_CAP) * 0.22


static func mitigate_damage(raw_damage: int, armor_power: int) -> int:
	var safe_raw := maxi(0, raw_damage)
	if safe_raw == 0:
		return 0
	return maxi(1, int(round(safe_raw * (100.0 / (100.0 + clampi(armor_power, 0, 300) * 4.0)))))


static func wave_enemy_count(region: int, wave: int) -> int:
	var safe_region := clampi(region, 0, REGION_COUNT - 1)
	var safe_wave := clampi(wave, 1, WAVES_PER_REGION)
	return 18 + safe_region * 4 if safe_wave == WAVES_PER_REGION \
			else 26 + safe_wave * 6 + safe_region * 5


static func reinforcement_batch_size(active: int, remaining: int, maximum: int) -> int:
	var desired := 6 if active <= 6 else 4 if active <= 12 else 2
	return mini(mini(desired, maxi(0, remaining)), maxi(0, maximum - maxi(0, active)))


static func is_elite_spawn(wave: int, spawn_serial: int, wave_total: int) -> bool:
	var midpoint := maxi(4, maxi(1, wave_total) / 2)
	return wave >= 2 and wave < WAVES_PER_REGION and spawn_serial == midpoint


static func enemy_max_health(kind: int, region: int, wave: int, hero_level: int,
		chapter_clears: int) -> int:
	var bases := [54, 72, 86, 320]
	var scale := 1.0 + clampi(region, 0, 2) * 0.48 \
			+ (clampi(wave, 1, 5) - 1) * 0.14 \
			+ (clampi(hero_level, 1, LEVEL_CAP) - 1) * 0.045 \
			+ clampi(chapter_clears, 0, 1000) * 0.28
	return maxi(1, int(round(bases[clampi(kind, 0, ENEMY_BOSS)] * scale)))


static func enemy_damage(kind: int, region: int, wave: int, hero_level: int,
		chapter_clears: int, heavy: bool) -> int:
	var bases := [7, 9, 11, 16]
	var scale := 1.0 + clampi(region, 0, 2) * 0.24 \
			+ (clampi(wave, 1, 5) - 1) * 0.07 \
			+ (clampi(hero_level, 1, LEVEL_CAP) - 1) * 0.018 \
			+ clampi(chapter_clears, 0, 1000) * 0.13
	if heavy:
		scale *= 1.55
	return maxi(1, int(round(bases[clampi(kind, 0, ENEMY_BOSS)] * scale)))


static func xp_reward(kind: int, region: int, wave: int, chapter_clears: int) -> int:
	var safe_kind := clampi(kind, 0, ENEMY_BOSS)
	var base := 165 if safe_kind == ENEMY_BOSS else 18 + safe_kind * 5
	return maxi(1, int(round(base * (1.0 + clampi(region, 0, 2) * 0.32 \
			+ (clampi(wave, 1, 5) - 1) * 0.08 + clampi(chapter_clears, 0, 1000) * 0.12))))


static func gold_reward(kind: int, region: int, wave: int, chapter_clears: int) -> int:
	var safe_kind := clampi(kind, 0, ENEMY_BOSS)
	var base := 145 if safe_kind == ENEMY_BOSS else 12 + safe_kind * 4
	return maxi(1, int(round(base * (1.0 + clampi(region, 0, 2) * 0.28 \
			+ (clampi(wave, 1, 5) - 1) * 0.06 + clampi(chapter_clears, 0, 1000) * 0.10))))


static func equipment_power(region: int, wave: int, hero_level: int,
		rarity: int, chapter_clears: int) -> int:
	return clampi(2 + region * 5 + wave * 2 + hero_level / 2 \
			+ clampi(rarity, 0, 3) * (4 + region * 2) + chapter_clears * 3, 1, 300)


static func hunt_chain_bonus_gold(chain: int, base_gold: int) -> int:
	if chain < 5 or chain % 5 != 0:
		return 0
	return maxi(20, maxi(1, base_gold) * (1 + chain / 5))


static func offline_elapsed_seconds(last_active: int, now_seconds: int) -> int:
	if last_active <= 0 or now_seconds <= last_active:
		return 0
	return mini(now_seconds - last_active, OFFLINE_REWARD_CAP_SECONDS)


static func offline_gold_reward(elapsed: int, level: int, region: int, wave: int) -> int:
	var minutes := clampi(elapsed, 0, OFFLINE_REWARD_CAP_SECONDS) / 60
	var per_minute := 10 + clampi(level, 1, LEVEL_CAP) * 2 \
			+ clampi(region, 0, 2) * 8 + clampi(wave, 1, 5) * 3
	return mini(2_000_000, minutes * per_minute)


static func offline_xp_reward(elapsed: int, level: int, region: int, wave: int) -> int:
	var minutes := clampi(elapsed, 0, OFFLINE_REWARD_CAP_SECONDS) / 60
	var per_minute := 3 + clampi(level, 1, LEVEL_CAP) / 2 \
			+ clampi(region, 0, 2) * 3 + clampi(wave, 1, 5)
	return mini(1_000_000, minutes * per_minute)
