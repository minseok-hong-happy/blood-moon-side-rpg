class_name CombatActor
extends Node2D

signal animation_event_finished(actor: CombatActor, animation_name: StringName)

var sprite: AnimatedSprite2D
var shadow: Polygon2D
var name_label: Label
var hp_back: ColorRect
var hp_fill: ColorRect
var hp: int = 1
var max_hp: int = 1
var kind: int = 0
var is_hero := false
var is_boss := false
var is_elite := false
var dead := false
var display_scale := 0.5
var ground_bob := 0.0
var move_velocity := Vector2.ZERO
var base_modulate := Color.WHITE
var flash_tween: Tween
var action_lock := 0.0
var breath_time := 0.0


func configure(frames: SpriteFrames, actor_scale: float, actor_kind: int,
		actor_name: String, hero: bool = false, boss: bool = false,
		elite: bool = false) -> void:
	kind = actor_kind
	is_hero = hero
	is_boss = boss
	is_elite = elite
	display_scale = actor_scale

	shadow = Polygon2D.new()
	var shadow_width := 80.0 if hero else 92.0
	if boss:
		shadow_width = 152.0
	shadow.polygon = PackedVector2Array([
		Vector2(-shadow_width, -7.0), Vector2(-shadow_width * 0.58, -13.0),
		Vector2(shadow_width * 0.58, -13.0), Vector2(shadow_width, -7.0),
		Vector2(shadow_width * 0.58, -1.0), Vector2(-shadow_width * 0.58, -1.0),
	])
	shadow.color = Color(0.01, 0.015, 0.035, 0.54)
	shadow.z_index = -2
	add_child(shadow)

	sprite = AnimatedSprite2D.new()
	sprite.sprite_frames = frames
	sprite.scale = Vector2.ONE * display_scale
	var cell_height := _first_frame_height(frames)
	sprite.position.y = -cell_height * display_scale * 0.47
	sprite.flip_h = hero
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR_WITH_MIPMAPS
	sprite.animation_finished.connect(_on_animation_finished)
	add_child(sprite)

	if not hero:
		var bar_width := 172.0 if boss else (112.0 if elite else 92.0)
		var bar_height := 10.0 if boss else (8.0 if elite else 7.0)
		hp_back = ColorRect.new()
		hp_back.position = Vector2(-bar_width * 0.5,
			-cell_height * display_scale - (34.0 if boss else 17.0))
		hp_back.size = Vector2(bar_width, bar_height)
		hp_back.color = Color(0.025, 0.035, 0.07, 0.92)
		hp_back.mouse_filter = Control.MOUSE_FILTER_IGNORE
		add_child(hp_back)

		hp_fill = ColorRect.new()
		hp_fill.position = hp_back.position + Vector2(1.5, 1.5)
		hp_fill.size = hp_back.size - Vector2(3.0, 3.0)
		hp_fill.color = Color(0.91, 0.12, 0.31) if not elite else Color(0.96, 0.68, 0.13)
		hp_fill.mouse_filter = Control.MOUSE_FILTER_IGNORE
		add_child(hp_fill)

		if boss or elite:
			name_label = Label.new()
			name_label.text = actor_name
			name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
			name_label.position = Vector2(-130.0, hp_back.position.y - 31.0)
			name_label.size = Vector2(260.0, 28.0)
			name_label.add_theme_font_size_override("font_size", 18 if boss else 14)
			name_label.add_theme_color_override("font_color",
				Color(1.0, 0.82, 0.45) if boss else Color(0.78, 0.91, 1.0))
			name_label.add_theme_color_override("font_shadow_color", Color(0, 0, 0, 0.9))
			name_label.add_theme_constant_override("shadow_offset_x", 2)
			name_label.add_theme_constant_override("shadow_offset_y", 2)
			name_label.mouse_filter = Control.MOUSE_FILTER_IGNORE
			add_child(name_label)

	play_animation(&"idle")


func set_health(value: int, maximum: int) -> void:
	max_hp = maxi(1, maximum)
	hp = clampi(value, 0, max_hp)
	_update_health_bar()


func apply_damage(amount: int) -> int:
	if dead:
		return 0
	var applied := mini(hp, maxi(0, amount))
	hp -= applied
	_update_health_bar()
	if hp <= 0:
		dead = true
	return applied


func heal(amount: int) -> int:
	if dead:
		return 0
	var before := hp
	hp = mini(max_hp, hp + maxi(0, amount))
	_update_health_bar()
	return hp - before


func set_health_bar_visible(value: bool) -> void:
	if hp_back:
		hp_back.visible = value
	if hp_fill:
		hp_fill.visible = value
	if name_label:
		name_label.visible = value


func play_animation(animation_name: StringName, restart: bool = false) -> void:
	if sprite == null or sprite.sprite_frames == null:
		return
	if not sprite.sprite_frames.has_animation(animation_name):
		return
	if restart or sprite.animation != animation_name or not sprite.is_playing():
		sprite.play(animation_name)
		if animation_name not in [&"idle", &"run"]:
			var frame_count := sprite.sprite_frames.get_frame_count(animation_name)
			var frame_speed := sprite.sprite_frames.get_animation_speed(animation_name)
			action_lock = maxf(action_lock, frame_count / maxf(1.0, frame_speed))


func set_running(running: bool) -> void:
	if dead:
		return
	if action_lock > 0.0:
		return
	if running:
		play_animation(&"run")
	else:
		play_animation(&"idle")


func set_facing_right(right: bool) -> void:
	# The source art looks left. Flipping it means the actor looks right.
	sprite.flip_h = right


func animate_stride(delta: float, speed_ratio: float) -> void:
	if dead or sprite == null:
		return
	action_lock = maxf(0.0, action_lock - delta)
	if sprite.animation == &"run":
		ground_bob += delta * lerpf(10.0, 17.0, clampf(speed_ratio, 0.0, 1.0))
		var lift := absf(sin(ground_bob)) * 4.2 * clampf(speed_ratio, 0.25, 1.0)
		sprite.position.y -= lift - move_velocity.y
		move_velocity.y = lift
		shadow.scale.x = 1.0 - lift * 0.018
		shadow.modulate.a = 1.0 - lift * 0.045
	elif sprite.animation == &"idle":
		if absf(move_velocity.y) > 0.01:
			sprite.position.y += move_velocity.y
		move_velocity.y = 0.0
		breath_time += delta
		var breath := sin(breath_time * 3.4)
		sprite.scale = Vector2(display_scale * (1.0 - breath * 0.006),
			display_scale * (1.0 + breath * 0.012))
		shadow.scale.x = lerpf(shadow.scale.x, 1.0, minf(1.0, delta * 16.0))
		shadow.modulate.a = lerpf(shadow.modulate.a, 1.0, minf(1.0, delta * 16.0))


func attack_lunge(direction: float, distance: float = 24.0) -> void:
	if dead:
		return
	var origin := position
	var tween := create_tween()
	tween.set_trans(Tween.TRANS_QUAD)
	tween.set_ease(Tween.EASE_OUT)
	tween.tween_property(self, "position:x", origin.x + direction * distance, 0.055)
	tween.set_ease(Tween.EASE_IN_OUT)
	tween.tween_property(self, "position:x", origin.x, 0.12)
	var base_scale := Vector2.ONE * display_scale
	var visual := create_tween()
	visual.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	visual.tween_property(sprite, "scale",
		Vector2(base_scale.x * 1.10, base_scale.y * 0.92), 0.055)
	visual.set_ease(Tween.EASE_IN_OUT)
	visual.tween_property(sprite, "scale", base_scale, 0.12)
	var rotation_tween := create_tween()
	rotation_tween.tween_property(sprite, "rotation", direction * 0.075, 0.055)
	rotation_tween.tween_property(sprite, "rotation", 0.0, 0.12)


func flash_hit(strength: float = 1.0) -> void:
	if sprite == null:
		return
	if flash_tween and flash_tween.is_valid():
		flash_tween.kill()
	sprite.modulate = Color(1.0, 0.55 + 0.25 * strength, 0.66 + 0.2 * strength, 1.0)
	flash_tween = create_tween()
	flash_tween.tween_property(sprite, "modulate", base_modulate, 0.085)


func death_burst() -> void:
	dead = true
	if sprite.sprite_frames.has_animation(&"death"):
		sprite.play(&"death")
	else:
		sprite.stop()
	var tween := create_tween().set_parallel(true)
	tween.set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
	tween.tween_property(self, "position:y", position.y + 22.0, 0.24)
	tween.tween_property(self, "modulate:a", 0.0, 0.26).set_delay(0.08)
	tween.tween_property(self, "scale", Vector2(1.08, 0.88), 0.20)


func _update_health_bar() -> void:
	if hp_fill == null or hp_back == null:
		return
	var ratio := hp / float(max_hp)
	hp_fill.size.x = (hp_back.size.x - 3.0) * ratio
	if ratio < 0.25:
		hp_fill.color = Color(1.0, 0.24, 0.21)


func _first_frame_height(frames: SpriteFrames) -> float:
	for animation_name in frames.get_animation_names():
		if frames.get_frame_count(animation_name) > 0:
			var texture := frames.get_frame_texture(animation_name, 0)
			if texture:
				return texture.get_height()
	return 320.0


func _on_animation_finished() -> void:
	animation_event_finished.emit(self, sprite.animation)
	if not dead and sprite.animation not in [&"idle", &"run"]:
		action_lock = 0.0
		play_animation(&"idle", true)
