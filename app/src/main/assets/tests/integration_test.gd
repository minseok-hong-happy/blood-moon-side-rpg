extends SceneTree

const FRAME_BUDGET := 900


func _initialize() -> void:
	_run_simulation.call_deferred()


func _run_simulation() -> void:
	var packed: PackedScene = load("res://main.tscn")
	if packed == null:
		push_error("Could not load the main VAYLORN scene")
		quit(1)
		return
	var game := packed.instantiate()
	root.add_child(game)
	await process_frame
	if game.skill_buttons.size() != 8:
		push_error("The battle HUD must expose exactly eight skill buttons")
		quit(1)
		return
	if game.skill_cooldown_overlays.size() != 8 or game.skill_cooldown_labels.size() != 8:
		push_error("Every skill card must own an isolated cooldown layer and countdown label")
		quit(1)
		return
	if game.ui_font_medium == null or game.ui_font_bold == null \
			or game.ui_root.theme.default_font_size < 19 \
			or game.skill_state_labels[0].get_theme_font_size("font_size") < 16:
		push_error("Portrait UI typography regressed below the mobile readability floor")
		quit(1)
		return
	if game.inventory_equipped_icons.size() != 3 or game.inventory_equipped_labels.size() != 3 \
			or game.inventory_slot_icons.size() != 6 or game.inventory_slot_panels.size() != 6:
		push_error("Equipment and inventory icon coverage must remain complete")
		quit(1)
		return
	for equipment_icon in game.inventory_equipped_icons:
		if equipment_icon.texture == null:
			push_error("Every equipped slot must have a generated equipment icon")
			quit(1)
			return
	game.inventory_overlay.visible = true
	game._update_inventory_ui()
	if game.inventory_equipment_label.text.find("총 전투력") < 0 \
			or game.inventory_slot_labels[0].text.is_empty():
		push_error("Inventory icon view did not render its readable equipment summary")
		quit(1)
		return
	game.inventory_overlay.visible = false
	if game.bgm_player == null or game.bgm_player.stream == null \
			or game.bgm_player.volume_db < -10.0 or not game.bgm_player.playing:
		push_error("The original battle BGM must start audibly with the main scene")
		quit(1)
		return
	if game.MAX_ACTIVE_ENEMIES != 48 or game.INITIAL_NORMAL_SPAWN_COUNT != 32 \
			or game.INITIAL_BOSS_SPAWN_COUNT != 24:
		push_error("Monster density constants regressed below the 2x rush target")
		quit(1)
		return
	if game.MONSTER_RUN_SPEED_BASE < 260.0 \
			or game._enemy_approach_speed({"boss": false, "kind": 0}) < 260.0 \
			or game._enemy_approach_speed({"boss": false, "kind": 2}) <= \
				game._enemy_approach_speed({"boss": false, "kind": 0}):
		push_error("Monsters must rush toward the combat line at the tuned fast speed")
		quit(1)
		return
	if absf(game.HERO_FIRST_QUARTER_X - game.VIEW_SIZE.x / 4.0) > 0.1 \
			or absf(game.HERO_START_X - game.HERO_FIRST_QUARTER_X) > 0.1:
		push_error("The hero must remain in the first quarter of the portrait battle view")
		quit(1)
		return
	if game.hero.z_index < game.effect_layer.z_index + 31:
		push_error("Hero silhouette must render above every skill VFX child layer")
		quit(1)
		return
	if game.hero.sprite.sprite_frames.get_frame_count(&"cast") != 3:
		push_error("Skill casting must use the complete three-pose hero animation")
		quit(1)
		return
	game.progress.region = 0
	game.progress.wave = 1
	game._start_wave()
	if game.enemies.size() < game.INITIAL_NORMAL_SPAWN_COUNT:
		push_error("A normal wave must enter with a visible initial monster group")
		quit(1)
		return
	var first_spawn: Node2D = game._nearest_enemy()
	if first_spawn == null or first_spawn.position.x > 650.0:
		push_error("New monsters must enter near the visible combat edge")
		quit(1)
		return
	for refill_frame in range(8):
		game._update_wave_flow(0.016)
	if game.enemies.size() < game.MAX_ACTIVE_ENEMIES:
		push_error("Empty combat slots must be refilled without waiting for six active enemies")
		quit(1)
		return
	game.bgm_player.stop()
	game._update_bgm_watchdog(1.01)
	if not game.bgm_player.playing:
		push_error("The BGM watchdog must recover interrupted playback")
		quit(1)
		return
	var safe_scale := 1.75
	var safe_position: Vector2 = game._safe_effect_position(
		game.EFFECT_B, Vector2(game.layout_size.x + 80.0, game.ground_y), safe_scale, 4)
	var vfx_half_width: float = game.EFFECT_B.get_width() / 4.0 * safe_scale * 0.5
	if safe_position.x + vfx_half_width > game.layout_size.x - game.VFX_VIEW_PADDING + 0.1:
		push_error("Large skill art can escape the portrait viewport safe area")
		quit(1)
		return
	var particle_count_before: int = game.effect_layer.get_child_count()
	game._spawn_burst(Vector2(520.0, game.ground_y - 90.0),
		Color(1.0, 0.12, 0.35, 1.0), 16, 180.0)
	var particle_node = game.effect_layer.get_child(game.effect_layer.get_child_count() - 1)
	if game.effect_layer.get_child_count() <= particle_count_before \
			or not particle_node is CPUParticles2D or particle_node.texture == null \
			or particle_node.gravity.y <= 0.0 or particle_node.angular_velocity_max <= 0.0:
		push_error("Skill bursts must use textured, gravity-driven physical particles")
		quit(1)
		return
	if game.SKILL_COOLDOWNS.max() > 4.8 or game.SKILL_COOLDOWNS[0] > 1.1 \
			or game.SKILL_BLOOD_COSTS.max() > 13:
		push_error("Automatic skill cadence or blood economy regressed to the slow profile")
		quit(1)
		return
	if absf(game.bottom_panel.position.y + game.bottom_panel.size.y - game.layout_size.y) > 0.1:
		push_error("The battle HUD must meet the physical bottom edge on every portrait aspect")
		quit(1)
		return
	var far_enemy = game._nearest_enemy()
	if far_enemy == null:
		push_error("Cooldown regression setup requires an active enemy")
		quit(1)
		return
	for enemy in game.enemies:
		if enemy != far_enemy:
			enemy.position.x = 900.0
	far_enemy.set_health(100000, 100000)
	far_enemy.position.x = 700.0
	game.skill_timers[0] = 2.0
	game.ui_timer = 0.0
	game._process(0.5)
	if absf(game.skill_timers[0] - 1.5) > 0.02 \
			or game.skill_cooldown_labels[0].text.find("1.5") < 0:
		push_error("Skill cooldowns must visibly tick while enemies are outside cast range")
		quit(1)
		return
	var saved_level: int = int(game.progress.level)
	var saved_blood: float = game.hero_blood
	game.progress.level = 9
	game.hero_blood = 999.0
	far_enemy.set_health(100000, 100000)
	for skill_index in range(game.skill_timers.size()):
		game.skill_timers[skill_index] = 99.0
	game.skill_timers[0] = 0.0
	game.auto_skill_cursor = 0
	game.auto_skill_cast_timer = 0.0
	far_enemy.position.x = game.AUTO_SKILL_ENTRY_X + 24.0
	game._update_ui()
	if game.skill_state_labels[0].text == "AUTO · 준비" \
			or game._auto_skill_state(0, far_enemy) != &"approach":
		push_error("A skill outside auto range must say that it is chasing, never READY")
		quit(1)
		return
	var casts_before_ready: int = game.automatic_skill_casts
	game._update_auto_skills()
	if game.automatic_skill_casts != casts_before_ready or game.skill_timers[0] > 0.0:
		push_error("An off-screen target must not consume a queued automatic skill")
		quit(1)
		return
	far_enemy.position.x = game.AUTO_SKILL_ENTRY_X - 1.0
	game._update_ui()
	if game.skill_state_labels[0].text != "AUTO · 준비" \
			or game._auto_skill_state(0, far_enemy) != &"ready":
		push_error("A castable automatic skill must expose the READY state")
		quit(1)
		return
	game._update_auto_skills()
	game._update_ui()
	if game.automatic_skill_casts != casts_before_ready + 1 \
			or absf(game.skill_timers[0] - game.SKILL_COOLDOWNS[0]) > 0.02 \
			or game.skill_state_labels[0].text == "AUTO · 준비":
		push_error("READY must transition to an automatic cast in the same combat decision")
		quit(1)
		return
	for skill_index in range(game.skill_timers.size()):
		game.skill_timers[skill_index] = 99.0
	game.skill_timers[2] = 0.0
	game.auto_skill_cursor = 2
	game.auto_skill_cast_timer = 0.0
	far_enemy.position.x = game.AUTO_SKILL_ENTRY_X - 1.0
	far_enemy.set_health(100000, 100000)
	var health_before_ranged_cast: int = far_enemy.hp
	game._update_auto_skills()
	if far_enemy.hp >= health_before_ranged_cast \
			or absf(game.skill_timers[2] - game.SKILL_COOLDOWNS[2]) > 0.02:
		push_error("A ranged automatic area skill must damage its visible target")
		quit(1)
		return
	for skill_index in range(game.skill_timers.size()):
		game.skill_timers[skill_index] = 0.0
	game.auto_skill_cursor = 3
	game.auto_skill_cast_timer = 0.0
	far_enemy.position.x = 470.0
	far_enemy.set_health(100000, 100000)
	game._update_auto_skills()
	if absf(game.skill_timers[3] - game.SKILL_COOLDOWNS[3]) > 0.02 \
			or game.auto_skill_cursor != 4 or game.auto_skill_cast_timer <= 0.0:
		push_error("Automatic skills must cast a ready art and rotate slot priority")
		quit(1)
		return
	game.progress.level = saved_level
	game.hero_blood = saved_blood
	var tall_height := 1560.0
	var tall_panel_y: float = game._bottom_panel_y_for_height(tall_height)
	var tall_ground_y: float = game._ground_y_for_height(tall_height)
	if absf(tall_panel_y + game.BOTTOM_PANEL_HEIGHT - tall_height) > 0.1 \
			or tall_ground_y >= tall_panel_y or tall_ground_y <= game.BASE_GROUND_Y:
		push_error("Tall-screen layout contract regressed")
		quit(1)
		return
	for first_index in range(game.skill_buttons.size()):
		var first_button: Button = game.skill_buttons[first_index]
		var first_rect := Rect2(first_button.position, first_button.size)
		if not Rect2(Vector2.ZERO, game.bottom_panel.size).encloses(first_rect):
			push_error("Skill card %d escaped the bottom HUD" % first_index)
			quit(1)
			return
		var cooldown: ColorRect = game.skill_cooldown_overlays[first_index]
		var countdown: Label = game.skill_cooldown_labels[first_index]
		if cooldown.get_parent() == first_button or cooldown.size.x > 58.1 \
				or countdown.get_index() <= cooldown.get_index():
			push_error("Skill card %d cooldown can obscure its text or neighboring content" % first_index)
			quit(1)
			return
		for second_index in range(first_index + 1, game.skill_buttons.size()):
			var second_button: Button = game.skill_buttons[second_index]
			var second_rect := Rect2(second_button.position, second_button.size)
			if first_rect.intersects(second_rect):
				push_error("Skill button layout regression: buttons %d and %d overlap" % [
					first_index, second_index])
				quit(1)
				return
	# Earlier combat assertions may legitimately defeat widened-range targets. Isolate this
	# HUD accumulation contract from those rewards.
	game.reward_toast_xp = 0
	game.reward_toast_gold = 0
	game.reward_toast_label.text = ""
	game._spawn_reward_label(Vector2.ZERO, 7, 3)
	game._spawn_reward_label(Vector2.ZERO, 11, 5)
	if game.reward_toast_xp != 18 or game.reward_toast_gold != 8 \
			or game.reward_toast_label.text.find("+18 XP") < 0:
		push_error("Rapid kill rewards must accumulate in one readable HUD toast")
		quit(1)
		return
	for frame in range(FRAME_BUDGET):
		await process_frame
	if not is_instance_valid(game):
		push_error("The main scene exited during the combat simulation")
		quit(1)
		return
	if game.automatic_skill_casts < 24:
		push_error("Automatic combat must cast skills repeatedly; observed only %d casts" %
			game.automatic_skill_casts)
		quit(1)
		return
	var observed_auto_casts: int = game.automatic_skill_casts
	game.game_paused = true
	# Stop producing combat callbacks, then let all scheduled VFX/story timers drain before
	# freeing the scene. This keeps teardown errors distinct from actual gameplay failures.
	for frame in range(360):
		await process_frame
	game.queue_free()
	for frame in range(60):
		await process_frame
	# Fixed-FPS tests run faster than wall time; give the native audio mixer a brief real-time
	# window to release playback handles after the scene has stopped them.
	OS.delay_msec(300)
	game = null
	packed = null
	print("GODOT INTEGRATION PASSED: 900-frame combat, %d automatic skill casts" %
		observed_auto_casts)
	_finish.call_deferred()


func _finish() -> void:
	quit(0)
