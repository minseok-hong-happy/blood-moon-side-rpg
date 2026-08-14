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
