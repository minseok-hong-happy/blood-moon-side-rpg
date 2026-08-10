extends SceneTree


func _initialize() -> void:
	var result := ProjectSettings.save_custom("res://project.binary")
	if result != OK:
		push_error("PROJECT_BINARY_EXPORT_FAILED: %s" % error_string(result))
		quit(int(result))
		return

	var binary := FileAccess.open("res://project.binary", FileAccess.READ)
	if binary == null:
		push_error("PROJECT_BINARY_EXPORT_FAILED: project.binary could not be reopened")
		quit(1)
		return

	var header := binary.get_buffer(4).get_string_from_ascii()
	if header != "ECFG":
		push_error("PROJECT_BINARY_EXPORT_FAILED: invalid header %s" % header)
		quit(1)
		return

	print("PROJECT_BINARY_EXPORT_PASS")
	quit(0)
