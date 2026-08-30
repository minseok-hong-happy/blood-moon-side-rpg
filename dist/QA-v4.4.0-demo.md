# v4.4.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 17 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40400`, name `4.4.0-demo`
- Basic attack hit timing: 80/90/130ms
- Skill activation timing: spear 110ms, siphon 150ms, nova 220ms
- Skill input buffer: 420ms
- Hit-confirm basic-to-skill cancel: enabled with cooldown/resource/unlock/range guards
- Existing v4.x save compatibility: unchanged save schema
- Signed APK verification: passed
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `F92C78A94741101CB7B43CA7E34907231D8737809583C981B88C30CAD1411051`

No physical Android device was connected for touch-play and frame-pacing validation. APK package metadata and launchable activity were verified with Android build tools.
