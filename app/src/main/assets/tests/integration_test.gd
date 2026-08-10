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
	for first_index in range(game.skill_buttons.size()):
		var first_button: Button = game.skill_buttons[first_index]
		var first_rect := Rect2(first_button.position, first_button.size)
		for second_index in range(first_index + 1, game.skill_buttons.size()):
			var second_button: Button = game.skill_buttons[second_index]
			var second_rect := Rect2(second_button.position, second_button.size)
			if first_rect.intersects(second_rect):
				push_error("Skill button layout regression: buttons %d and %d overlap" % [
					first_index, second_index])
				quit(1)
				return
	for frame in range(FRAME_BUDGET):
		await process_frame
	if not is_instance_valid(game):
		push_error("The main scene exited during the combat simulation")
		quit(1)
		return
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
	print("GODOT INTEGRATION PASSED: 900-frame automatic combat simulation")
	_finish.call_deferred()


func _finish() -> void:
	quit(0)
