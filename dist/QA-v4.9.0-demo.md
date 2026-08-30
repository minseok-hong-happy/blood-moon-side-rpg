# v4.9.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 24 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40900`, name `4.9.0-demo`
- Combat anchor: x=320 on a 720 logical-pixel canvas
- Recenter trigger: x=350
- Recenter speed cap: 620 logical pixels per second
- Actor spacing during recenter: preserved
- Projectile and effect alignment during recenter: preserved by shared translation
- New enemies still spawn from the right side
- Existing R4 save compatibility: unchanged serialization schema
- Signed APK verification: passed (v1 and v2)
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `D9CBA7D3D6211DCE974B28F244FBAF867AB3A5428966BF843B6D97F3D85397CD`

No physical Android device was connected for touch-play and visual framing validation. Position formulas, build output, package metadata, lint and signature were verified automatically.
