# v4.11.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 25 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `41100`, name `4.11.0-demo`
- VFX atlas dimensions: 1776 × 887
- VFX atlas cells: 8 in a 4 × 2 layout
- Transparent pixels: 1,036,329 / 1,573,538
- Partially transparent edge pixels: 53,225 / 1,573,538
- VFX atlas file size: 1,602,101 bytes
- Final APK size: 13,923,413 bytes
- Primary skill rendering: hand-painted bitmap atlas
- Procedural shape rendering: decode-failure fallback only
- Bitmap lifecycle: lazy load and explicit recycle
- Existing R4 save compatibility: unchanged serialization schema
- Signed APK verification: passed (v1 and v2)
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `201277555F7024CE713883DF7028C0DB1AD5B633729518B62410A644B37867C4`

No physical Android device was connected for real-device alpha blending, effect pacing and sustained frame-rate validation. Resource packaging, build output, lint, unit tests and signature were verified automatically.
