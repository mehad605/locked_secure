# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Replaced Jetpack Compose, Material 3, Navigation 3, and AndroidX runtime dependencies with pure Android framework Views (`DashboardViewFactory`, `DashboardComponents`, `ShieldView`), preserving exact matte-dark visual parity and `minSdk = 24` compatibility while shrinking the universal release APK from `4.15 MB` to `~59 KB` (`-98.6%`), DEX method references from `67,865` to `279` (`-99.6%`), and idle RAM from `32.8 MB` to `20.5 MB` (`-37.5%`).
- Refactored into a clean package-by-feature architecture (`feature/powerblocker`, `feature/deviceadmin`, `feature/dashboard`) with `domain` / `data` / `ui` boundaries and lazy manual dependency injection (`AppGraph`).
- Switched release packaging from four ABI-split APKs to a single universal APK after eliminating the last native `.so` library (`libandroidx.graphics.path.so`).
- Fixed Accessibility Service status detection to parse colon-separated `ComponentName` entries (handling both full and OEM short component names without substring false positives).
- Removed release `Log.d`/`Log.i` logging from `PowerMenuBlockerService` and eliminated the white startup flash via a dark matte window theme.

### Added
- Automated characterization, unit, Robolectric, and `ArchitectureBoundaryTest` test suite (35 tests) plus R8 release smoke test script (`scripts/verify_release_smoke.sh`).
- Spotless (`ktlint`), Detekt, and zero-warning Android Lint (`warningsAsErrors = true`) gates.
- Debug-only `StrictMode` thread and VM verification in `LockedSecureApp`.
- Hardened CI (`.github/workflows/ci.yml`) and release pipeline (`.github/workflows/release.yml`) with SHA-pinned actions, Gradle wrapper validation, deterministic SemVer `versionCode`, `apksigner`/size/version verification (`scripts/verify_release_apk.sh`), CycloneDX SBOM, SLSA build provenance attestation, `SHA256SUMS.txt`, idempotent draft-then-publish releases, `workflow_dispatch` dry-run support, Dependabot, and `CODEOWNERS`.
- Complete engineering documentation: `docs/ARCHITECTURE.md`, `docs/DEPENDENCIES.md`, `docs/HOW_TO.md`, `docs/RELEASING.md`, `docs/REPO_SETTINGS.md`, `docs/adr/`, and package-level READMEs.

## [1.0.0] - 2026-06-20

### Added
- Initial project setup with Kotlin + Jetpack Compose
- Power menu blocking via Accessibility Service
- Device Admin receiver for uninstall protection
- Single-screen dashboard with protection status
- Split APK build configuration (`armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`)
- GitHub Actions CI/CD pipeline for automated release builds
- Dark theme with matte finish palette
