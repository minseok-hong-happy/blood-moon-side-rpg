# v4.3.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 17 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40300`, name `4.3.0-demo`
- New hero atlas: 16 poses, transparent PNG, chroma despill and 1 px edge contraction verified
- Fixed-step position interpolation: enabled for hero, regular enemies and bosses
- Hot render-path source rectangle reuse: enabled
- Signed APK verification: passed
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `59BBABE6E004A201EF6FC9D83DC167A826A7F0BEC97C51649909DA1EBE19EB12`

No physical Android device was connected for touch-play and frame-pacing validation. APK package metadata and launchable activity were verified with Android build tools.
