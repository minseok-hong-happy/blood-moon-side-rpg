# v4.2.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 17 passed, 0 failures
- Android lint: 0 errors; 1 pre-existing unused legacy sprite warning
- Package: `com.example.bloodmoonnightfall`
- Version: code `40200`, name `4.2.0-demo`
- R3 to R4 save compatibility test: passed
- Inventory round-trip and checksum validation: passed
- Signed APK verification: passed
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `483973DE6C042CC7DAEBA5683AE871ED51265AE8679AA5A1D22C3CCE656407ED`

No physical Android device was connected for touch-play validation. APK package metadata and launchable activity were verified with Android build tools.
