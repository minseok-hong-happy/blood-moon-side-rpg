# v4.6.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 18 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40600`, name `4.6.0-demo`
- Maximum simultaneous enemies: 6
- Normal wave density: 8 to 18 enemies
- Boss wave composition: 1 boss with 2 to 4 escorts
- Normal spawn interval: 300ms
- Background exposure lift: 1.16x to 1.18x plus channel offset
- Combat darkness overlay: reduced from alpha 100 to 42
- Vignette edge alpha: reduced from 175 to 88
- Actor visibility filter and readability glow: enabled
- Existing v4.x save compatibility: unchanged save schema
- Signed APK verification: passed
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `EC252C890F3CD019019265FE216556D4B8C1143476C8E76FA6E9BCD29EC72CC1`

No physical Android device was connected for touch-play, brightness and frame-pacing validation. APK package metadata and launchable activity were verified with Android build tools.
