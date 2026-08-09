# v4.7.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 22 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40700`, name `4.7.0-demo`
- Minimum Android version: API 23
- Offline reward minimum: 60 seconds
- Offline reward cap: 8 hours
- Missing timestamp and backward-clock reward: 0
- Gold reward scaling: level, region and wave
- XP reward scaling: level, region and wave
- Gold safety cap: 100,000,000 total
- XP safety cap: 9,999,999 before level conversion
- Reward persistence: saved before report confirmation
- Existing v4.x save compatibility: unchanged R4 serialization schema
- Signed APK verification: passed (v1 and v2)
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `2E943D20D48A1917AED9CAC4157F8A2F14759B2F80B266D938FB775D72F86E33`

No physical Android device was connected for touch-play, reward-screen layout and frame-pacing validation. APK package metadata, version, launchable activity and signature were verified with Android build tools.
