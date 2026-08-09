# v4.10.0-demo QA

- Release build with R8/resource shrinking: passed
- JVM unit tests: 25 passed, 0 failures
- Android lint: 0 issues
- Package: `com.example.bloodmoonnightfall`
- Version: code `41000`, name `4.10.0-demo`
- Maximum active enemies: 10
- Reinforcement batch: 3 enemies when active count is 0 to 3
- Normal wave size: 18 to 36 enemies
- Boss wave size: 7 to 9 enemies including one boss
- Normal spawn interval: 160ms
- Boss reinforcement interval: 260ms
- Enemy crowd spacing: 44 logical pixels
- Particle safety cap: retained at 420
- Skill-effect safety cap: retained at 48
- Existing R4 save compatibility: unchanged serialization schema
- Signed APK verification: passed (v1 and v2)
- Signer SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- APK SHA-256: `BDB638367A36B8E45C2FA0BF1DAB89833F0422EE20E2B4DDCE05CA65F45A86E2`

No physical Android device was connected for sustained frame-rate and visual crowd-density validation. Spawn formulas, capacity guards, build output, lint and signature were verified automatically.
