# v4.8.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 22 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `40800`, name `4.8.0-demo`
- Total combat skills: 8
- Manual skill icons: dash, blood spear, siphon, blood moon nova
- Automatic skill unlocks: levels 3, 6, 9, 12 and 15
- New damage formulas: bounded and growth-scaling
- Maximum chain targets: 4
- Crimson pillar targets: all active enemies in range
- Eclipse targets: all active enemies
- Existing R4 save compatibility: unchanged serialization schema
- Offline reward system: retained
- Android Canvas draw calls: no allocated bitmap assets added
- Signed APK verification: passed (v1 and v2)
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `20B970082A80F29725795F0FE1FC33EB4CC21FB167635AF96A1A225E0CDCB70A`

No physical Android device was connected for touch-play, icon readability, effect pacing or sustained frame-rate validation. Package metadata, tests, lint, R8 output and signature were verified automatically.
