-keep class com.example.bloodmoonnightfall.MainActivity { *; }
-keep class com.example.bloodmoonnightfall.GodotProgressPlugin { *; }
# Defense in depth: the native engine resolves bridge classes and methods by exact JNI names.
# Release shrinking is disabled, but these rules keep the bridge intact if it is ever re-enabled.
-keep class org.godotengine.** { *; }
-keep interface org.godotengine.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,InnerClasses,EnclosingMethod,Signature
-keep @org.godotengine.godot.plugin.UsedByGodot class * { *; }
-keepclassmembers class * {
    @org.godotengine.godot.plugin.UsedByGodot <methods>;
}
