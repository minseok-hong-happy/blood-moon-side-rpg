class_name SaveAdapter
extends RefCounted

const SAVE_PATH := "user://vaylorn_progress.json"

var bridge: Object
var fallback_last_active := 0


func _init() -> void:
	if Engine.has_singleton("VaylornProgress"):
		bridge = Engine.get_singleton("VaylornProgress")


func load_progress() -> Dictionary:
	var payload := ""
	if bridge:
		payload = str(bridge.loadProgressJson())
	elif FileAccess.file_exists(SAVE_PATH):
		var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
		if file:
			payload = file.get_as_text()
	if payload.is_empty():
		return {}
	var parsed = JSON.parse_string(payload)
	if parsed is Dictionary:
		var result: Dictionary = parsed
		fallback_last_active = int(result.get("last_active", 0))
		result.erase("last_active")
		return result
	return {}


func store_progress(progress: Dictionary) -> bool:
	if bridge:
		return bool(bridge.storeProgressJson(JSON.stringify(progress)))
	var copy := progress.duplicate(true)
	copy["last_active"] = int(Time.get_unix_time_from_system())
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file == null:
		return false
	file.store_string(JSON.stringify(copy))
	fallback_last_active = int(copy["last_active"])
	return true


func last_active_epoch_seconds() -> int:
	if bridge:
		return int(bridge.lastActiveEpochSeconds())
	return fallback_last_active


func now_epoch_seconds() -> int:
	if bridge:
		return int(bridge.nowEpochSeconds())
	return int(Time.get_unix_time_from_system())


func clear_progress() -> void:
	if bridge:
		bridge.clearProgress()
	elif FileAccess.file_exists(SAVE_PATH):
		DirAccess.remove_absolute(SAVE_PATH)
