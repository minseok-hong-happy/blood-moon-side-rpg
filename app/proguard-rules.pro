-keep class com.example.bloodmoonnightfall.MainActivity { *; }
-keep class com.example.bloodmoonnightfall.GodotProgressPlugin { *; }
-keep @org.godotengine.godot.plugin.UsedByGodot class * { *; }
-keepclassmembers class * {
    @org.godotengine.godot.plugin.UsedByGodot <methods>;
}
