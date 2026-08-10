extends SceneTree

const Rules = preload("res://scripts/game_rules.gd")
const Atlases = preload("res://scripts/atlas_library.gd")
const Stories = preload("res://scripts/story_data.gd")
const Actor = preload("res://scripts/combat_actor.gd")
const HERO_TEXTURE = preload("res://art/hero_side_atlas_v5.png")
const EFFECT_TEXTURE = preload("res://art/vfx_blood_arts_anim_a_v2.png")

var failures: Array[String] = []


func _initialize() -> void:
	_test_progress_rules()
	_test_combat_rules()
	_test_atlas_contracts()
	_test_action_animation_lock()
	_test_story_contracts()
	if failures.is_empty():
		print("GODOT TESTS PASSED: rules, atlas animation, story data")
		quit(0)
	else:
		for failure in failures:
			push_error(failure)
		quit(1)


func _expect(condition: bool, message: String) -> void:
	if not condition:
		failures.append(message)


func _test_progress_rules() -> void:
	var fresh := Rules.fresh_progress()
	_expect(int(fresh.level) == 1, "Fresh progress must begin at level 1")
	_expect((fresh.inventory as Array).size() == 6, "Inventory must keep six stable slots")
	var dirty := {"level": 999, "wave": -2, "region": 8, "gold": -20,
		"inventory": [1]}
	var normalized := Rules.normalize_progress(dirty)
	_expect(int(normalized.level) == Rules.LEVEL_CAP, "Level must clamp to the cap")
	_expect(int(normalized.wave) == 1, "Wave must clamp to the first wave")
	_expect(int(normalized.region) == Rules.REGION_COUNT - 1, "Region must clamp")
	_expect(int(normalized.gold) == 0, "Gold cannot be negative")
	_expect((normalized.inventory as Array).size() == 6, "Normalization must restore six slots")


func _test_combat_rules() -> void:
	_expect(Rules.wave_enemy_count(0, 1) >= 30, "A normal wave must present a visible horde")
	_expect(Rules.wave_enemy_count(2, 5) >= 20, "Boss waves must include an escort")
	_expect(Rules.reinforcement_batch_size(0, 20, 9) >= 4,
		"An empty battlefield must receive a full reinforcement batch")
	_expect(Rules.melee_damage(50, 2) > Rules.melee_damage(50, 0),
		"Combo finisher must out-damage the opener")
	_expect(Rules.skill_damage(7, 50, 20, 4) > Rules.skill_damage(0, 50, 20, 4),
		"Eclipse must read as an ultimate")
	_expect(Rules.offline_elapsed_seconds(1, 100_000) == Rules.OFFLINE_REWARD_CAP_SECONDS,
		"Offline progress must respect its safety cap")


func _test_atlas_contracts() -> void:
	var hero_frames := Atlases.hero_frames(HERO_TEXTURE)
	_expect(hero_frames.has_animation(&"run"), "Hero atlas must expose a run animation")
	_expect(hero_frames.get_frame_count(&"run") == 4, "Run animation must use four distinct frames")
	_expect(hero_frames.get_animation_speed(&"attack_1") >= 18.0,
		"Attack response must remain faster than the original prototype")
	var effect_frames := Atlases.effect_frames(EFFECT_TEXTURE, 0, 4)
	_expect(effect_frames.get_frame_count(&"play") == 4,
		"Each blood art must animate through four image frames")


func _test_action_animation_lock() -> void:
	var actor := Actor.new()
	actor.configure(Atlases.hero_frames(HERO_TEXTURE), 0.53, -1, "카엘", true)
	actor.play_animation(&"attack_1", true)
	actor.set_running(true)
	_expect(actor.sprite.animation == &"attack_1",
		"Locomotion must not overwrite an attack before its action frames finish")
	actor.animate_stride(0.04, 0.0)
	actor.set_running(false)
	_expect(actor.sprite.animation == &"attack_1",
		"Idle must not truncate a locked attack animation")
	actor.free()


func _test_story_contracts() -> void:
	_expect(Stories.REGION_NAMES.size() == Rules.REGION_COUNT,
		"Each region needs a story identity")
	for region in range(Rules.REGION_COUNT):
		for wave in range(1, Rules.WAVES_PER_REGION + 1):
			var opening := Stories.opening(region, wave, 0)
			var midpoint := Stories.midpoint(region, wave)
			_expect(not str(opening.line).is_empty(), "Every wave needs an opening beat")
			_expect(not str(midpoint.line).is_empty(), "Every wave needs a midpoint beat")
