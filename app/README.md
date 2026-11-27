# `:app` Module

The sole Android application module for LockedSecure (`com.example.lockedsecure`).

- **Build config:** `build.gradle.kts` (zero third-party runtime dependencies, R8 full mode, `localeFilters = ["en"]`, single universal APK).
- **ProGuard/R8 rules:** `proguard-rules.pro` (no blanket `-keep` rules; strips debug/info logs in release).
- **Lint config:** `lint.xml` (`warningsAsErrors = true`).
- **Sources:** `src/main/java/com/example/lockedsecure/` (organized by feature under `feature/` plus composition root `app/` and thin manifest entry points at root).
- **Tests:** `src/test/java/com/example/lockedsecure/` (characterization tests for C1–C8, pure domain tests, Robolectric data/UI tests, and `ArchitectureBoundaryTest`).
