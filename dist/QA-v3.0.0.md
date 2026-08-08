# v3.0.0 release QA

- Build: Gradle release build with R8/resource shrinking succeeded
- Unit tests: 16 passed, 0 failed, 0 skipped
- Android lint: no issues found
- Package: `com.example.bloodmoonnightfall`
- Version: code `30000`, name `3.0.0`
- Android: min SDK 23, target SDK 35, portrait feature declared
- Permissions: none declared (including no `INTERNET` permission)
- APK signing: v1 and v2 signature verification passed
- Signer certificate SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- Update compatibility: v2.0.0 and v3.0.0 signer certificates match
- APK SHA-256: `23138063C401E57E3CF479C835048F09EE6F88FBC1EBC090FA3461626FC02DFF`
- Distribution copies: project `dist` copy and Desktop copy have identical hashes

Runtime limitation: no Android phone is connected to this PC, and the available PC configuration has no working Android emulator hypervisor. Device-level frame pacing, touch ergonomics, and OEM-specific installation behavior therefore require final confirmation on a physical Android device.
