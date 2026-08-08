# v3.0.1 crash-fix release QA

- Build: Gradle release build with R8/resource shrinking succeeded
- Unit tests: 16 passed, 0 failed, 0 skipped
- Android lint: no issues found
- Package: `com.example.bloodmoonnightfall`
- Version: code `30001`, name `3.0.1`
- Android: min SDK 23, target SDK 35
- Permissions: none declared
- APK signing: v1 and v2 signature verification passed
- Signer certificate SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
- Update compatibility: v2.0.0 and v3.0.1 signer certificates match
- Forbidden Android 11/13 startup API references in final DEX: 0
- Approximate bitmap allocation before the first v3.0.0 title frame: 27 MB
- Approximate v3.0.1 title background allocation: 1.76 MB; combat and boss atlases are deferred
- Bitmap decoding has half-resolution retry and non-bitmap fallback surfaces
- APK SHA-256: `D519D5108A08BC7E05033B9D1A57E8EB14AC0AC40DEB00FB088B354372CC4E81`
- Project and Desktop distribution copies have identical hashes

The user's report establishes a reproducible startup crash on their device, but the phone is not connected to ADB, so the original exception stack is unavailable. v3.0.1 removes both startup-only risks introduced in v3.0.0: direct newer-API references and eager decoding of every large bitmap.
