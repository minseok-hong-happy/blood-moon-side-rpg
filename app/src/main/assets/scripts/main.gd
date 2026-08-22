extends Node2D

const Rules = preload("res://scripts/game_rules.gd")
const Atlases = preload("res://scripts/atlas_library.gd")
const Stories = preload("res://scripts/story_data.gd")
const ActorScene = preload("res://scripts/combat_actor.gd")
const SaveLayer = preload("res://scripts/save_adapter.gd")

const HERO_TEXTURE = preload("res://art/hero_side_atlas_v5.png")
const ENEMY_TEXTURE = preload("res://art/enemy_side_atlas.png")
const BOSS_TEXTURES := [
	preload("res://art/boss_sun_inquisitor_atlas_v3.png"),
	preload("res://art/boss_ash_warden_atlas_v3.png"),
	preload("res://art/boss_eclipse_sovereign_atlas_v3.png"),
]
const BACKGROUNDS := [
	preload("res://art/bg_duel_arena_portrait.png"),
	preload("res://art/bg_ashwood_hunt_portrait.png"),
	preload("res://art/bg_nocturne_sanctuary_portrait.png"),
]
const EFFECT_A = preload("res://art/vfx_blood_arts_anim_a_v2.png")
const EFFECT_B = preload("res://art/vfx_blood_arts_anim_b_v2.png")
const EFFECT_IMPACT = preload("res://art/vfx_combat_impact_anim_v1.png")
const SKILL_ICONS = preload("res://art/skill_icons_atlas_v2.png")
const EQUIPMENT_ICONS = preload("res://art/equipment_icons_atlas_v1.png")
const BLOOD_SHARD_PARTICLE = preload("res://art/vfx_blood_shard_particle_v1.png")
const UI_FONT = preload("res://fonts/NotoSansKR-VariableFont_wght.ttf")

const BGM = preload("res://audio/bgm_blood_road.wav")
const SFX_HIT_LIGHT = preload("res://audio/sfx_hit_light.wav")
const SFX_HIT_HEAVY = preload("res://audio/sfx_hit_heavy.wav")
const SFX_HERO_HURT = preload("res://audio/sfx_player_hurt.wav")
const SFX_DASH = preload("res://audio/sfx_dash.wav")
const SFX_SPEAR = preload("res://audio/sfx_blood_spear.wav")
const SFX_SIPHON = preload("res://audio/sfx_siphon.wav")
const SFX_NOVA = preload("res://audio/sfx_blood_nova.wav")
const SFX_LEVEL = preload("res://audio/sfx_level_up.wav")
const SFX_TAP = preload("res://audio/sfx_ui_tap.wav")

const VIEW_SIZE := Vector2(720.0, 1280.0)
const BASE_GROUND_Y := 842.0
const BOTTOM_PANEL_HEIGHT := 354.0
const SKILL_CARD_SIZE := Vector2(164.0, 112.0)
const SKILL_ICON_RECT := Rect2(12.0, 10.0, 66.0, 66.0)
const HERO_FIRST_QUARTER_X := 180.0
const HERO_START_X := HERO_FIRST_QUARTER_X
const HERO_VFX_Z_INDEX := 54
const COMBAT_LINE_X := 280.0
const ENEMY_COMBAT_SPACING := 22.0
const SKILL_ENGAGE_X := 520.0
const AUTO_SKILL_ENTRY_X := 680.0
const SKILL_SPLASH_MAX_X := 752.0
const VFX_VIEW_PADDING := 18.0
const MAX_ACTIVE_ENEMIES := 24
const MONSTER_SPAWN_BASE_X := 600.0
const MONSTER_SPAWN_SPACING := 8.0
const INITIAL_NORMAL_SPAWN_COUNT := 16
const INITIAL_BOSS_SPAWN_COUNT := 12
const SKILL_NAMES := ["혈창", "흡혈", "혈화", "혈보", "적우", "사슬", "혈주", "월식"]
const SKILL_SUBTITLES := ["관통", "회복", "폭발", "돌진", "낙하", "연쇄", "분출", "필살"]
const SKILL_UNLOCK_LEVELS := [1, 1, 2, 3, 4, 5, 7, 9]
const SKILL_COOLDOWNS := [1.1, 2.0, 2.6, 1.4, 2.9, 2.2, 3.3, 4.8]
const SKILL_BLOOD_COSTS := [5, 7, 8, 6, 9, 7, 10, 13]
const AUTO_SKILL_CAST_GAP := 0.16
const STAT_KEYS := ["vitality_level", "might_level", "blood_level", "recovery_level"]
const STAT_NAMES := ["생명", "공격", "혈기", "재생"]
const ITEM_SLOT_NAMES := ["무기", "갑옷", "유물"]
const RARITY_NAMES := ["낡은", "정교한", "저주받은", "월식의"]
const RARITY_COLORS := [
	Color(0.72, 0.77, 0.86), Color(0.29, 0.84, 1.0),
	Color(0.78, 0.38, 1.0), Color(1.0, 0.72, 0.23),
]

var save_layer: SaveAdapter
var progress: Dictionary
var hero: CombatActor
var enemies: Array = []
var enemy_meta: Dictionary = {}
var world: Node2D
var actor_layer: Node2D
var effect_layer: Node2D
var background_sprite: Sprite2D
var background_readability: ColorRect
var horizon_glow: Polygon2D
var ambient_layer: Node2D
var ui_root: Control
var screen_flash: ColorRect
var bottom_panel: Panel
var layout_size := VIEW_SIZE
var ground_y := BASE_GROUND_Y

var hero_hp := 1
var hero_blood := 0.0
var hero_attack_timer := 0.0
var hero_recovery_bank := 0.0
var hero_combo := 0
var hero_down_timer := 0.0
var dash_motion_timer := 0.0
var wave_total := 0
var wave_remaining := 0
var wave_defeated := 0
var spawn_serial := 0
var wave_transition_timer := -1.0
var story_midpoint_shown := false
var game_paused := false
var hit_stop := 0.0
var shake_energy := 0.0
var ambient_time := 0.0
var ui_timer := 0.0
var save_timer := 0.0
var bgm_watchdog_timer := 0.0
var trail_timer := 0.0
var hunt_chain := 0
var skill_timers: Array[float] = []
var auto_skill_cast_timer := 0.0
var auto_skill_cursor := 0
var automatic_skill_casts := 0

var level_label: Label
var location_label: Label
var currency_label: Label
var objective_label: Label
var stats_label: Label
var hp_fill: ColorRect
var hp_text: Label
var blood_fill: ColorRect
var blood_text: Label
var xp_fill: ColorRect
var xp_text: Label
var wave_fill: ColorRect
var wave_text: Label
var auto_label: Label
var banner_label: Label
var story_panel: Panel
var story_speaker: Label
var story_line: Label
var boss_panel: Panel
var boss_title: Label
var boss_health_fill: ColorRect
var boss_health_text: Label
var skill_buttons: Array[Button] = []
var skill_cooldown_overlays: Array[ColorRect] = []
var skill_state_labels: Array[Label] = []
var skill_cooldown_labels: Array[Label] = []
var reward_toast: Panel
var reward_toast_label: Label
var reward_toast_tween: Tween
var reward_toast_xp := 0
var reward_toast_gold := 0
var growth_overlay: Control
var inventory_overlay: Control
var pause_overlay: Control
var offline_overlay: Control
var growth_stat_labels: Array[Label] = []
var growth_buttons: Array[Button] = []
var inventory_equipment_label: Label
var inventory_slot_labels: Array[Label] = []
var inventory_slot_icons: Array[TextureRect] = []
var inventory_slot_panels: Array[Panel] = []
var inventory_equipped_labels: Array[Label] = []
var inventory_equipped_icons: Array[TextureRect] = []
var ui_font_medium: FontVariation
var ui_font_bold: FontVariation
var vfx_additive_material: CanvasItemMaterial
var bgm_player: AudioStreamPlayer
var sfx_pool: Array[AudioStreamPlayer] = []


func _ready() -> void:
	seed(int(Time.get_unix_time_from_system()))
	save_layer = SaveLayer.new()
	var loaded := save_layer.load_progress()
	progress = Rules.normalize_progress(loaded if not loaded.is_empty() else Rules.fresh_progress())
	for index in range(SKILL_COOLDOWNS.size()):
		# Stagger the opening volley without making late slots wait several seconds.
		skill_timers.append(0.08 + index * 0.14)
	_build_scene()
	_apply_progress_to_runtime(true)
	_apply_offline_reward()
	_start_audio()
	_start_wave()
	print("VAYLORN_UI_LAYOUT_READY height=%.1f bottom=%.1f ground=%.1f" % [
		layout_size.y, bottom_panel.position.y, ground_y])
	# Emit a Godot-side marker before optional presentation work. The Java bridge marker remains
	# defense in depth, but release QA must not depend on plugin reflection to prove scene startup.
	print("VAYLORN_GAME_READY")
	if save_layer.bridge:
		save_layer.bridge.markGameReady()
	_show_story(Stories.opening(int(progress.region), int(progress.wave),
		int(progress.chapter_clears)))
	_update_ui()


func _process(delta: float) -> void:
	ambient_time += delta
	_update_ambient(delta)
	_update_shake(delta)
	_update_bgm_watchdog(delta)
	save_timer += delta
	if save_timer >= 5.0:
		save_timer = 0.0
		_save_progress()
	if game_paused:
		_tick_ui(delta)
		return
	# Cooldowns are combat clocks, not range checks. Keep them moving while the hero runs
	# toward a wave, waits for reinforcements, recovers from hit-stop, or revives.
	_tick_skill_cooldowns(delta)
	if hero_down_timer > 0.0:
		hero_down_timer -= delta
		if hero_down_timer <= 0.0:
			_revive_hero()
		_tick_ui(delta)
		return
	if hit_stop > 0.0:
		hit_stop -= delta
		_tick_ui(delta)
		return

	_recover_hero(delta)
	_update_wave_flow(delta)
	_update_enemies(delta)
	_update_hero(delta)
	_update_auto_skills()
	# Derive the HUD after combat decisions so a cast cannot leave a stale READY label
	# on screen until a later UI tick.
	_tick_ui(delta)


func _tick_skill_cooldowns(delta: float) -> void:
	for index in range(skill_timers.size()):
		skill_timers[index] = maxf(0.0, skill_timers[index] - delta)
	auto_skill_cast_timer = maxf(0.0, auto_skill_cast_timer - delta)


func _tick_ui(delta: float) -> void:
	ui_timer += delta
	if ui_timer >= 0.05:
		ui_timer = 0.0
		_update_ui()


func _notification(what: int) -> void:
	if what in [NOTIFICATION_WM_CLOSE_REQUEST, NOTIFICATION_APPLICATION_PAUSED,
			NOTIFICATION_APPLICATION_FOCUS_OUT]:
		_save_progress()


func _exit_tree() -> void:
	# Explicitly release native playback objects when Android replaces or destroys the host
	# Activity. This also keeps automated scene teardown free of retained audio streams.
	if bgm_player:
		bgm_player.stop()
		bgm_player.stream = null
	for player in sfx_pool:
		if is_instance_valid(player):
			player.stop()
			player.stream = null
	sfx_pool.clear()


func _unhandled_input(event: InputEvent) -> void:
	if event.is_action_pressed("ui_cancel"):
		if _close_top_overlay():
			get_viewport().set_input_as_handled()
		else:
			_toggle_pause()
			get_viewport().set_input_as_handled()


func _build_scene() -> void:
	_resolve_layout()
	_build_background()
	world = Node2D.new()
	world.name = "CombatWorld"
	add_child(world)
	actor_layer = Node2D.new()
	actor_layer.name = "Actors"
	world.add_child(actor_layer)
	effect_layer = Node2D.new()
	effect_layer.name = "Effects"
	effect_layer.z_index = 20
	world.add_child(effect_layer)
	vfx_additive_material = CanvasItemMaterial.new()
	vfx_additive_material.blend_mode = CanvasItemMaterial.BLEND_MODE_ADD

	hero = ActorScene.new()
	hero.configure(Atlases.hero_frames(HERO_TEXTURE), 0.53, -1, "카엘", true)
	hero.position = Vector2(HERO_START_X, ground_y)
	# Effects live on a z=20 layer and can reach child z=31. Keep the hero above
	# every VFX layer so additive cast art never cuts across the head or silhouette.
	hero.z_index = HERO_VFX_Z_INDEX
	actor_layer.add_child(hero)

	_build_ui()
	_build_overlays()
	_build_audio_pool()


func _resolve_layout() -> void:
	var viewport_size := get_viewport_rect().size
	layout_size = Vector2(maxf(VIEW_SIZE.x, viewport_size.x),
		maxf(VIEW_SIZE.y, viewport_size.y))
	ground_y = _ground_y_for_height(layout_size.y)


func _bottom_panel_y_for_height(height: float) -> float:
	return maxf(0.0, height - BOTTOM_PANEL_HEIGHT)


func _ground_y_for_height(height: float) -> float:
	return BASE_GROUND_Y + maxf(0.0, height - VIEW_SIZE.y)


func _build_background() -> void:
	var layer := CanvasLayer.new()
	layer.layer = -10
	add_child(layer)
	background_sprite = Sprite2D.new()
	background_sprite.texture = BACKGROUNDS[int(progress.region)]
	background_sprite.position = layout_size * 0.5
	var cover_scale := maxf(layout_size.x / background_sprite.texture.get_width(),
		layout_size.y / background_sprite.texture.get_height())
	background_sprite.scale = Vector2.ONE * cover_scale
	background_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
	layer.add_child(background_sprite)

	background_readability = ColorRect.new()
	background_readability.position = Vector2.ZERO
	background_readability.size = layout_size
	background_readability.color = Color(0.018, 0.025, 0.07, 0.08)
	background_readability.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(background_readability)

	horizon_glow = Polygon2D.new()
	horizon_glow.polygon = PackedVector2Array([
		Vector2(0, ground_y - 252.0), Vector2(layout_size.x, ground_y - 252.0),
		Vector2(layout_size.x, _bottom_panel_y_for_height(layout_size.y) + 12.0),
		Vector2(0, _bottom_panel_y_for_height(layout_size.y) + 12.0),
	])
	horizon_glow.color = Color(0.48, 0.08, 0.19, 0.10)
	layer.add_child(horizon_glow)

	ambient_layer = Node2D.new()
	layer.add_child(ambient_layer)
	for index in range(18):
		var mote := Polygon2D.new()
		var radius := randf_range(1.4, 3.4)
		mote.polygon = PackedVector2Array([
			Vector2(-radius, 0), Vector2(0, -radius * 1.8),
			Vector2(radius, 0), Vector2(0, radius * 1.8),
		])
		mote.position = Vector2(randf_range(10, layout_size.x - 10.0),
			randf_range(330, ground_y + 108.0))
		mote.color = Color(1.0, randf_range(0.14, 0.42), 0.22, randf_range(0.16, 0.42))
		mote.set_meta("speed", randf_range(7.0, 19.0))
		mote.set_meta("phase", randf_range(0.0, TAU))
		ambient_layer.add_child(mote)


func _build_ui() -> void:
	var ui_layer := CanvasLayer.new()
	ui_layer.layer = 50
	add_child(ui_layer)
	ui_root = Control.new()
	ui_root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	ui_font_medium = _font_variation(620.0)
	ui_font_bold = _font_variation(760.0)
	var theme := Theme.new()
	theme.default_font = ui_font_medium
	theme.default_font_size = 19
	ui_root.theme = theme
	ui_layer.add_child(ui_root)
	var content_x := (layout_size.x - VIEW_SIZE.x) * 0.5

	var top_panel := Panel.new()
	top_panel.position = Vector2(content_x + 18, 18)
	top_panel.size = Vector2(684, 225)
	top_panel.add_theme_stylebox_override("panel", _panel_style(
		Color(0.012, 0.020, 0.058, 0.965), Color(0.72, 0.51, 0.25, 0.90), 22, 2))
	ui_root.add_child(top_panel)

	level_label = _label(top_panel, Vector2(20, 11), Vector2(112, 36), "LV.1", 29,
		Color(0.96, 0.98, 1.0))
	location_label = _label(top_panel, Vector2(128, 14), Vector2(350, 31), "", 20,
		Color(0.82, 0.89, 0.98))
	currency_label = _label(top_panel, Vector2(480, 16), Vector2(132, 30), "◆ 0", 20,
		Color(1.0, 0.79, 0.29), HORIZONTAL_ALIGNMENT_RIGHT)

	var pause_button := _button(top_panel, Vector2(620, 7), Vector2(48, 48), "Ⅱ", 20)
	pause_button.pressed.connect(_toggle_pause)
	var header_divider := ColorRect.new()
	header_divider.position = Vector2(18, 54)
	header_divider.size = Vector2(590, 1)
	header_divider.color = Color(0.57, 0.70, 0.92, 0.20)
	header_divider.mouse_filter = Control.MOUSE_FILTER_IGNORE
	top_panel.add_child(header_divider)
	var hp_bar := _bar(top_panel, Vector2(18, 61), Vector2(648, 25),
		Color(0.76, 0.035, 0.20), "생명")
	hp_fill = hp_bar[0]
	hp_text = hp_bar[1]
	var blood_bar := _bar(top_panel, Vector2(18, 94), Vector2(648, 22),
		Color(0.08, 0.66, 0.84), "혈기")
	blood_fill = blood_bar[0]
	blood_text = blood_bar[1]
	var xp_bar := _bar(top_panel, Vector2(18, 124), Vector2(648, 20),
		Color(0.48, 0.31, 0.88), "경험치")
	xp_fill = xp_bar[0]
	xp_text = xp_bar[1]
	objective_label = _label(top_panel, Vector2(22, 151), Vector2(440, 29), "", 18,
		Color(0.94, 0.96, 1.0))
	stats_label = _label(top_panel, Vector2(22, 184), Vector2(420, 27), "", 17,
		Color(0.72, 0.81, 0.94))
	var inventory_button := _button(top_panel, Vector2(463, 162), Vector2(94, 47), "가방", 16)
	inventory_button.add_theme_color_override("font_color", Color(0.25, 0.87, 1.0))
	inventory_button.pressed.connect(func(): _open_overlay(inventory_overlay))
	var growth_button := _button(top_panel, Vector2(566, 162), Vector2(96, 47), "성장 ▲", 16)
	growth_button.add_theme_color_override("font_color", Color(1.0, 0.76, 0.28))
	growth_button.pressed.connect(func(): _open_overlay(growth_overlay))

	story_panel = Panel.new()
	story_panel.position = Vector2(content_x + 28, 263)
	story_panel.size = Vector2(664, 96)
	story_panel.add_theme_stylebox_override("panel", _panel_style(
		Color(0.018, 0.032, 0.085, 0.94), Color(0.35, 0.79, 0.98, 0.80), 15, 2))
	ui_root.add_child(story_panel)
	story_speaker = _label(story_panel, Vector2(18, 8), Vector2(150, 27), "", 18,
		Color(0.35, 0.88, 1.0))
	story_line = _label(story_panel, Vector2(18, 35), Vector2(628, 54), "", 18,
		Color(0.91, 0.94, 0.99))
	story_line.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART

	boss_panel = Panel.new()
	boss_panel.position = Vector2(content_x + 60, 381)
	boss_panel.size = Vector2(600, 66)
	boss_panel.add_theme_stylebox_override("panel", _panel_style(
		Color(0.08, 0.015, 0.035, 0.92), Color(0.98, 0.58, 0.20, 0.82), 13, 2))
	boss_panel.visible = false
	ui_root.add_child(boss_panel)
	boss_title = _label(boss_panel, Vector2(16, 4), Vector2(568, 27), "", 18,
		Color(1.0, 0.83, 0.48), HORIZONTAL_ALIGNMENT_CENTER)
	var boss_bar := _bar(boss_panel, Vector2(17, 36), Vector2(566, 18),
		Color(0.92, 0.08, 0.21), "")
	boss_health_fill = boss_bar[0]
	boss_health_text = boss_bar[1]

	banner_label = _label(ui_root, Vector2(content_x + 90, 465), Vector2(540, 58), "", 24,
		Color(1.0, 0.82, 0.43), HORIZONTAL_ALIGNMENT_CENTER)
	banner_label.add_theme_color_override("font_shadow_color", Color(0.04, 0.01, 0.08, 0.98))
	banner_label.add_theme_constant_override("shadow_offset_x", 3)
	banner_label.add_theme_constant_override("shadow_offset_y", 3)
	banner_label.modulate.a = 0.0

	reward_toast = Panel.new()
	reward_toast.position = Vector2(content_x + 394, ground_y - 250.0)
	reward_toast.size = Vector2(300, 44)
	reward_toast.add_theme_stylebox_override("panel", _panel_style(
		Color(0.020, 0.025, 0.065, 0.94), Color(1.0, 0.64, 0.20, 0.82), 14, 1))
	reward_toast.mouse_filter = Control.MOUSE_FILTER_IGNORE
	reward_toast.visible = false
	ui_root.add_child(reward_toast)
	reward_toast_label = _label(reward_toast, Vector2(10, 3), Vector2(280, 38), "", 17,
		Color(1.0, 0.82, 0.38), HORIZONTAL_ALIGNMENT_CENTER)

	bottom_panel = Panel.new()
	bottom_panel.position = Vector2(0, _bottom_panel_y_for_height(layout_size.y))
	bottom_panel.size = Vector2(layout_size.x, BOTTOM_PANEL_HEIGHT)
	bottom_panel.add_theme_stylebox_override("panel", _panel_style(
		Color(0.008, 0.016, 0.048, 0.985), Color(0.20, 0.69, 0.86, 0.70), 0, 2))
	ui_root.add_child(bottom_panel)
	var bottom_content_x := (layout_size.x - VIEW_SIZE.x) * 0.5
	var auto_strip := Panel.new()
	auto_strip.position = Vector2(bottom_content_x + 24, 10)
	auto_strip.size = Vector2(672, 39)
	auto_strip.add_theme_stylebox_override("panel", _panel_style(
		Color(0.025, 0.056, 0.105, 0.96), Color(0.24, 0.84, 1.0, 0.88), 13, 1))
	bottom_panel.add_child(auto_strip)
	auto_label = _label(auto_strip, Vector2(12, 2), Vector2(648, 35),
		"●  AUTO HUNT   자동 추격 · 기본 공격 · 혈술 연계", 17,
		Color(0.35, 0.88, 1.0), HORIZONTAL_ALIGNMENT_CENTER)
	var wave_bar := _bar(bottom_panel, Vector2(bottom_content_x + 28, 57), Vector2(664, 24),
		Color(0.84, 0.19, 0.42), "")
	wave_fill = wave_bar[0]
	wave_text = wave_bar[1]
	_build_skill_grid(bottom_panel, bottom_content_x)

	screen_flash = ColorRect.new()
	screen_flash.position = Vector2.ZERO
	screen_flash.size = layout_size
	screen_flash.color = Color(0.92, 0.06, 0.25, 0.0)
	screen_flash.mouse_filter = Control.MOUSE_FILTER_IGNORE
	ui_root.add_child(screen_flash)


func _build_skill_grid(parent: Control, content_x: float = 0.0) -> void:
	for index in range(SKILL_NAMES.size()):
		var column := index % 4
		var row := int(index / 4)
		var button := Button.new()
		button.position = Vector2(content_x + 20 + column * 172, 92 + row * 121)
		button.size = SKILL_CARD_SIZE
		button.focus_mode = Control.FOCUS_NONE
		button.clip_contents = true
		button.add_theme_stylebox_override("normal", _panel_style(
			Color(0.020, 0.030, 0.075, 0.98), _skill_color(index, 0.84), 17, 2))
		button.add_theme_stylebox_override("hover", _panel_style(
			Color(0.055, 0.055, 0.12, 0.99), _skill_color(index, 0.98), 17, 2))
		button.add_theme_stylebox_override("pressed", _panel_style(
			Color(0.16, 0.025, 0.08, 0.99), Color(1.0, 0.72, 0.33), 17, 3))
		button.add_theme_stylebox_override("disabled", _panel_style(
			Color(0.018, 0.024, 0.055, 0.98), Color(0.30, 0.34, 0.47, 0.78), 17, 1))
		button.pressed.connect(_manual_skill.bind(index))
		parent.add_child(button)

		var accent := ColorRect.new()
		accent.position = Vector2(5, 16)
		accent.size = Vector2(3, 82)
		accent.color = _skill_color(index, 0.90)
		accent.mouse_filter = Control.MOUSE_FILTER_IGNORE
		button.add_child(accent)

		var icon_shell := Panel.new()
		icon_shell.position = SKILL_ICON_RECT.position
		icon_shell.size = SKILL_ICON_RECT.size
		icon_shell.clip_contents = true
		icon_shell.mouse_filter = Control.MOUSE_FILTER_IGNORE
		icon_shell.add_theme_stylebox_override("panel", _panel_style(
			Color(0.045, 0.038, 0.095, 0.98), _skill_color(index, 0.78), 12, 1))
		button.add_child(icon_shell)

		var icon := TextureRect.new()
		icon.texture = Atlases.frame(SKILL_ICONS, 4, 2, column, row)
		icon.position = Vector2(4, 4)
		icon.size = Vector2(58, 58)
		icon.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		icon.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		icon.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
		icon.mouse_filter = Control.MOUSE_FILTER_IGNORE
		icon_shell.add_child(icon)

		var cooldown := ColorRect.new()
		cooldown.position = Vector2(4, 4)
		cooldown.size = Vector2(58, 0)
		cooldown.color = Color(0.004, 0.008, 0.028, 0.80)
		cooldown.mouse_filter = Control.MOUSE_FILTER_IGNORE
		icon_shell.add_child(cooldown)
		var cooldown_label := _label(icon_shell, Vector2(4, 4), Vector2(58, 58), "", 20,
			Color(1.0, 0.94, 0.84), HORIZONTAL_ALIGNMENT_CENTER)

		var title := _label(button, Vector2(84, 8), Vector2(72, 29), SKILL_NAMES[index], 20,
			Color(0.97, 0.98, 1.0), HORIZONTAL_ALIGNMENT_LEFT)
		title.mouse_filter = Control.MOUSE_FILTER_IGNORE
		var skill_meta := _label(button, Vector2(84, 39), Vector2(72, 26),
			"%s ·%d" % [SKILL_SUBTITLES[index], SKILL_BLOOD_COSTS[index]], 16,
			_skill_color(index, 0.98),
			HORIZONTAL_ALIGNMENT_LEFT)
		skill_meta.mouse_filter = Control.MOUSE_FILTER_IGNORE
		var state_back := ColorRect.new()
		state_back.position = Vector2(12, 80)
		state_back.size = Vector2(140, 25)
		state_back.color = Color(0.035, 0.075, 0.12, 0.90)
		state_back.mouse_filter = Control.MOUSE_FILTER_IGNORE
		button.add_child(state_back)
		var state := _label(button, Vector2(12, 79), Vector2(140, 27), "AUTO · 준비", 16,
			Color(0.37, 0.90, 1.0), HORIZONTAL_ALIGNMENT_CENTER)
		state.mouse_filter = Control.MOUSE_FILTER_IGNORE
		button.set_meta("icon_rect", SKILL_ICON_RECT)

		skill_buttons.append(button)
		skill_cooldown_overlays.append(cooldown)
		skill_state_labels.append(state)
		skill_cooldown_labels.append(cooldown_label)


func _build_overlays() -> void:
	growth_overlay = _overlay_shell("혈맥 성장")
	var growth_panel: Panel = growth_overlay.get_meta("panel")
	_label(growth_panel, Vector2(36, 78), Vector2(572, 52),
		"전투 중에도 성장은 즉시 반영됩니다. 네 능력 중 원하는 혈맥을 강화하세요.", 17,
		Color(0.70, 0.78, 0.90)).autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	for index in range(STAT_KEYS.size()):
		var y := 151.0 + index * 105.0
		var row_panel := Panel.new()
		row_panel.position = Vector2(34, y)
		row_panel.size = Vector2(576, 87)
		row_panel.add_theme_stylebox_override("panel", _panel_style(
			Color(0.025, 0.04, 0.09, 0.94), Color(0.22, 0.52, 0.68, 0.58), 14, 1))
		growth_panel.add_child(row_panel)
		_label(row_panel, Vector2(18, 12), Vector2(92, 28), STAT_NAMES[index], 20,
			_skill_color(index + 1, 1.0))
		var stat_label := _label(row_panel, Vector2(115, 11), Vector2(275, 58), "", 16,
			Color(0.82, 0.87, 0.96))
		stat_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		growth_stat_labels.append(stat_label)
		var upgrade := _button(row_panel, Vector2(408, 14), Vector2(150, 58), "강화", 17)
		upgrade.pressed.connect(_upgrade_stat.bind(index))
		growth_buttons.append(upgrade)
	_label(growth_panel, Vector2(36, 592), Vector2(572, 80),
		"혈술은 영웅 레벨에 따라 자동 개방됩니다. 레벨 9부터 8개 혈술을 모두 자동 사용합니다.",
		16, Color(0.68, 0.80, 0.94)).autowrap_mode = TextServer.AUTOWRAP_WORD_SMART

	inventory_overlay = _overlay_shell("혈흔 가방")
	var inventory_panel: Panel = inventory_overlay.get_meta("panel")
	for slot in range(3):
		var equipped_panel := Panel.new()
		equipped_panel.position = Vector2(36 + slot * 190, 76)
		equipped_panel.size = Vector2(174, 76)
		equipped_panel.add_theme_stylebox_override("panel", _panel_style(
			Color(0.025, 0.038, 0.088, 0.97), _skill_color(slot * 2, 0.70), 14, 1))
		inventory_panel.add_child(equipped_panel)
		var icon_shell := Panel.new()
		icon_shell.position = Vector2(8, 8)
		icon_shell.size = Vector2(60, 60)
		icon_shell.mouse_filter = Control.MOUSE_FILTER_IGNORE
		icon_shell.add_theme_stylebox_override("panel", _panel_style(
			Color(0.006, 0.012, 0.038, 0.98), _skill_color(slot * 2, 0.88), 30, 2))
		equipped_panel.add_child(icon_shell)
		var icon := TextureRect.new()
		icon.texture = Atlases.frame(EQUIPMENT_ICONS, 3, 1, slot, 0)
		icon.position = Vector2(3, 3)
		icon.size = Vector2(54, 54)
		icon.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		icon.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		icon.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
		icon.mouse_filter = Control.MOUSE_FILTER_IGNORE
		icon_shell.add_child(icon)
		inventory_equipped_icons.append(icon)
		var equipped_label := _label(equipped_panel, Vector2(74, 8), Vector2(92, 60),
			"", 16, Color(0.92, 0.95, 1.0), HORIZONTAL_ALIGNMENT_CENTER)
		equipped_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		inventory_equipped_labels.append(equipped_label)
	inventory_equipment_label = _label(inventory_panel, Vector2(36, 157), Vector2(572, 39),
		"", 17, Color(0.92, 0.95, 1.0), HORIZONTAL_ALIGNMENT_CENTER)
	_label(inventory_panel, Vector2(36, 201), Vector2(572, 28), "획득한 전리품", 19,
		Color(1.0, 0.77, 0.30))
	for index in range(6):
		var column := index % 2
		var row := int(index / 2)
		var slot_panel := Panel.new()
		slot_panel.position = Vector2(36 + column * 290, 237 + row * 106)
		slot_panel.size = Vector2(274, 88)
		slot_panel.add_theme_stylebox_override("panel", _panel_style(
			Color(0.025, 0.036, 0.082, 0.95), Color(0.25, 0.48, 0.64, 0.65), 13, 1))
		inventory_panel.add_child(slot_panel)
		inventory_slot_panels.append(slot_panel)
		var slot_icon := TextureRect.new()
		slot_icon.texture = Atlases.frame(EQUIPMENT_ICONS, 3, 1, index % 3, 0)
		slot_icon.position = Vector2(10, 10)
		slot_icon.size = Vector2(68, 68)
		slot_icon.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		slot_icon.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		slot_icon.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
		slot_icon.mouse_filter = Control.MOUSE_FILTER_IGNORE
		slot_panel.add_child(slot_icon)
		inventory_slot_icons.append(slot_icon)
		var slot_label := _label(slot_panel, Vector2(88, 8), Vector2(174, 72), "", 16,
			Color(0.76, 0.82, 0.91))
		slot_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		inventory_slot_labels.append(slot_label)
	_label(inventory_panel, Vector2(36, 566), Vector2(572, 82),
		"더 강한 장비는 즉시 착용됩니다. 가방이 가득 차면 같은 부위의 가장 약한 전리품을 교체합니다.",
		16, Color(0.65, 0.77, 0.91)).autowrap_mode = TextServer.AUTOWRAP_WORD_SMART

	pause_overlay = _overlay_shell("일시 정지", 460)
	var pause_panel: Panel = pause_overlay.get_meta("panel")
	_label(pause_panel, Vector2(40, 112), Vector2(564, 70),
		"밤의 행군이 멈췄습니다. 저장은 자동으로 유지됩니다.", 19,
		Color(0.83, 0.88, 0.96), HORIZONTAL_ALIGNMENT_CENTER).autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	var resume := _button(pause_panel, Vector2(132, 215), Vector2(380, 68), "전투 계속", 20)
	resume.pressed.connect(_toggle_pause)

	offline_overlay = _overlay_shell("밤의 원정 보고", 500)
	var offline_panel: Panel = offline_overlay.get_meta("panel")
	offline_panel.set_meta("message", _label(offline_panel, Vector2(42, 118), Vector2(560, 160),
		"", 18, Color(0.90, 0.93, 1.0), HORIZONTAL_ALIGNMENT_CENTER))
	var accept := _button(offline_panel, Vector2(132, 312), Vector2(380, 68), "보상 받기", 20)
	accept.pressed.connect(func(): _close_overlay(offline_overlay))


func _overlay_shell(title: String, panel_height: float = 760.0) -> Control:
	var overlay := Control.new()
	overlay.position = Vector2.ZERO
	overlay.size = layout_size
	overlay.visible = false
	overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	ui_root.add_child(overlay)
	var dim := ColorRect.new()
	dim.position = Vector2.ZERO
	dim.size = layout_size
	dim.color = Color(0.005, 0.008, 0.025, 0.88)
	dim.mouse_filter = Control.MOUSE_FILTER_STOP
	overlay.add_child(dim)
	var panel := Panel.new()
	panel.position = Vector2((layout_size.x - 644.0) * 0.5,
		(layout_size.y - panel_height) * 0.5)
	panel.size = Vector2(644, panel_height)
	panel.add_theme_stylebox_override("panel", _panel_style(
		Color(0.018, 0.026, 0.068, 0.985), Color(0.53, 0.39, 0.71, 0.88), 24, 2))
	overlay.add_child(panel)
	_label(panel, Vector2(28, 20), Vector2(500, 45), title, 29,
		Color(1.0, 0.83, 0.46))
	var close_button := _button(panel, Vector2(560, 16), Vector2(58, 50), "×", 25)
	close_button.pressed.connect(_close_overlay.bind(overlay))
	overlay.set_meta("panel", panel)
	return overlay


func _label(parent: Node, position_value: Vector2, size_value: Vector2, text_value: String,
		font_size: int, color: Color, alignment: HorizontalAlignment = HORIZONTAL_ALIGNMENT_LEFT) -> Label:
	var result := Label.new()
	result.position = position_value
	result.size = size_value
	result.text = text_value
	result.horizontal_alignment = alignment
	result.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	if ui_font_medium:
		result.add_theme_font_override("font", ui_font_bold if font_size >= 16 else ui_font_medium)
	result.add_theme_font_size_override("font_size", font_size)
	result.add_theme_color_override("font_color", color)
	result.add_theme_color_override("font_outline_color", Color(0.0, 0.004, 0.025, 0.96))
	result.add_theme_constant_override("outline_size", 2)
	result.add_theme_color_override("font_shadow_color", Color(0.0, 0.0, 0.015, 0.94))
	result.add_theme_constant_override("shadow_offset_x", 2)
	result.add_theme_constant_override("shadow_offset_y", 2)
	result.mouse_filter = Control.MOUSE_FILTER_IGNORE
	parent.add_child(result)
	return result


func _button(parent: Node, position_value: Vector2, size_value: Vector2,
		text_value: String, font_size: int) -> Button:
	var result := Button.new()
	result.position = position_value
	result.size = size_value
	result.text = text_value
	result.focus_mode = Control.FOCUS_NONE
	if ui_font_bold:
		result.add_theme_font_override("font", ui_font_bold)
	result.add_theme_font_size_override("font_size", font_size)
	result.add_theme_color_override("font_color", Color(0.88, 0.92, 0.98))
	result.add_theme_color_override("font_outline_color", Color(0.0, 0.004, 0.025, 0.96))
	result.add_theme_constant_override("outline_size", 2)
	result.add_theme_color_override("font_shadow_color", Color(0.0, 0.0, 0.02, 0.88))
	result.add_theme_constant_override("shadow_offset_x", 1)
	result.add_theme_constant_override("shadow_offset_y", 1)
	result.add_theme_stylebox_override("normal", _panel_style(
		Color(0.035, 0.052, 0.11, 0.96), Color(0.31, 0.52, 0.68, 0.75), 13, 1))
	result.add_theme_stylebox_override("hover", _panel_style(
		Color(0.07, 0.075, 0.15, 0.98), Color(0.40, 0.80, 0.94, 0.92), 13, 2))
	result.add_theme_stylebox_override("pressed", _panel_style(
		Color(0.14, 0.025, 0.08, 0.98), Color(0.96, 0.36, 0.57, 0.95), 13, 2))
	parent.add_child(result)
	return result


func _bar(parent: Node, position_value: Vector2, size_value: Vector2,
		color: Color, prefix: String) -> Array:
	var back := ColorRect.new()
	back.position = position_value
	back.size = size_value
	back.color = Color(0.010, 0.016, 0.046, 0.97)
	back.mouse_filter = Control.MOUSE_FILTER_IGNORE
	parent.add_child(back)
	var fill := ColorRect.new()
	fill.position = Vector2(2, 2)
	fill.size = size_value - Vector2(4, 4)
	fill.color = color
	fill.mouse_filter = Control.MOUSE_FILTER_IGNORE
	back.add_child(fill)
	var text_label := _label(back, Vector2.ZERO, size_value, prefix,
		maxi(17, int(size_value.y * 0.72)),
		Color(0.96, 0.97, 1.0), HORIZONTAL_ALIGNMENT_CENTER)
	return [fill, text_label]


func _panel_style(color: Color, border_color: Color, radius: int, width: int) -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = color
	style.border_color = border_color
	style.set_border_width_all(width)
	style.set_corner_radius_all(radius)
	style.content_margin_left = 8
	style.content_margin_right = 8
	style.content_margin_top = 6
	style.content_margin_bottom = 6
	return style


func _skill_color(index: int, alpha: float) -> Color:
	var colors := [
		Color(0.96, 0.13, 0.38), Color(0.08, 0.82, 0.95), Color(0.69, 0.25, 1.0),
		Color(1.0, 0.31, 0.20), Color(0.91, 0.16, 0.62), Color(0.26, 0.73, 1.0),
		Color(0.96, 0.55, 0.11), Color(0.58, 0.29, 1.0),
	]
	var result: Color = colors[index % colors.size()]
	result.a = alpha
	return result


func _font_variation(weight: float) -> FontVariation:
	var result := FontVariation.new()
	result.base_font = UI_FONT
	result.variation_opentype = {&"wght": weight}
	return result


func _build_audio_pool() -> void:
	bgm_player = AudioStreamPlayer.new()
	bgm_player.stream = BGM
	bgm_player.volume_db = -9.5
	bgm_player.process_mode = Node.PROCESS_MODE_ALWAYS
	bgm_player.finished.connect(func(): bgm_player.play())
	add_child(bgm_player)
	for index in range(14):
		var player := AudioStreamPlayer.new()
		player.volume_db = -7.0
		add_child(player)
		sfx_pool.append(player)


func _start_audio() -> void:
	if bgm_player and not bgm_player.playing:
		bgm_player.play()


func _update_bgm_watchdog(delta: float) -> void:
	bgm_watchdog_timer += delta
	if bgm_watchdog_timer < 1.0:
		return
	bgm_watchdog_timer = 0.0
	if bgm_player and bgm_player.stream and not bgm_player.playing:
		bgm_player.play()


func _play_sfx(stream: AudioStream, volume_db: float = -7.0, pitch: float = 1.0) -> void:
	for player in sfx_pool:
		if not player.playing:
			player.stream = stream
			player.volume_db = volume_db
			player.pitch_scale = pitch
			player.play()
			return


func _apply_progress_to_runtime(full_restore: bool) -> void:
	var maximum := _hero_max_health()
	if full_restore:
		hero_hp = maximum
	else:
		hero_hp = mini(maximum, maxi(1, hero_hp))
	if full_restore or hero_blood <= 0.0:
		hero_blood = float(_hero_max_blood())
	hero.set_health(hero_hp, maximum)


func _hero_max_health() -> int:
	return Rules.hero_max_health(int(progress.level), int(progress.vitality_level))


func _hero_max_blood() -> int:
	return Rules.hero_max_blood(int(progress.level), int(progress.blood_level),
		int(progress.relic_power))


func _attack_power() -> int:
	return Rules.hero_attack_power(int(progress.level), int(progress.might_level),
		int(progress.weapon_power))


func _start_wave() -> void:
	_clear_enemies()
	wave_total = Rules.wave_enemy_count(int(progress.region), int(progress.wave))
	wave_remaining = wave_total
	wave_defeated = 0
	spawn_serial = 0
	wave_transition_timer = -1.0
	story_midpoint_shown = false
	hero.position = Vector2(HERO_START_X, ground_y)
	hero.modulate = Color.WHITE
	hero.dead = false
	hero.play_animation(&"idle", true)
	background_sprite.texture = BACKGROUNDS[int(progress.region)]
	_show_banner("WAVE %d  ·  %s" % [int(progress.wave), Stories.REGION_NAMES[int(progress.region)]])
	_fill_reinforcements(true)


func _clear_enemies() -> void:
	for enemy in enemies:
		if is_instance_valid(enemy):
			enemy.queue_free()
	enemies.clear()
	enemy_meta.clear()
	boss_panel.visible = false


func _fill_reinforcements(initial: bool = false) -> void:
	if wave_remaining <= 0 or hero_down_timer > 0.0:
		return
	var count := Rules.reinforcement_batch_size(enemies.size(), wave_remaining,
		MAX_ACTIVE_ENEMIES)
	if initial:
		count = mini(INITIAL_NORMAL_SPAWN_COUNT if int(progress.wave) < Rules.WAVES_PER_REGION \
			else INITIAL_BOSS_SPAWN_COUNT, wave_remaining)
	for index in range(count):
		_spawn_enemy()


func _spawn_enemy() -> void:
	if wave_remaining <= 0:
		return
	spawn_serial += 1
	wave_remaining -= 1
	var boss := int(progress.wave) == Rules.WAVES_PER_REGION and spawn_serial == 1
	var elite := not boss and Rules.is_elite_spawn(int(progress.wave), spawn_serial, wave_total)
	var kind := Rules.ENEMY_BOSS if boss else (spawn_serial + int(progress.wave) + int(progress.region)) % 3
	if elite:
		kind = Rules.ENEMY_WRAITH
	var actor := ActorScene.new()
	var frames: SpriteFrames
	var actor_scale := 0.41
	var actor_name := ""
	if boss:
		frames = Atlases.boss_frames(BOSS_TEXTURES[int(progress.region)])
		actor_scale = 0.88
		actor_name = Stories.BOSS_NAMES[int(progress.region)]
	else:
		frames = Atlases.enemy_frames(ENEMY_TEXTURE, kind)
		actor_scale = 0.48 if elite else 0.41 + kind * 0.012
		actor_name = "정예 · 피안개 추적자" if elite else ""
	actor.configure(frames, actor_scale, kind, actor_name, false, boss, elite)
	actor.position = Vector2(MONSTER_SPAWN_BASE_X + randf_range(0.0, 38.0) \
		+ enemies.size() * MONSTER_SPAWN_SPACING,
		ground_y + randf_range(-12.0, 13.0))
	actor.z_index = 7 + int(actor.position.y - ground_y) / 6
	actor.set_facing_right(false)
	var maximum := Rules.enemy_max_health(kind, int(progress.region), int(progress.wave),
		int(progress.level), int(progress.chapter_clears))
	if elite:
		maximum = int(maximum * 2.4)
	actor.set_health(maximum, maximum)
	actor_layer.add_child(actor)
	enemies.append(actor)
	enemy_meta[actor] = {
		"attack_timer": randf_range(0.7, 1.5),
		"heavy_serial": 0,
		"phase": false,
		"boss": boss,
		"elite": elite,
		"kind": kind,
	}
	if boss:
		boss_panel.visible = true
		boss_title.text = "%s  ·  %s" % [Stories.BOSS_NAMES[int(progress.region)],
			Stories.BOSS_EPITHETS[int(progress.region)]]
		_spawn_boss_arrival(actor)
		_show_banner("BOSS  ·  %s" % Stories.BOSS_NAMES[int(progress.region)])


func _update_wave_flow(delta: float) -> void:
	if wave_transition_timer >= 0.0:
		wave_transition_timer -= delta
		if wave_transition_timer <= 0.0:
			_advance_wave()
		return
	if enemies.size() < MAX_ACTIVE_ENEMIES and wave_remaining > 0:
		_fill_reinforcements()
	if not story_midpoint_shown and wave_defeated >= maxi(4, int(wave_total * 0.5)):
		story_midpoint_shown = true
		_show_story(Stories.midpoint(int(progress.region), int(progress.wave)))
	if wave_remaining == 0 and enemies.is_empty():
		wave_transition_timer = 1.15
		_show_banner("WAVE CLEAR  ·  피의 길이 열렸습니다")
		hero.set_running(false)


func _advance_wave() -> void:
	if int(progress.wave) >= Rules.WAVES_PER_REGION:
		progress.boss_kills = int(progress.boss_kills) + 1
		progress.region = int(progress.region) + 1
		progress.wave = 1
		if int(progress.region) >= Rules.REGION_COUNT:
			progress.region = 0
			progress.chapter_clears = int(progress.chapter_clears) + 1
			_show_banner("CHAPTER CLEAR  ·  붉은 밤이 다시 시작됩니다")
	else:
		progress.wave = int(progress.wave) + 1
	hero_hp = mini(_hero_max_health(), hero_hp + int(_hero_max_health() * 0.30))
	hero_blood = float(_hero_max_blood())
	_save_progress()
	_start_wave()
	_show_story(Stories.opening(int(progress.region), int(progress.wave),
		int(progress.chapter_clears)))


func _update_enemies(delta: float) -> void:
	var alive: Array = []
	for enemy in enemies:
		if is_instance_valid(enemy) and not enemy.dead:
			alive.append(enemy)
	alive.sort_custom(func(first, second): return first.position.x < second.position.x)
	for index in range(alive.size()):
		var enemy: CombatActor = alive[index]
		var meta: Dictionary = enemy_meta.get(enemy, {})
		if meta.is_empty():
			continue
		var is_boss: bool = bool(meta.get("boss", false))
		# The global boss bar carries boss health. In-world bars are reserved for the
		# current target and a nearby elite so crowded waves remain readable.
		enemy.set_health_bar_visible(not is_boss and
			(index == 0 or (bool(meta.get("elite", false)) and index <= 2)))
		var spacing := 36.0 if is_boss else ENEMY_COMBAT_SPACING
		var desired_x := maxf(COMBAT_LINE_X + index * spacing,
			hero.position.x + (116.0 if is_boss else 82.0) + index * 8.0)
		if enemy.position.x > desired_x + 3.0:
			var speed := 98.0 if is_boss else 142.0 + int(meta.kind) * 12.0
			enemy.position.x = move_toward(enemy.position.x, desired_x, speed * delta)
			enemy.set_running(true)
			enemy.animate_stride(delta, speed / 175.0)
		else:
			enemy.set_running(false)
			enemy.animate_stride(delta, 0.0)
		meta.attack_timer = float(meta.attack_timer) - delta
		if index <= 3 and enemy.position.x - hero.position.x < (135.0 if is_boss else 102.0) \
				and float(meta.attack_timer) <= 0.0:
			_enemy_attack(enemy, meta)
		var health_ratio := enemy.hp / float(enemy.max_hp)
		if is_boss and health_ratio <= 0.55 and not bool(meta.phase):
			meta.phase = true
			_boss_phase_shift(enemy)
		enemy_meta[enemy] = meta


func _enemy_attack(enemy: CombatActor, meta: Dictionary) -> void:
	meta.heavy_serial = int(meta.heavy_serial) + 1
	var boss: bool = bool(meta.boss)
	var heavy := boss and int(meta.heavy_serial) % 3 == 0
	meta.attack_timer = 1.15 if not boss else (1.45 if heavy else 1.05)
	enemy.play_animation(&"special" if heavy else &"attack", true)
	enemy.attack_lunge(-1.0, 34.0 if boss else 18.0)
	var damage := Rules.enemy_damage(int(meta.kind), int(progress.region), int(progress.wave),
		int(progress.level), int(progress.chapter_clears), heavy)
	damage = Rules.mitigate_damage(damage, int(progress.armor_power))
	hero_hp = maxi(0, hero_hp - damage)
	hero.set_health(hero_hp, _hero_max_health())
	hero.flash_hit(0.7)
	_spawn_damage_label(hero.position + Vector2(0, -165), damage, Color(1.0, 0.35, 0.31), false)
	_spawn_impact(hero.position + Vector2(18, -82), 1 if heavy else 0, 0.72 if heavy else 0.52)
	_play_sfx(SFX_HERO_HURT, -7.0, randf_range(0.96, 1.04))
	shake_energy = maxf(shake_energy, 3.2 if heavy else 1.4)
	if hero_hp <= 0:
		_hero_fall()


func _update_hero(delta: float) -> void:
	if hero.dead or enemies.is_empty():
		hero.set_running(false)
		return
	var target := _nearest_enemy()
	if target == null:
		return
	var desired_x := minf(HERO_FIRST_QUARTER_X,
		target.position.x - (110.0 if target.is_boss else 78.0))
	var distance := desired_x - hero.position.x
	if distance > 5.0:
		var fast_dash := distance > 190.0
		var speed := 530.0 if fast_dash else 275.0
		hero.position.x = move_toward(hero.position.x, desired_x, speed * delta)
		hero.set_facing_right(true)
		hero.set_running(true)
		hero.animate_stride(delta, speed / 530.0)
		trail_timer -= delta
		if fast_dash:
			dash_motion_timer -= delta
			if dash_motion_timer <= 0.0:
				dash_motion_timer = 0.15
				hero.play_animation(&"dash", true)
				_spawn_motion_echo(hero, Color(0.18, 0.76, 1.0, 0.34), 0.20)
				_play_sfx(SFX_DASH, -14.0, 1.08)
		if trail_timer <= 0.0:
			trail_timer = 0.075 if fast_dash else 0.14
			_spawn_run_dust(hero.position + Vector2(-16, -5), fast_dash)
		return
	hero.set_running(false)
	hero.animate_stride(delta, 0.0)
	hero_attack_timer -= delta
	if hero_attack_timer <= 0.0:
		_hero_melee(target)


func _hero_melee(target: CombatActor) -> void:
	if target == null or target.dead:
		return
	hero_combo = (hero_combo + 1) % 3
	hero_attack_timer = 0.37 if hero_combo < 2 else 0.48
	hero.play_animation(StringName("attack_%d" % (hero_combo + 1)), true)
	hero.attack_lunge(1.0, 22.0 if hero_combo < 2 else 34.0)
	_spawn_motion_echo(hero, Color(0.86, 0.10, 0.34, 0.26), 0.16)
	var damage := Rules.melee_damage(_attack_power(), hero_combo)
	_apply_damage(target, damage, hero_combo == 2, Color(1.0, 0.34, 0.52))
	hero_blood = minf(float(_hero_max_blood()), hero_blood + 7.0 + hero_combo * 2.0)
	_play_sfx(SFX_HIT_HEAVY if hero_combo == 2 else SFX_HIT_LIGHT,
		-5.0 if hero_combo == 2 else -8.0, randf_range(0.96, 1.08))
	hit_stop = 0.034 if hero_combo == 2 else 0.018
	shake_energy = maxf(shake_energy, 2.5 if hero_combo == 2 else 1.05)


func _nearest_enemy() -> CombatActor:
	var result: CombatActor
	var best_x := INF
	for enemy in enemies:
		if is_instance_valid(enemy) and not enemy.dead and enemy.position.x < best_x:
			best_x = enemy.position.x
			result = enemy
	return result


func _recover_hero(delta: float) -> void:
	var recovery := Rules.recovery_per_second(int(progress.recovery_level))
	hero_recovery_bank += recovery * delta
	if hero_recovery_bank >= 1.0:
		var recovered := int(hero_recovery_bank)
		hero_recovery_bank -= recovered
		hero_hp = mini(_hero_max_health(), hero_hp + recovered)
	hero_blood = minf(float(_hero_max_blood()), hero_blood + delta * (3.8 + int(progress.blood_level) * 0.12))
	hero.set_health(hero_hp, _hero_max_health())


func _hero_fall() -> void:
	if hero_down_timer > 0.0:
		return
	hero_down_timer = 2.1
	hero.dead = true
	hero.play_animation(&"death", true)
	_show_banner("피의 맹세가 카엘을 다시 일으킵니다")
	hunt_chain = 0
	progress.gold = maxi(0, int(progress.gold) - mini(120, int(progress.gold) / 20))
	_save_progress()


func _revive_hero() -> void:
	_clear_enemies()
	hero.dead = false
	hero.modulate = Color.WHITE
	hero.position = Vector2(HERO_START_X, ground_y)
	hero_hp = _hero_max_health()
	hero_blood = float(_hero_max_blood())
	hero.set_health(hero_hp, _hero_max_health())
	hero.play_animation(&"idle", true)
	wave_remaining = wave_total
	wave_defeated = 0
	spawn_serial = 0
	wave_transition_timer = -1.0
	_fill_reinforcements(true)


func _update_auto_skills() -> void:
	if enemies.is_empty() or hero.dead or auto_skill_cast_timer > 0.0:
		return
	var nearest := _nearest_enemy()
	if nearest == null:
		return
	# Start from the skill after the previous cast. This prevents the short-cooldown
	# first slot from starving the rest when several arts become ready between waves.
	for offset in range(skill_timers.size()):
		var index := (auto_skill_cursor + offset) % skill_timers.size()
		if _auto_skill_state(index, nearest) == &"ready":
			_cast_skill(index)
			automatic_skill_casts += 1
			auto_skill_cursor = (index + 1) % skill_timers.size()
			return


func _auto_skill_state(index: int, target: CombatActor = null) -> StringName:
	if index < 0 or index >= skill_timers.size() or not _skill_unlocked(index):
		return &"locked"
	if skill_timers[index] > 0.0:
		return &"cooldown"
	if hero_blood < SKILL_BLOOD_COSTS[index]:
		return &"blood"
	if game_paused:
		return &"paused"
	if hero == null or hero.dead:
		return &"down"
	if target == null or not is_instance_valid(target) or target.dead:
		return &"target"
	# Auto arts are allowed as soon as the leading monster visibly enters the battle view.
	# The previous absolute 520 px gate left cards saying READY while the hero kept walking.
	if target.position.x > AUTO_SKILL_ENTRY_X:
		return &"approach"
	if auto_skill_cast_timer > 0.0:
		return &"chain"
	return &"ready"


func _manual_skill(index: int) -> void:
	_play_sfx(SFX_TAP, -15.0, 1.0)
	if game_paused or not _skill_unlocked(index) or skill_timers[index] > 0.0:
		return
	if hero_blood < SKILL_BLOOD_COSTS[index] or enemies.is_empty():
		_show_banner("혈기가 부족합니다")
		return
	var nearest := _nearest_enemy()
	if nearest == null or nearest.position.x > SKILL_ENGAGE_X:
		_show_banner("적이 혈술 사거리로 들어오고 있습니다")
		return
	_cast_skill(index)


func _skill_unlocked(index: int) -> bool:
	return int(progress.level) >= SKILL_UNLOCK_LEVELS[index]


func _skill_level(index: int) -> int:
	match index:
		0: return maxi(1, int(progress.spear_level))
		1: return maxi(1, int(progress.siphon_level))
		2: return maxi(1, int(progress.nova_level))
		_: return clampi(1 + int(progress.level) / 6, 1, 12)


func _cast_skill(index: int) -> void:
	var target := _nearest_enemy()
	if target == null:
		return
	skill_timers[index] = SKILL_COOLDOWNS[index]
	auto_skill_cast_timer = AUTO_SKILL_CAST_GAP
	hero_blood = maxf(0.0, hero_blood - SKILL_BLOOD_COSTS[index])
	var base_damage := Rules.skill_damage(index, _attack_power(), int(progress.level),
		_skill_level(index))
	_show_skill_callout(index)
	match index:
		0: _skill_blood_spear(target, base_damage)
		1: _skill_siphon(target, base_damage)
		2: _skill_nova(target, base_damage)
		3: _skill_vein_rush(base_damage)
		4: _skill_scarlet_rain(target, base_damage)
		5: _skill_chain(base_damage)
		6: _skill_pillar(target, base_damage)
		7: _skill_eclipse(target, base_damage)


func _show_skill_callout(index: int) -> void:
	var state := skill_state_labels[index]
	state.text = "AUTO · 발동"
	state.add_theme_color_override("font_color", Color(1.0, 0.82, 0.38))
	var button := skill_buttons[index]
	button.pivot_offset = button.size * 0.5
	var tween := create_tween()
	tween.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	tween.tween_property(button, "scale", Vector2(1.035, 1.035), 0.07)
	tween.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN_OUT)
	tween.tween_property(button, "scale", Vector2.ONE, 0.11)


func _skill_blood_spear(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	hero.attack_lunge(1.0, 20.0)
	_play_sfx(SFX_SPEAR, -4.5, 1.04)
	var start := hero.position + Vector2(48, -105)
	var finish := target.position + Vector2(8, -92)
	var visual_finish := _safe_effect_position(EFFECT_A, finish, 0.90, 4)
	var effect := _effect_sprite(EFFECT_A, 0, start, 0.55,
		Color(1.0, 0.76, 0.88), 0.90)
	effect.rotation = (visual_finish - effect.position).angle()
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_CUBIC).set_ease(Tween.EASE_IN)
	tween.tween_property(effect, "position", visual_finish, 0.17)
	tween.tween_property(effect, "scale", Vector2(0.90, 0.58), 0.17)
	_spawn_motion_echo(hero, Color(0.93, 0.08, 0.34, 0.32), 0.18)
	var targets := _front_targets(3)
	for index in range(targets.size()):
		var enemy: CombatActor = targets[index]
		_apply_damage(enemy, int(damage * (1.0 - index * 0.14)), index == 0,
			Color(1.0, 0.22, 0.42))
	_spawn_streak(start, visual_finish, Color(1.0, 0.09, 0.34, 0.86), 13.0)
	_spawn_skill_impact(visual_finish, Color(1.0, 0.08, 0.34, 0.96), 0.88)
	shake_energy = maxf(shake_energy, 2.0)


func _skill_siphon(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_SIPHON, -5.0, 0.98)
	var center := _safe_effect_position(EFFECT_A,
		target.position + Vector2(0, -92), 0.70, 4)
	var hero_center := _safe_effect_position(EFFECT_A,
		hero.position + Vector2(20, -100), 0.70, 4)
	for orbit in range(3):
		var effect := _effect_sprite(EFFECT_A, 1, center, 0.42 + orbit * 0.13,
			Color(0.45, 0.92, 1.0, 0.88))
		effect.rotation = orbit * TAU / 3.0
		var tween := create_tween().set_parallel(true)
		tween.tween_property(effect, "rotation", effect.rotation + TAU * (1.0 if orbit % 2 == 0 else -1.0), 0.42)
		tween.tween_property(effect, "position", hero_center, 0.40).set_delay(0.06 * orbit)
	var total_drained := 0
	for enemy in _nearby_targets(target.position.x, 270.0, 5):
		total_drained += mini(enemy.hp, int(damage * 0.72))
		_apply_damage(enemy, int(damage * 0.72), false, Color(0.24, 0.88, 1.0))
	var healed := hero.heal(maxi(5, int(total_drained * 0.12)))
	hero_hp = hero.hp
	if healed > 0:
		_spawn_damage_label(hero.position + Vector2(0, -190), healed,
			Color(0.32, 1.0, 0.75), false, true)
	_spawn_burst(hero.position + Vector2(0, -90), Color(0.19, 0.89, 1.0, 0.84), 18, 135.0)
	_spawn_skill_impact(center, Color(0.20, 0.90, 1.0, 0.92), 0.72)


func _skill_nova(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_NOVA, -3.0, 0.95)
	var center_x := clampf(target.position.x, COMBAT_LINE_X, AUTO_SKILL_ENTRY_X)
	var center := _safe_effect_position(EFFECT_A,
		Vector2(center_x, ground_y - 90.0), 1.81, 4)
	for pulse in range(3):
		var effect := _effect_sprite(EFFECT_A, 2, center, 0.45 + pulse * 0.12,
			Color(1.0, 0.22 + pulse * 0.10, 0.48, 0.88), 1.81)
		effect.rotation = pulse * 0.72
		var tween := create_tween().set_parallel(true)
		tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
		tween.tween_property(effect, "scale", Vector2.ONE * (1.45 + pulse * 0.18), 0.26)
		tween.tween_property(effect, "rotation", effect.rotation + 1.2, 0.28)
	for enemy in _nearby_targets(center_x, 300.0, 8):
		_apply_damage(enemy, damage, true, Color(1.0, 0.18, 0.44))
	_spawn_radial_slashes(center, Color(1.0, 0.12, 0.42, 0.88), 10, 250.0)
	_spawn_skill_impact(center, Color(1.0, 0.08, 0.38, 0.96), 1.35)
	_screen_pulse(Color(0.90, 0.04, 0.20, 0.24), 0.13)
	shake_energy = maxf(shake_energy, 3.6)
	hit_stop = 0.042


func _skill_vein_rush(damage: int) -> void:
	hero.play_animation(&"dash", true)
	_play_sfx(SFX_DASH, -4.0, 1.16)
	var origin := hero.position
	var target_x := minf(414.0, (_nearest_enemy().position.x if _nearest_enemy() else COMBAT_LINE_X) - 46.0)
	var tween := create_tween()
	tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
	tween.tween_property(hero, "position:x", target_x, 0.13)
	tween.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN_OUT)
	tween.tween_property(hero, "position:x", minf(target_x, HERO_FIRST_QUARTER_X), 0.10)
	for echo in range(5):
		var timer := get_tree().create_timer(echo * 0.028)
		timer.timeout.connect(func():
			if is_instance_valid(hero):
				_spawn_motion_echo(hero, Color(0.25, 0.80, 1.0, 0.38), 0.19))
	for enemy in _front_targets(5):
		_apply_damage(enemy, int(damage * 0.84), false, Color(0.30, 0.86, 1.0))
		_spawn_streak(origin + Vector2(20, -110), enemy.position + Vector2(0, -95),
			Color(0.20, 0.80, 1.0, 0.72), 8.0)
	_spawn_burst(Vector2(target_x, ground_y - 78), Color(0.30, 0.87, 1.0, 0.88), 22, 185.0)
	_spawn_skill_impact(Vector2(target_x, ground_y - 86),
		Color(0.22, 0.84, 1.0, 0.94), 0.92)
	shake_energy = maxf(shake_energy, 2.4)


func _skill_scarlet_rain(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_SPEAR, -6.0, 0.78)
	var center_x := clampf(target.position.x, 455.0, 590.0)
	var targets := _nearby_targets(target.position.x, 315.0, 8)
	for drop in range(7):
		var x := center_x - 138.0 + drop * 46.0 + randf_range(-16.0, 16.0)
		var start := _safe_effect_position(EFFECT_B,
			Vector2(x, 420.0 - randf_range(0, 90)), 0.48, 4)
		var finish := _safe_effect_position(EFFECT_B,
			Vector2(x - 35.0, ground_y - 65.0), 0.48, 4)
		var effect := _effect_sprite(EFFECT_B, 0, start, 0.42,
			Color(1.0, 0.42, 0.62, 0.94), 0.48)
		effect.rotation = 1.78
		var tween := create_tween().set_parallel(true)
		tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_IN)
		tween.tween_property(effect, "position", finish, 0.22 + drop * 0.018).set_delay(drop * 0.045)
		tween.tween_property(effect, "modulate:a", 0.0, 0.16).set_delay(0.20 + drop * 0.045)
	for enemy in targets:
		if is_instance_valid(enemy) and not enemy.dead:
			_apply_damage(enemy, int(damage * 0.76), false, Color(1.0, 0.30, 0.51))
	var rain_impact := _safe_effect_position(EFFECT_IMPACT,
		Vector2(center_x, ground_y - 75), 0.92, 3)
	_spawn_radial_slashes(rain_impact, Color(1.0, 0.10, 0.33, 0.74), 8, 190.0)
	_spawn_skill_impact(rain_impact, Color(1.0, 0.12, 0.36, 0.96), 1.05)


func _skill_chain(damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_SIPHON, -5.0, 1.16)
	var targets := _front_targets(6)
	var previous := hero.position + Vector2(30, -112)
	var final_visual := previous
	for index in range(targets.size()):
		var enemy: CombatActor = targets[index]
		var current := _safe_effect_position(EFFECT_B,
			enemy.position + Vector2(0, -95), 0.62, 4)
		_spawn_lightning(previous, current, _skill_color(5, 0.92))
		var effect := _effect_sprite(EFFECT_B, 1, current, 0.34,
			Color(0.38, 0.83, 1.0, 0.92), 0.62)
		effect.rotation = index * 0.8
		var effect_tween := create_tween().set_parallel(true)
		effect_tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
		effect_tween.tween_property(effect, "scale", Vector2.ONE * 0.62, 0.17)
		_apply_damage(enemy, int(damage * (1.0 - index * 0.06)), index == targets.size() - 1,
			Color(0.36, 0.82, 1.0))
		previous = current
		final_visual = current
	_spawn_skill_impact(final_visual, Color(0.28, 0.82, 1.0, 0.96), 0.82)
	shake_energy = maxf(shake_energy, 2.2)


func _skill_pillar(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_NOVA, -4.0, 1.12)
	var center := target.position + Vector2(0, -82)
	for column in range(3):
		var position_value := center + Vector2((column - 1) * 105.0, 150.0)
		var visual_finish := _safe_effect_position(EFFECT_B,
			center + Vector2((column - 1) * 105.0, -55.0), 0.78, 4)
		var effect := _effect_sprite(EFFECT_B, 2, position_value, 0.54,
			Color(1.0, 0.38, 0.20, 0.96), 0.78)
		effect.modulate.a = 0.0
		var tween := create_tween().set_parallel(true)
		tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
		tween.tween_property(effect, "position", visual_finish, 0.23).set_delay(column * 0.055)
		tween.tween_property(effect, "scale", Vector2.ONE * 0.78, 0.23).set_delay(column * 0.055)
		tween.tween_property(effect, "modulate:a", 1.0, 0.07).set_delay(column * 0.055)
	for enemy in _nearby_targets(target.position.x, 330.0, 8):
		_apply_damage(enemy, damage, true, Color(1.0, 0.46, 0.20))
	_spawn_burst(center, Color(1.0, 0.32, 0.12, 0.92), 28, 220.0)
	_spawn_skill_impact(center, Color(1.0, 0.30, 0.10, 0.98), 1.30)
	_screen_pulse(Color(1.0, 0.18, 0.06, 0.19), 0.10)
	shake_energy = maxf(shake_energy, 3.2)


func _skill_eclipse(target: CombatActor, damage: int) -> void:
	hero.play_animation(&"cast", true)
	_play_sfx(SFX_NOVA, -1.5, 0.72)
	var logical_center_x := clampf(target.position.x, 460.0, 610.0)
	var center := _safe_effect_position(EFFECT_B,
		Vector2(logical_center_x, ground_y - 175.0), 1.75, 4)
	var eclipse := _effect_sprite(EFFECT_B, 3, center, 0.72,
		Color(0.90, 0.65, 1.0, 0.96), 1.75)
	eclipse.rotation = -0.35
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
	tween.tween_property(eclipse, "scale", Vector2.ONE * 1.75, 0.38)
	tween.tween_property(eclipse, "rotation", 0.55, 0.42)
	_screen_pulse(Color(0.16, 0.025, 0.30, 0.43), 0.24)
	_spawn_radial_slashes(center, Color(0.75, 0.28, 1.0, 0.93), 14, 320.0)
	_spawn_skill_impact(center, Color(0.76, 0.24, 1.0, 0.98), 1.55)
	for pulse in range(3):
		var timer := get_tree().create_timer(0.07 + pulse * 0.09)
		timer.timeout.connect(_eclipse_hit.bind(logical_center_x, center, damage, pulse))
	shake_energy = maxf(shake_energy, 4.4)
	hit_stop = 0.052


func _eclipse_hit(center_x: float, visual_center: Vector2, damage: int, pulse: int) -> void:
	for enemy in _nearby_targets(center_x, 360.0, 9):
		_apply_damage(enemy, int(damage * (0.46 if pulse < 2 else 0.72)), pulse == 2,
			Color(0.83, 0.46, 1.0))
	_spawn_impact(visual_center + Vector2(randf_range(-90, 90), randf_range(-30, 55)),
		2, 1.0 + pulse * 0.13)
	_spawn_burst(visual_center + Vector2(randf_range(-70, 70), randf_range(-24, 48)),
		Color(0.72, 0.24, 1.0, 0.92), 12 + pulse * 5, 170.0 + pulse * 35.0)


func _front_targets(maximum: int) -> Array:
	var result: Array = []
	for enemy in enemies:
		if is_instance_valid(enemy) and not enemy.dead \
				and enemy.position.x <= SKILL_SPLASH_MAX_X:
			result.append(enemy)
	result.sort_custom(func(first, second): return first.position.x < second.position.x)
	return result.slice(0, mini(maximum, result.size()))


func _nearby_targets(center_x: float, radius: float, maximum: int) -> Array:
	var result: Array = []
	for enemy in enemies:
		if is_instance_valid(enemy) and not enemy.dead \
				and enemy.position.x <= SKILL_SPLASH_MAX_X \
				and absf(enemy.position.x - center_x) <= radius:
			result.append(enemy)
	result.sort_custom(func(first, second): return absf(first.position.x - center_x) < absf(second.position.x - center_x))
	return result.slice(0, mini(maximum, result.size()))


func _apply_damage(enemy: CombatActor, amount: int, critical: bool, color: Color) -> void:
	if enemy == null or not is_instance_valid(enemy) or enemy.dead:
		return
	var applied := enemy.apply_damage(amount)
	if applied <= 0:
		return
	enemy.flash_hit(1.0 if critical else 0.65)
	_spawn_damage_label(enemy.position + Vector2(randf_range(-14, 14), -180 if enemy.is_boss else -142),
		applied, color, critical)
	_spawn_impact(enemy.position + Vector2(randf_range(-12, 12), -105 if enemy.is_boss else -83),
		2 if critical else 0, 0.88 if critical else 0.56)
	if enemy.is_boss:
		_update_boss_bar(enemy)
	if enemy.hp <= 0:
		_defeat_enemy(enemy)


func _defeat_enemy(enemy: CombatActor) -> void:
	if not enemy_meta.has(enemy):
		return
	var meta: Dictionary = enemy_meta[enemy]
	var kind := int(meta.kind)
	var was_boss := bool(meta.boss)
	var was_elite := bool(meta.elite)
	var xp := Rules.xp_reward(kind, int(progress.region), int(progress.wave),
		int(progress.chapter_clears))
	var gold := Rules.gold_reward(kind, int(progress.region), int(progress.wave),
		int(progress.chapter_clears))
	if was_elite:
		xp = int(xp * 2.4)
		gold = int(gold * 2.2)
	hunt_chain += 1
	gold += Rules.hunt_chain_bonus_gold(hunt_chain, gold)
	progress.kills = int(progress.kills) + 1
	progress.xp = int(progress.xp) + xp
	progress.gold = int(progress.gold) + gold
	wave_defeated += 1
	_grant_levels()
	_roll_loot(was_boss, was_elite)
	hero_blood = minf(float(_hero_max_blood()), hero_blood + (32.0 if was_boss else 9.0))
	enemy.death_burst()
	_spawn_burst(enemy.position + Vector2(0, -85),
		Color(1.0, 0.08, 0.28, 0.9), 26 if was_boss else 12, 225.0 if was_boss else 130.0)
	_spawn_reward_label(enemy.position + Vector2(0, -210 if was_boss else -160), xp, gold)
	enemies.erase(enemy)
	enemy_meta.erase(enemy)
	if was_boss:
		boss_panel.visible = false
		_screen_pulse(Color(1.0, 0.20, 0.06, 0.28), 0.18)
		_show_banner("BOSS DEFEATED  ·  봉인이 무너집니다")
	var timer := get_tree().create_timer(0.36 if was_boss else 0.28)
	timer.timeout.connect(func():
		if is_instance_valid(enemy):
			enemy.queue_free())
	_fill_reinforcements()


func _grant_levels() -> void:
	var leveled := false
	while int(progress.level) < Rules.LEVEL_CAP:
		var required := Rules.xp_for_next_level(int(progress.level))
		if int(progress.xp) < required:
			break
		progress.xp = int(progress.xp) - required
		progress.level = int(progress.level) + 1
		leveled = true
	if leveled:
		var old_max := hero.max_hp
		_apply_progress_to_runtime(false)
		hero_hp = mini(_hero_max_health(), hero_hp + (_hero_max_health() - old_max) + 30)
		hero_blood = float(_hero_max_blood())
		hero.set_health(hero_hp, _hero_max_health())
		_play_sfx(SFX_LEVEL, -2.0, 1.0)
		_show_banner("LEVEL UP  ·  LV.%d" % int(progress.level))
		_spawn_level_aura()


func _roll_loot(boss: bool, elite: bool) -> void:
	var chance := 1.0 if boss else (0.42 if elite else 0.13)
	if randf() > chance:
		return
	var slot := randi_range(0, 2)
	var rarity := 3 if boss and randf() < 0.24 else 2 if boss or elite else 1 if randf() < 0.25 else 0
	var power := Rules.equipment_power(int(progress.region), int(progress.wave),
		int(progress.level), rarity, int(progress.chapter_clears))
	var code := 1 + slot * 10_000 + rarity * 1_000 + power
	var inventory: Array = progress.inventory
	var insert_index := inventory.find(0)
	if insert_index < 0:
		var weakest_index := -1
		var weakest_power := 1000
		for index in range(inventory.size()):
			var data := _decode_item(int(inventory[index]))
			if int(data.slot) == slot and int(data.power) < weakest_power:
				weakest_power = int(data.power)
				weakest_index = index
		if weakest_index >= 0 and power > weakest_power:
			insert_index = weakest_index
	if insert_index >= 0:
		inventory[insert_index] = code
		progress.inventory = inventory
		var equipment_key: String = ["weapon_power", "armor_power", "relic_power"][slot]
		if power > int(progress[equipment_key]):
			progress[equipment_key] = power
		_show_banner("전리품  ·  %s %s +%d" % [RARITY_NAMES[rarity], ITEM_SLOT_NAMES[slot], power])


func _decode_item(code: int) -> Dictionary:
	if code <= 0:
		return {"slot": -1, "rarity": 0, "power": 0}
	var value := code - 1
	return {
		"slot": int(value / 10_000),
		"rarity": int(value % 10_000 / 1_000),
		"power": value % 1_000,
	}


func _spawn_boss_arrival(actor: CombatActor) -> void:
	_screen_pulse(Color(0.75, 0.05, 0.12, 0.36), 0.22)
	_spawn_radial_slashes(actor.position + Vector2(0, -145),
		Color(1.0, 0.42, 0.10, 0.88), 12, 270.0)
	var effect := _effect_sprite(EFFECT_B, 3, actor.position + Vector2(0, -140), 1.08,
		Color(1.0, 0.64, 0.25, 0.94))
	effect.rotation = 0.4
	shake_energy = 4.2


func _boss_phase_shift(actor: CombatActor) -> void:
	actor.play_animation(&"special", true)
	actor.modulate = Color(1.08, 0.77, 0.82, 1.0)
	_show_banner("PHASE Ⅱ  ·  %s의 진명" % Stories.BOSS_NAMES[int(progress.region)])
	_screen_pulse(Color(0.82, 0.02, 0.21, 0.34), 0.21)
	_spawn_radial_slashes(actor.position + Vector2(0, -145),
		Color(1.0, 0.18, 0.40, 0.95), 16, 320.0)
	_spawn_burst(actor.position + Vector2(0, -110), Color(1.0, 0.14, 0.32, 0.94), 34, 260.0)
	shake_energy = 4.4
	for index in range(2):
		if wave_remaining > 0 and enemies.size() < MAX_ACTIVE_ENEMIES:
			_spawn_enemy()


func _effect_sprite(texture: Texture2D, row: int, position_value: Vector2,
		scale_value: float, color: Color, safe_scale_value: float = 0.0) -> AnimatedSprite2D:
	var effect := AnimatedSprite2D.new()
	effect.sprite_frames = Atlases.effect_frames(texture, row, 4)
	effect.animation = &"play"
	effect.position = _safe_effect_position(texture, position_value,
		maxf(scale_value, safe_scale_value), 4)
	effect.scale = Vector2.ONE * scale_value
	effect.modulate = color
	effect.z_index = 24
	effect.material = vfx_additive_material
	effect.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
	effect_layer.add_child(effect)
	effect.animation_finished.connect(func():
		if is_instance_valid(effect):
			effect.queue_free())
	effect.play(&"play")
	return effect


func _safe_effect_position(texture: Texture2D, desired: Vector2, max_scale: float,
		rows: int = 4) -> Vector2:
	if texture == null or max_scale <= 0.0:
		return _clamp_vfx_point(desired, VFX_VIEW_PADDING)
	var cell_size := Vector2(texture.get_width() / 4.0, texture.get_height() / float(rows))
	var half_size := cell_size * max_scale * 0.5
	half_size.x = minf(half_size.x, layout_size.x * 0.5 - VFX_VIEW_PADDING)
	half_size.y = minf(half_size.y, layout_size.y * 0.5 - VFX_VIEW_PADDING)
	return Vector2(
		clampf(desired.x, VFX_VIEW_PADDING + half_size.x,
			layout_size.x - VFX_VIEW_PADDING - half_size.x),
		clampf(desired.y, VFX_VIEW_PADDING + half_size.y,
			layout_size.y - VFX_VIEW_PADDING - half_size.y))


func _clamp_vfx_point(point: Vector2, padding: float = 8.0) -> Vector2:
	return Vector2(clampf(point.x, padding, layout_size.x - padding),
		clampf(point.y, padding, layout_size.y - padding))


func _spawn_impact(position_value: Vector2, row: int, scale_value: float) -> void:
	var effect := AnimatedSprite2D.new()
	effect.sprite_frames = Atlases.effect_frames(EFFECT_IMPACT, clampi(row, 0, 2), 3)
	effect.position = _safe_effect_position(EFFECT_IMPACT, position_value, scale_value, 3)
	effect.scale = Vector2.ONE * scale_value
	effect.rotation = randf_range(-0.32, 0.32)
	effect.modulate = Color(1.0, 0.75, 0.82, 0.97)
	effect.z_index = 30
	effect.material = vfx_additive_material
	effect_layer.add_child(effect)
	effect.animation_finished.connect(func():
		if is_instance_valid(effect):
			effect.queue_free())
	effect.play(&"play")


func _spawn_motion_echo(actor: CombatActor, color: Color, duration: float) -> void:
	if actor == null or actor.sprite == null:
		return
	var echo := Sprite2D.new()
	echo.texture = actor.sprite.sprite_frames.get_frame_texture(actor.sprite.animation,
		actor.sprite.frame)
	echo.position = actor.position + actor.sprite.position
	echo.scale = actor.sprite.scale
	echo.flip_h = actor.sprite.flip_h
	echo.modulate = color
	echo.z_index = actor.z_index - 1
	echo.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
	effect_layer.add_child(echo)
	var tween := create_tween().set_parallel(true)
	tween.tween_property(echo, "modulate:a", 0.0, duration)
	tween.tween_property(echo, "position:x", echo.position.x - 24.0, duration)
	tween.tween_property(echo, "scale", echo.scale * 1.04, duration)
	tween.chain().tween_callback(echo.queue_free)


func _spawn_streak(start: Vector2, finish: Vector2, color: Color, width: float) -> void:
	var streak := Line2D.new()
	streak.points = PackedVector2Array([_clamp_vfx_point(start), _clamp_vfx_point(finish)])
	streak.width = width
	streak.default_color = color
	streak.begin_cap_mode = Line2D.LINE_CAP_ROUND
	streak.end_cap_mode = Line2D.LINE_CAP_ROUND
	streak.z_index = 22
	effect_layer.add_child(streak)
	var tween := create_tween().set_parallel(true)
	tween.tween_property(streak, "modulate:a", 0.0, 0.16)
	tween.tween_property(streak, "width", 1.0, 0.16)
	tween.chain().tween_callback(streak.queue_free)


func _spawn_lightning(start: Vector2, finish: Vector2, color: Color) -> void:
	var line := Line2D.new()
	var points := PackedVector2Array()
	start = _clamp_vfx_point(start)
	finish = _clamp_vfx_point(finish)
	for index in range(8):
		var ratio := index / 7.0
		var point := start.lerp(finish, ratio)
		if index > 0 and index < 7:
			point += Vector2(randf_range(-8, 8), randf_range(-17, 17))
		points.append(point)
	line.points = points
	line.width = 8.0
	line.default_color = color
	line.joint_mode = Line2D.LINE_JOINT_ROUND
	line.z_index = 28
	effect_layer.add_child(line)
	var core := Line2D.new()
	core.points = points
	core.width = 2.5
	core.default_color = Color(0.92, 0.98, 1.0, 0.96)
	line.add_child(core)
	var tween := create_tween().set_parallel(true)
	tween.tween_property(line, "modulate:a", 0.0, 0.19)
	tween.tween_property(line, "width", 1.0, 0.19)
	tween.chain().tween_callback(line.queue_free)


func _spawn_radial_slashes(center: Vector2, color: Color, count: int, radius: float) -> void:
	center = _clamp_vfx_point(center, VFX_VIEW_PADDING)
	for index in range(count):
		var angle := TAU * index / float(count) + randf_range(-0.08, 0.08)
		var inner := center + Vector2.from_angle(angle) * randf_range(24.0, 58.0)
		var outer := center + Vector2.from_angle(angle) * randf_range(radius * 0.70, radius)
		_spawn_streak(inner, outer, color, randf_range(4.0, 11.0))


func _spawn_burst(position_value: Vector2, color: Color, amount_value: int,
		velocity: float) -> void:
	var particles := CPUParticles2D.new()
	particles.position = _clamp_vfx_point(position_value, 28.0)
	particles.texture = BLOOD_SHARD_PARTICLE
	particles.material = vfx_additive_material
	particles.amount = amount_value
	particles.lifetime = 0.56
	particles.one_shot = true
	particles.explosiveness = 0.94
	particles.randomness = 0.28
	particles.emission_shape = CPUParticles2D.EMISSION_SHAPE_SPHERE
	particles.emission_sphere_radius = 16.0
	particles.direction = Vector2(0, -1)
	particles.spread = 165.0
	particles.initial_velocity_min = velocity * 0.38
	particles.initial_velocity_max = velocity
	particles.gravity = Vector2(0, 310)
	particles.damping_min = 55.0
	particles.damping_max = 145.0
	particles.angular_velocity_min = -640.0
	particles.angular_velocity_max = 640.0
	particles.scale_amount_min = 0.026
	particles.scale_amount_max = 0.068
	particles.color = color
	particles.z_index = 31
	effect_layer.add_child(particles)
	particles.finished.connect(func():
		if is_instance_valid(particles):
			particles.queue_free())
	particles.emitting = true


func _spawn_skill_impact(position_value: Vector2, color: Color,
		intensity: float = 1.0) -> void:
	var visual_position := _safe_effect_position(EFFECT_IMPACT, position_value,
		0.62 + intensity * 0.28, 3)
	_spawn_impact(visual_position, 2, 0.54 + intensity * 0.20)
	_spawn_burst(visual_position, color, clampi(int(12.0 + intensity * 12.0), 14, 38),
		130.0 + intensity * 92.0)
	var bloom := _effect_sprite(EFFECT_A, 2, visual_position,
		0.24 + intensity * 0.10, Color(color.r, color.g, color.b, 0.88),
		0.58 + intensity * 0.18)
	bloom.rotation = randf_range(-0.45, 0.45)
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_EXPO).set_ease(Tween.EASE_OUT)
	tween.tween_property(bloom, "scale", Vector2.ONE * (0.58 + intensity * 0.18), 0.20)
	tween.tween_property(bloom, "modulate:a", 0.0, 0.25).set_delay(0.08)
	_spawn_radial_slashes(visual_position, Color(color.r, color.g, color.b, 0.78),
		clampi(int(5.0 + intensity * 3.0), 6, 12), 86.0 + intensity * 68.0)


func _spawn_run_dust(position_value: Vector2, fast: bool) -> void:
	var effect := _effect_sprite(EFFECT_A, 1, position_value, 0.16 if not fast else 0.23,
		Color(0.34, 0.62, 0.78, 0.24 if not fast else 0.40))
	effect.rotation = randf_range(-0.25, 0.25)


func _spawn_level_aura() -> void:
	for ring in range(3):
		var effect := _effect_sprite(EFFECT_B, 3, hero.position + Vector2(0, -95),
			0.35 + ring * 0.18, Color(1.0, 0.69, 0.24, 0.88))
		effect.rotation = ring * 0.95
	_spawn_radial_slashes(hero.position + Vector2(0, -105),
		Color(1.0, 0.72, 0.24, 0.82), 12, 220.0)


func _spawn_damage_label(position_value: Vector2, value: int, color: Color,
		critical: bool, positive: bool = false) -> void:
	var label := Label.new()
	label.text = "+%d" % value if positive else (("CRIT  " if critical else "") + "-%d" % value)
	label.position = position_value - Vector2(80, 20)
	label.size = Vector2(160, 42)
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.add_theme_font_override("font", UI_FONT)
	label.add_theme_font_size_override("font_size", 26 if critical else 20)
	label.add_theme_color_override("font_color", color)
	label.add_theme_color_override("font_shadow_color", Color(0.02, 0.01, 0.04, 0.95))
	label.add_theme_constant_override("shadow_offset_x", 2)
	label.add_theme_constant_override("shadow_offset_y", 3)
	label.z_index = 40
	label.mouse_filter = Control.MOUSE_FILTER_IGNORE
	effect_layer.add_child(label)
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	tween.tween_property(label, "position:y", label.position.y - (66 if critical else 48), 0.48)
	tween.tween_property(label, "scale", Vector2.ONE * (1.18 if critical else 1.0), 0.13)
	tween.tween_property(label, "modulate:a", 0.0, 0.22).set_delay(0.31)
	tween.chain().tween_callback(label.queue_free)


func _spawn_reward_label(_position_value: Vector2, xp: int, gold: int) -> void:
	# One accumulating loot toast replaces per-enemy labels. Rapid kills no longer pile
	# reward text over monsters and their health bars.
	if reward_toast == null or reward_toast_label == null:
		return
	reward_toast_xp += xp
	reward_toast_gold += gold
	reward_toast_label.text = "전리품   +%d XP  ·  +%d G" % [reward_toast_xp, reward_toast_gold]
	if reward_toast_tween and reward_toast_tween.is_valid():
		reward_toast_tween.kill()
	var base_y := ground_y - 250.0
	reward_toast.position.y = base_y + 12.0
	reward_toast.modulate.a = 0.0
	reward_toast.visible = true
	reward_toast_tween = create_tween()
	reward_toast_tween.set_trans(Tween.TRANS_CUBIC).set_ease(Tween.EASE_OUT)
	reward_toast_tween.tween_property(reward_toast, "position:y", base_y, 0.15)
	reward_toast_tween.parallel().tween_property(reward_toast, "modulate:a", 1.0, 0.10)
	reward_toast_tween.tween_interval(0.72)
	reward_toast_tween.tween_property(reward_toast, "modulate:a", 0.0, 0.24)
	reward_toast_tween.tween_callback(_clear_reward_toast)


func _clear_reward_toast() -> void:
	if reward_toast:
		reward_toast.visible = false
	reward_toast_xp = 0
	reward_toast_gold = 0


func _screen_pulse(color: Color, duration: float) -> void:
	screen_flash.color = color
	var tween := create_tween()
	tween.tween_property(screen_flash, "color:a", 0.0, duration)


func _update_shake(delta: float) -> void:
	if shake_energy <= 0.02:
		shake_energy = 0.0
		world.position = world.position.lerp(Vector2.ZERO, minf(1.0, delta * 20.0))
		return
	var capped := minf(shake_energy, 4.8)
	world.position = Vector2(randf_range(-capped, capped), randf_range(-capped * 0.55, capped * 0.55))
	shake_energy = move_toward(shake_energy, 0.0, delta * 24.0)


func _update_ambient(delta: float) -> void:
	if ambient_layer == null:
		return
	for mote in ambient_layer.get_children():
		mote.position.y -= float(mote.get_meta("speed")) * delta
		mote.position.x += sin(ambient_time * 0.8 + float(mote.get_meta("phase"))) * delta * 7.0
		if mote.position.y < 310.0:
			mote.position.y = ground_y + 108.0
			mote.position.x = randf_range(10.0, layout_size.x - 10.0)
	background_sprite.modulate = Color(1.0 + sin(ambient_time * 0.18) * 0.015,
		1.0, 1.0 + cos(ambient_time * 0.13) * 0.018, 1.0)


func _update_ui() -> void:
	var level := int(progress.level)
	var max_health := _hero_max_health()
	var max_blood := _hero_max_blood()
	var required_xp := Rules.xp_for_next_level(level)
	level_label.text = "LV.%d" % level
	location_label.text = "%s  ·  %d막" % [Stories.REGION_NAMES[int(progress.region)], int(progress.wave)]
	currency_label.text = "◆ %s" % _compact_number(int(progress.gold))
	hp_fill.size.x = 644.0 * clampf(hero_hp / float(max_health), 0.0, 1.0)
	hp_text.text = "생명  %d / %d" % [hero_hp, max_health]
	blood_fill.size.x = 644.0 * clampf(hero_blood / float(max_blood), 0.0, 1.0)
	blood_text.text = "혈기  %d / %d" % [int(hero_blood), max_blood]
	xp_fill.size.x = 644.0 * clampf(int(progress.xp) / float(required_xp), 0.0, 1.0)
	xp_text.text = "경험치  %d / %d" % [int(progress.xp), required_xp]
	objective_label.text = "✦ %s" % Stories.SEAL_OBJECTIVES[int(progress.region)]
	stats_label.text = "전투력 %s  ·  처치 %s  ·  연속 %d" % [
		_compact_number(_attack_power() + max_health / 4 + int(progress.armor_power) * 3),
		_compact_number(int(progress.kills)), hunt_chain]
	var cleared := wave_defeated
	wave_fill.size.x = 660.0 * clampf(cleared / float(maxi(1, wave_total)), 0.0, 1.0)
	wave_text.text = "WAVE %d  ·  %d / %d" % [int(progress.wave), cleared, wave_total]
	var auto_target := _nearest_enemy()
	for index in range(skill_buttons.size()):
		var unlocked := _skill_unlocked(index)
		skill_buttons[index].disabled = not unlocked
		skill_buttons[index].modulate = Color.WHITE if unlocked else Color(0.55, 0.59, 0.68, 0.86)
		if not unlocked:
			skill_state_labels[index].text = "LV.%d 개방" % SKILL_UNLOCK_LEVELS[index]
			skill_state_labels[index].add_theme_color_override("font_color", Color(0.68, 0.72, 0.82))
			skill_cooldown_overlays[index].position.y = 4.0
			skill_cooldown_overlays[index].size.y = 58.0
			skill_cooldown_labels[index].text = "잠금"
		elif skill_timers[index] > 0.0:
			var ratio := clampf(skill_timers[index] / SKILL_COOLDOWNS[index], 0.0, 1.0)
			skill_cooldown_overlays[index].position.y = 4.0 + 58.0 * (1.0 - ratio)
			skill_cooldown_overlays[index].size.y = 58.0 * ratio
			skill_cooldown_labels[index].text = "%.1f" % skill_timers[index]
			skill_state_labels[index].text = "AUTO · 발동" if ratio > 0.93 else "재사용 %.1f초" % skill_timers[index]
			skill_state_labels[index].add_theme_color_override("font_color",
				Color(1.0, 0.81, 0.37) if ratio > 0.93 else Color(0.63, 0.75, 0.90))
		else:
			skill_cooldown_overlays[index].size.y = 0
			skill_cooldown_labels[index].text = ""
			var auto_state := _auto_skill_state(index, auto_target)
			match auto_state:
				&"blood":
					skill_state_labels[index].text = "혈기 부족"
					skill_state_labels[index].add_theme_color_override("font_color", Color(1.0, 0.42, 0.52))
				&"approach":
					skill_state_labels[index].text = "AUTO · 추격 중"
					skill_state_labels[index].add_theme_color_override("font_color", Color(0.62, 0.78, 0.94))
				&"target":
					skill_state_labels[index].text = "다음 적 대기"
					skill_state_labels[index].add_theme_color_override("font_color", Color(0.58, 0.68, 0.82))
				&"chain":
					skill_state_labels[index].text = "AUTO · 연계"
					skill_state_labels[index].add_theme_color_override("font_color", Color(1.0, 0.76, 0.32))
				&"down":
					skill_state_labels[index].text = "부활 대기"
					skill_state_labels[index].add_theme_color_override("font_color", Color(0.70, 0.62, 0.78))
				&"paused":
					skill_state_labels[index].text = "일시 정지"
					skill_state_labels[index].add_theme_color_override("font_color", Color(0.66, 0.70, 0.78))
				_:
					skill_state_labels[index].text = "AUTO · 준비"
					skill_state_labels[index].add_theme_color_override("font_color", Color(0.37, 0.90, 1.0))
	_update_boss_ui()
	_update_growth_ui()
	_update_inventory_ui()


func _update_boss_ui() -> void:
	var boss := _current_boss()
	if boss == null:
		boss_panel.visible = false
		return
	boss_panel.visible = true
	_update_boss_bar(boss)


func _update_boss_bar(boss: CombatActor) -> void:
	if boss == null or not is_instance_valid(boss):
		return
	var ratio := clampf(boss.hp / float(boss.max_hp), 0.0, 1.0)
	boss_health_fill.size.x = 562.0 * ratio
	boss_health_text.text = "%d / %d" % [boss.hp, boss.max_hp]


func _current_boss() -> CombatActor:
	for enemy in enemies:
		if is_instance_valid(enemy) and not enemy.dead and enemy.is_boss:
			return enemy
	return null


func _compact_number(value: int) -> String:
	if value >= 1_000_000:
		return "%.1fM" % (value / 1_000_000.0)
	if value >= 1_000:
		return "%.1fK" % (value / 1_000.0)
	return str(value)


func _show_banner(text_value: String) -> void:
	banner_label.text = text_value
	banner_label.modulate.a = 0.0
	banner_label.scale = Vector2(0.92, 0.92)
	banner_label.pivot_offset = banner_label.size * 0.5
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	tween.tween_property(banner_label, "modulate:a", 1.0, 0.15)
	tween.tween_property(banner_label, "scale", Vector2.ONE, 0.20)
	tween.chain().tween_interval(1.35)
	tween.chain().tween_property(banner_label, "modulate:a", 0.0, 0.28)


func _show_story(beat: Dictionary) -> void:
	story_speaker.text = str(beat.get("speaker", ""))
	story_line.text = str(beat.get("line", ""))
	story_panel.visible = true
	story_panel.modulate.a = 0.0
	story_panel.position.x = 45.0
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_CUBIC).set_ease(Tween.EASE_OUT)
	tween.tween_property(story_panel, "position:x", 28.0, 0.20)
	tween.tween_property(story_panel, "modulate:a", 1.0, 0.18)
	tween.chain().tween_interval(4.6)
	tween.chain().tween_property(story_panel, "modulate:a", 0.0, 0.35)
	tween.chain().tween_callback(func(): story_panel.visible = false)


func _open_overlay(overlay: Control) -> void:
	_play_sfx(SFX_TAP, -14.0, 1.0)
	_close_all_overlays()
	overlay.visible = true
	if overlay == inventory_overlay:
		_update_inventory_ui()
	overlay.modulate.a = 0.0
	var panel: Panel = overlay.get_meta("panel")
	panel.scale = Vector2(0.96, 0.96)
	panel.pivot_offset = panel.size * 0.5
	var tween := create_tween().set_parallel(true)
	tween.tween_property(overlay, "modulate:a", 1.0, 0.15)
	tween.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	tween.tween_property(panel, "scale", Vector2.ONE, 0.22)


func _close_overlay(overlay: Control) -> void:
	_play_sfx(SFX_TAP, -15.0, 0.98)
	overlay.visible = false


func _close_all_overlays() -> void:
	for overlay in [growth_overlay, inventory_overlay, offline_overlay]:
		if overlay:
			overlay.visible = false


func _close_top_overlay() -> bool:
	for overlay in [offline_overlay, growth_overlay, inventory_overlay]:
		if overlay and overlay.visible:
			_close_overlay(overlay)
			return true
	if pause_overlay and pause_overlay.visible:
		_toggle_pause()
		return true
	return false


func _toggle_pause() -> void:
	_play_sfx(SFX_TAP, -14.0, 1.0)
	game_paused = not game_paused
	pause_overlay.visible = game_paused
	if game_paused:
		pause_overlay.modulate.a = 0.0
		var tween := create_tween()
		tween.tween_property(pause_overlay, "modulate:a", 1.0, 0.15)
	else:
		pause_overlay.visible = false
	_save_progress()


func _upgrade_stat(index: int) -> void:
	_play_sfx(SFX_TAP, -13.0, 1.05)
	var key: String = STAT_KEYS[index]
	var current := int(progress[key])
	if current >= Rules.UPGRADE_CAP:
		return
	var cost := Rules.stat_upgrade_cost(current)
	if int(progress.gold) < cost:
		_show_banner("골드가 부족합니다")
		return
	var old_max := _hero_max_health()
	progress.gold = int(progress.gold) - cost
	progress[key] = current + 1
	if key == "vitality_level":
		hero_hp += _hero_max_health() - old_max
	if key == "blood_level":
		hero_blood = float(_hero_max_blood())
	hero.set_health(hero_hp, _hero_max_health())
	_save_progress()
	_update_ui()


func _update_growth_ui() -> void:
	if growth_stat_labels.is_empty():
		return
	var descriptions := [
		"최대 생명 +24", "기본 공격력 +5", "최대 혈기 +15", "초당 회복 +0.22",
	]
	for index in range(STAT_KEYS.size()):
		var current := int(progress[STAT_KEYS[index]])
		growth_stat_labels[index].text = "단계 %d / %d\n%s" % [current, Rules.UPGRADE_CAP,
			descriptions[index]]
		growth_buttons[index].text = "MAX" if current >= Rules.UPGRADE_CAP else "◆ %d\n강화" % Rules.stat_upgrade_cost(current)
		growth_buttons[index].disabled = current >= Rules.UPGRADE_CAP


func _update_inventory_ui() -> void:
	if inventory_equipment_label == null or inventory_overlay == null \
			or not inventory_overlay.visible:
		return
	var equipped_powers := [int(progress.weapon_power), int(progress.armor_power),
		int(progress.relic_power)]
	for slot in range(mini(inventory_equipped_labels.size(), equipped_powers.size())):
		inventory_equipped_labels[slot].text = "%s\n+%d" % [ITEM_SLOT_NAMES[slot], equipped_powers[slot]]
		inventory_equipped_icons[slot].modulate = Color.WHITE
	inventory_equipment_label.text = "총 전투력  %s" % _compact_number(
		_attack_power() + _hero_max_health() / 4 + int(progress.armor_power) * 3)
	var inventory: Array = progress.inventory
	for index in range(inventory_slot_labels.size()):
		var code := int(inventory[index]) if index < inventory.size() else 0
		if code == 0:
			var preview_slot := index % 3
			inventory_slot_icons[index].texture = Atlases.frame(
				EQUIPMENT_ICONS, 3, 1, preview_slot, 0)
			inventory_slot_icons[index].modulate = Color(0.48, 0.58, 0.72, 0.20)
			inventory_slot_labels[index].text = "빈 슬롯\n전투에서 획득"
			inventory_slot_labels[index].add_theme_color_override("font_color",
				Color(0.52, 0.63, 0.78))
			inventory_slot_panels[index].add_theme_stylebox_override("panel", _panel_style(
				Color(0.025, 0.036, 0.082, 0.95), Color(0.25, 0.48, 0.64, 0.52), 13, 1))
		else:
			var item := _decode_item(code)
			var item_slot: int = clampi(int(item.slot), 0, 2)
			var rarity: int = clampi(int(item.rarity), 0, RARITY_COLORS.size() - 1)
			inventory_slot_icons[index].texture = Atlases.frame(
				EQUIPMENT_ICONS, 3, 1, item_slot, 0)
			inventory_slot_icons[index].modulate = Color.WHITE
			inventory_slot_labels[index].text = "%s %s\n전투력 +%d" % [
				RARITY_NAMES[rarity], ITEM_SLOT_NAMES[item_slot], int(item.power)]
			inventory_slot_labels[index].add_theme_color_override("font_color",
				RARITY_COLORS[rarity])
			inventory_slot_panels[index].add_theme_stylebox_override("panel", _panel_style(
				Color(0.025, 0.036, 0.082, 0.97), RARITY_COLORS[rarity], 13, 2))


func _apply_offline_reward() -> void:
	# Desktop/headless previews use a fallback file and should not cover visual QA with a
	# synthetic return report. The Android bridge is the production offline clock source.
	if save_layer.bridge == null:
		return
	var last_active := save_layer.last_active_epoch_seconds()
	var elapsed := Rules.offline_elapsed_seconds(last_active, save_layer.now_epoch_seconds())
	if elapsed < 90:
		return
	var gold := Rules.offline_gold_reward(elapsed, int(progress.level), int(progress.region), int(progress.wave))
	var xp := Rules.offline_xp_reward(elapsed, int(progress.level), int(progress.region), int(progress.wave))
	progress.gold = int(progress.gold) + gold
	progress.xp = int(progress.xp) + xp
	_grant_levels()
	var hours := elapsed / 3600.0
	var message: Label = offline_overlay.get_meta("panel").get_meta("message")
	message.text = "당신이 자리를 비운 %.1f시간 동안\n리안은 밤의 길을 계속 걸었습니다.\n\n+%s XP     +%s G" % [
		hours, _compact_number(xp), _compact_number(gold)]
	_open_overlay(offline_overlay)
	_save_progress()


func _save_progress() -> void:
	if save_layer == null or progress.is_empty():
		return
	progress = Rules.normalize_progress(progress)
	save_layer.store_progress(progress)
