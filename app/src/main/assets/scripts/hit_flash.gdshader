shader_type canvas_item;

uniform float flash_amount : hint_range(0.0, 1.0) = 0.0;
uniform vec4 flash_color : source_color = vec4(1.0, 0.95, 0.9, 1.0);

void fragment() {
	vec4 source = texture(TEXTURE, UV) * COLOR;
	vec3 flashed = mix(source.rgb, flash_color.rgb, flash_amount);
	COLOR = vec4(flashed, source.a);
}
