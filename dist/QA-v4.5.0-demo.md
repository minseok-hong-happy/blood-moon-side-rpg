# v4.5.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 18 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40500`, name `4.5.0-demo`
- Moving basic combo: enabled with forward hit follow-through
- Automatic skill rotation: spear, siphon, nova, blood rush and blade rain
- New skill unlocks: blood rush at level 3, blade rain at level 6
- Hero/regular enemy/boss render reduction: approximately 23%/19%/18%
- Comfort camera: shake 24%, kick 42%/34%, zoom 36% of the previous render amplitude
- Existing v4.x save compatibility: unchanged save schema
- Signed APK verification: passed
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `0D6C8D587379520FA95A267CB2C137F1B2D19CAB4C5E7726CC4B3CFAFBE91B00`

No physical Android device was connected for touch-play, comfort and frame-pacing validation. APK package metadata and launchable activity were verified with Android build tools.
