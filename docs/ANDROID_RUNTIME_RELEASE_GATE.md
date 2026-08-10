# Android runtime release gate

## Why this gate exists

VAYLORN 5.0.0 passed compilation, JVM tests, Godot headless simulation, APK content checks,
signature verification, and 16 KB ZIP alignment, but it still crashed on every Android launch.
An ARM Android 7.0 reproduction exposed two consecutive startup defects. First, the process
aborted inside JNI:

```text
JNI DETECTED ERROR IN APPLICATION
java.lang.NoSuchMethodError:
GodotNativeBridge.getRenderView()Lorg/godotengine/godot/GodotRenderView;
Fatal signal 6 (SIGABRT)
```

R8 had changed or removed a JVM method that Godot resolves by its exact JNI name. Static build
checks could not detect that native-to-JVM lookup failure. After fixing it, the same runtime gate
caught a second defect before release: the embedded Android runtime requires an exported
`project.binary`, while the old packager copied only the editor-facing `project.godot`.

## Permanent safeguards

1. Production builds do not run R8 or resource shrinking. This matches Godot's official Android
   template and avoids rewriting the engine's JNI surface.
2. `app/proguard-rules.pro` keeps every `org.godotengine` class and member as defense in depth if
   shrinking is accidentally enabled later.
3. The release builder exports and validates an `ECFG`-formatted `project.binary`, then verifies
   that the exact file is present once inside the APK.
4. Android and Godot emit boot markers for activity creation, engine setup, main-loop start, and
   `GAME_READY` after the real scene, hero, enemies, and first combat wave are constructed.
5. Production remains ARM-only. The builder also creates an x86_64 QA variant from the same
   release configuration and rejects it unless every Godot asset is byte-identical to the signed
   ARM APK. This allows full rendering tests without adding a desktop ABI to the mobile download.
6. `tools/build_release.ps1` invokes `tools/android_runtime_smoke.ps1` against that QA variant. A
   release is not copied to `dist` unless it installs, reaches `GAME_READY`, survives the initial
   combat loop, and has no Java/JNI/native crash record.

## Local runtime environment

The Windows gate uses an API 24 x86_64 AVD with OpenGL ES 3, the minimum graphics path required by
Godot 4. It also initializes the old emulator kernel's entropy pool with the small
`tools/android_entropy_seed.c` QA utility. The AVD, compiled utility, and system image live under
`%LOCALAPPDATA%\BloodMoonNightfall`, outside the repository and release APK.

The Bash equivalent, `tools/android_runtime_smoke.sh`, is available for a Linux CI runner. Adding
or changing `.github/workflows/*` requires a GitHub credential with the `workflow` scope.
