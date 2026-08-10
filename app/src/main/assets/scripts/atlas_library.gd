class_name AtlasLibrary
extends RefCounted


static func frame(texture: Texture2D, columns: int, rows: int,
		column: int, row: int) -> AtlasTexture:
	var result := AtlasTexture.new()
	result.atlas = texture
	var cell := Vector2(texture.get_width() / float(columns), texture.get_height() / float(rows))
	result.region = Rect2(Vector2(column, row) * cell, cell)
	return result


static func animations(texture: Texture2D, columns: int, rows: int,
		definitions: Dictionary) -> SpriteFrames:
	var result := SpriteFrames.new()
	result.remove_animation(&"default")
	for animation_name in definitions:
		var definition: Dictionary = definitions[animation_name]
		var name := StringName(animation_name)
		result.add_animation(name)
		result.set_animation_speed(name, float(definition.get("fps", 10.0)))
		result.set_animation_loop(name, bool(definition.get("loop", false)))
		for cell_value in definition.get("cells", []):
			var cell: Vector2i = cell_value
			result.add_frame(name, frame(texture, columns, rows, cell.x, cell.y))
	return result


static func hero_frames(texture: Texture2D) -> SpriteFrames:
	return animations(texture, 4, 4, {
		"idle": {"fps": 5.0, "loop": true, "cells": [Vector2i(0, 1), Vector2i(0, 1)]},
		"run": {"fps": 13.5, "loop": true, "cells": [Vector2i(0, 0), Vector2i(1, 0), Vector2i(2, 0), Vector2i(3, 0)]},
		"dash": {"fps": 20.0, "loop": false, "cells": [Vector2i(1, 1), Vector2i(2, 1), Vector2i(3, 1)]},
		"attack_1": {"fps": 18.0, "loop": false, "cells": [Vector2i(0, 2), Vector2i(1, 2)]},
		"attack_2": {"fps": 19.0, "loop": false, "cells": [Vector2i(1, 2), Vector2i(2, 2)]},
		"attack_3": {"fps": 20.0, "loop": false, "cells": [Vector2i(2, 2), Vector2i(3, 2)]},
		"cast": {"fps": 12.0, "loop": false, "cells": [Vector2i(0, 3), Vector2i(1, 3)]},
		"hurt": {"fps": 1.0, "loop": false, "cells": [Vector2i(2, 3)]},
		"death": {"fps": 1.0, "loop": false, "cells": [Vector2i(3, 3)]},
	})


static func enemy_frames(texture: Texture2D, kind: int) -> SpriteFrames:
	var row := clampi(kind, 0, 2)
	return animations(texture, 4, 3, {
		"idle": {"fps": 4.0, "loop": true, "cells": [Vector2i(0, row), Vector2i(0, row)]},
		"run": {"fps": 10.0, "loop": true, "cells": [Vector2i(0, row), Vector2i(1, row)]},
		"attack": {"fps": 13.0, "loop": false, "cells": [Vector2i(2, row), Vector2i(3, row)]},
		"hurt": {"fps": 1.0, "loop": false, "cells": [Vector2i(3, row)]},
	})


static func boss_frames(texture: Texture2D) -> SpriteFrames:
	return animations(texture, 4, 3, {
		"idle": {"fps": 4.5, "loop": true, "cells": [Vector2i(0, 0), Vector2i(1, 0)]},
		"run": {"fps": 8.5, "loop": true, "cells": [Vector2i(0, 0), Vector2i(1, 0), Vector2i(2, 0), Vector2i(3, 0)]},
		"attack": {"fps": 10.5, "loop": false, "cells": [Vector2i(0, 1), Vector2i(1, 1), Vector2i(2, 1)]},
		"special": {"fps": 9.0, "loop": false, "cells": [Vector2i(0, 2), Vector2i(1, 2), Vector2i(2, 2)]},
		"death": {"fps": 1.0, "loop": false, "cells": [Vector2i(3, 2)]},
	})


static func effect_frames(texture: Texture2D, row: int, rows: int = 4) -> SpriteFrames:
	return animations(texture, 4, rows, {
		"play": {"fps": 20.0, "loop": false, "cells": [
			Vector2i(0, row), Vector2i(1, row), Vector2i(2, row), Vector2i(3, row)
		]},
	})

