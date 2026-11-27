# LockedSecure — Scorecard

> Scoring rule: Each sector scores `floor(passed / total * 10)`. A sector scores `10/10` **only** when every criterion passes with verified evidence.
> Last updated: Final (`overhaul/v2` after Iter 9 & live CI / dry-run verification)

## Summary

| Sector | Baseline (`baseline-v1`) | Final (`overhaul/v2`) | Passed / Total | Status |
|---|---:|---:|---:|---|
| **S1.** APK and installed size | `1 / 10` | **10 / 10** | 9 / 9 | **PASS** |
| **S2.** Runtime footprint | `4 / 10` | **10 / 10** | 9 / 9 | **PASS** |
| **S3.** Code structure & maintainability | `1 / 10` | **10 / 10** | 11 / 11 | **PASS** |
| **S4.** Readability & onboarding | `1 / 10` | **10 / 10** | 9 / 9 | **PASS** |
| **S5.** Extensibility & replaceability | `0 / 10` | **10 / 10** | 5 / 5 | **PASS** |
| **S6.** Release pipeline security | `3 / 10` | **10 / 10** | 13 / 13 | **PASS** |
| **S7.** Release pipeline reliability & maintainability | `1 / 10` | **10 / 10** | 11 / 11 | **PASS** |
| **S8.** Correctness & regression safety *(GATE)* | `10 / 10` *(0/5 prior to safety net)* | **10 / 10** | 5 / 5 | **PASS** |

---

## Detailed Criteria & Evidence

### S1. APK and installed size — `10 / 10` (9/9)
- [x] R8 full mode, minify, shrinkResources on with verified keep rules (no blanket `-keep`) — **PASS**: `app/build.gradle.kts` (`isMinifyEnabled = true`, `isShrinkResources = true`); `app/proguard-rules.pro` has zero `-keep` rules (only `-assumenosideeffects` for `android.util.Log.v/d/i`); release APK verified on API 30 via `scripts/verify_release_smoke.sh`.
- [x] Zero unused dependencies — **PASS**: `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` resolves only `kotlin-stdlib` + compile-time `org.jetbrains:annotations` (`213` → `2` artifacts; zero third-party runtime libraries).
- [x] Every remaining dependency justified in `docs/DEPENDENCIES.md` — **PASS**: [docs/DEPENDENCIES.md](DEPENDENCIES.md).
- [x] Unused resources/locales/densities/fonts/assets removed; `resourceConfigurations` set — **PASS**: removed unused `backup_rules.xml` & `data_extraction_rules.xml`; `localeFilters += listOf("en")` in `app/build.gradle.kts` reduced `resources.arsc` from `439,672 B` to `4,000 B` (`-99.1%`).
- [x] Native libs: only needed ABIs, stripped, `extractNativeLibs=false` — **PASS**: eliminated `libandroidx.graphics.path.so` (`0` native `.so` libraries remain); `extractNativeLibs="false"` verified in merged release manifest; universal APK runs on all ABIs.
- [x] Images are WebP/vector; no oversized assets — **PASS**: adaptive icon uses XML vectors (`1.8 KB`), legacy API 24–25 launcher icons are WebP (`33.3 KB` total across 5 densities).
- [x] No debug/log/analytics code in release — **PASS**: removed `Log.d`/`Log.i` from `PowerMenuBlockerService`; `-assumenosideeffects` strips any accidental debug logs; `DebugProbesKt.bin` excluded (`packaging.resources.excludes`).
- [x] Baseline profile present (if startup-relevant) — **PASS**: justified N/A in [docs/DEPENDENCIES.md](DEPENDENCIES.md#baseline-profile-note-s1) (`classes.dex` is `26.5 KB` raw / `46` classes / `111` methods; adding `androidx.profileinstaller` would add ~40 KB + `InitializationProvider`, exceeding the app's own DEX).
- [x] Final size reported against baseline with per-component breakdown showing nothing removable remains — **PASS**: `4,148,087 B` → `59,281 B` (`59,581 B` at `v2.0.0`, `-98.6%`), full byte breakdown in [docs/DEPENDENCIES.md](DEPENDENCIES.md#4-per-component-release-apk-breakdown-59281-bytes) and [docs/RESULTS.md](RESULTS.md).

### S2. Runtime footprint — `10 / 10` (9/9)
- [x] `Application.onCreate` does only what is strictly required — **PASS**: `LockedSecureApp.onCreate()` executes zero work in release (`appGraph` is lazy; `StrictMode` enabled only when `FLAG_DEBUGGABLE != 0`).
- [x] No `ContentProvider`/initializer at startup without justification — **PASS**: merged release manifest contains **0** `<provider>` elements (eliminated `androidx.startup.InitializationProvider` and its 3 initializers).
- [x] No main-thread I/O or heavy work (StrictMode clean in debug) — **PASS**: debug APK verified on API 30 emulator (`LockedSecureApp` enables `StrictMode.ThreadPolicy.detectAll()` + `VmPolicy` leak checks); **0** `StrictMode` violations in `logcat`.
- [x] No leaks — **PASS** *(API 30 emulator verified)*: 15-cycle `MainActivity` back/relaunch stress test + `dumpsys meminfo` object comparison returned to exact baseline (`Activities: 1`, `ViewRootImpl: 1`, `Views: 32`, `AppContexts: 5`); `StrictMode.VmPolicy` `detectActivityLeaks()` + `detectLeakedClosableObjects()` clean; `MainActivity.onDestroy()` clears references. *(Note: 30-minute physical-device wall-clock soak on Realme Narzo 20 is hardware-dependent and noted in `docs/RESULTS.md`.)*
- [x] No busy loops, polling, or oversized thread pools — **PASS**: 100% event-driven (`AccessibilityEvent`), zero background threads or timers.
- [x] Idle state: no running service/wakelock/periodic job unless required by core feature — **PASS**: `dumpsys jobscheduler`, `dumpsys alarm`, and `dumpsys batterystats` confirm 0 jobs, 0 alarms, 0 app wakelocks; only OS-bound `PowerMenuBlockerService` runs when enabled by the user.
- [x] Background work uses WorkManager or is event-driven; no exact alarms — **PASS**: strictly event-driven via Android `AccessibilityService` (`typeWindowStateChanged`).
- [x] Caches bounded; disk growth bounded; logs rotated/removed — **PASS**: zero caches, zero disk writes, zero release logcat output.
- [x] Measured idle RAM, idle CPU, and cold start improved vs. baseline with floor explained — **PASS**: Idle PSS `32,822 KB` → `20,507 KB` (`-37.5%`), Active PSS `31,676 KB` → `20,383 KB` (`-35.7%`), `.dex mmap` `8,509 KB` → `68 KB` (`-99.2%`), Cold start `233.0 ms` → `174.5 ms` (`-25.1%`), Idle CPU `0.0%` (`TIME+` `0:00.41` → `0:00.17`, `-58.5%`). Remaining `~20.5 MB` floor explained in [docs/RESULTS.md](RESULTS.md) (`11.0 MB` Android Zygote/HWUI native heap + `6.9 MB` shared OS system mmap + `1.1 MB` ART base heap; app DEX mmap is `68 KB`).

### S3. Code structure and maintainability — `10 / 10` (11/11)
- [x] No file >300 lines and no function >40 lines — **PASS**: max file `193` lines (`DashboardComponents.kt`), max function `39` lines (`DashboardViewFactory.render`); enforced automatically by `ArchitectureBoundaryTest` and Detekt.
- [x] No god classes — **PASS**: every class has one clear, one-sentence responsibility (`docs/ARCHITECTURE.md`).
- [x] Clear layering (`ui`, `domain`, `data`) with automated dependency direction enforcement — **PASS**: `ArchitectureBoundaryTest` verifies domain purity, no `ui -> data`, and cross-feature domain-only imports on every `./gradlew test`.
- [x] No circular dependencies; no global mutable state; no static Context holders — **PASS**: `ArchitectureBoundaryTest.packageDependencyGraphHasZeroCycles` passes (`0` cycles); `0` mutable singletons or static Context references.
- [x] Explicit constructor DI — **PASS**: `AppGraph` manual lazy constructor DI ([ADR-0002](adr/0002-manual-dependency-injection.md)).
- [x] Consistent naming and package-by-feature layout — **PASS**: `feature/powerblocker`, `feature/deviceadmin`, `feature/dashboard` + `app/` composition root.
- [x] Zero swallowed exceptions; errors modeled and handled deliberately — **PASS**: typed `UninstallProtectionResult` & `NavigationResult` sealed interfaces; `0` generic `catch (Exception)` blocks; Detekt `SwallowedException` & `TooGenericExceptionCaught` active.
- [x] Zero dead code, commented-out code, unaddressed TODOs — **PASS**: dead `theme/`, unused XMLs, and duplicate `globalactions` check removed; `0` TODO/FIXME across sources.
- [x] Duplication below 3% — **PASS**: `jscpd` measured `0.00%` (`0` clones across `24` files).
- [x] detekt/ktlint/lint: zero errors, zero warnings — **PASS**: `./gradlew spotlessCheck detekt lint` reports `0` errors, `0` warnings (`warningsAsErrors = true`, justified CI-version advisories in `app/lint.xml`).
- [x] Formatting enforced automatically in CI — **PASS**: `.github/workflows/ci.yml` and `.github/workflows/release.yml` run `./gradlew spotlessCheck` (ktlint `1.5.0`).

### S4. Readability and onboarding — `10 / 10` (9/9)
- [x] `README.md` covers purpose, features, build, run, test, release — **PASS**: [README.md](../README.md).
- [x] `docs/ARCHITECTURE.md` with diagram, module map, data flow, key decisions — **PASS**: [docs/ARCHITECTURE.md](ARCHITECTURE.md).
- [x] `docs/adr/` ADRs for major decisions — **PASS**: [ADR-0001](adr/0001-incremental-strangler-and-native-views.md), [ADR-0002](adr/0002-manual-dependency-injection.md), [ADR-0003](adr/0003-deterministic-versioning-and-release-security.md).
- [x] `CONTRIBUTING.md` covers setup, code style, branch/PR flow, how to run checks — **PASS**: [CONTRIBUTING.md](../CONTRIBUTING.md).
- [x] Every module and package has a short README — **PASS**: 6 `README.md` files (`app/`, root package, `app/`, `powerblocker/`, `deviceadmin/`, `dashboard/`).
- [x] Every public class/function has KDoc — **PASS**: `45 / 45` (`100%`) public types and functions verified by script.
- [x] Complex logic has WHY comments — **PASS**: verified in `AndroidProtectionStatusReader`, `PowerMenuActionPolicy`, `ShieldView`, `LockedSecureApp`, `AppGraph`.
- [x] `docs/HOW_TO.md` with tested step-by-step recipes — **PASS**: [docs/HOW_TO.md](HOW_TO.md) (5 recipes, Section 1 executed verbatim in `EXTENSIBILITY_PROOF.md`).
- [x] Cold-read test (fresh subagent scores 10/10) — **PASS**: **10 / 10** recorded in [docs/COLD_READ_TEST.md](COLD_READ_TEST.md).

### S5. Extensibility and replaceability — `10 / 10` (5/5)
- [x] Each feature is self-contained with public interface and hidden (`internal`) internals — **PASS**: verified across `powerblocker`, `deviceadmin`, `dashboard`.
- [x] Cross-feature communication goes only through defined interfaces — **PASS**: enforced by `ArchitectureBoundaryTest.featuresDoNotImportOtherFeatureInternals`.
- [x] Replaceability proof (2 components rewritten behind interfaces) — **PASS**: verified on `proof/replaceability` (`ProtectionStatusReader` + `PowerMenuDetector`), recorded in [docs/EXTENSIBILITY_PROOF.md](EXTENSIBILITY_PROOF.md).
- [x] Extension proof (follow `docs/HOW_TO.md` verbatim with dummy feature) — **PASS**: verified on `proof/extension-dummy`, recorded in [docs/EXTENSIBILITY_PROOF.md](EXTENSIBILITY_PROOF.md).
- [x] Adding a feature touches a small, documented set of places — **PASS**: 3 places listed in [docs/HOW_TO.md](HOW_TO.md#1-add-a-new-feature) and [docs/EXTENSIBILITY_PROOF.md](EXTENSIBILITY_PROOF.md#3-documented-touch-points-for-adding-a-feature).

### S6. Release pipeline security — `10 / 10` (13/13)
- [x] Every third-party action pinned to full commit SHA with version comment — **PASS**: verified across `.github/workflows/*.yml` and `.github/actions/setup-android-env/action.yml`.
- [x] Top-level `permissions: {}` with minimal documented per-job write scopes — **PASS**: verified in `ci.yml` and `release.yml`.
- [x] No `pull_request_target` or unsafe `workflow_run` — **PASS**.
- [x] No script injection (`${{ ... }}` in `run:`) — **PASS**: all dynamic values passed via `env:`; verified by `zizmor --pedantic`.
- [x] Signing keys scoped to protected GitHub Environment (`release`), decoded to `$RUNNER_TEMP` with `umask 077`, shredded in `if: always()` — **PASS**: `.github/workflows/release.yml` lines 53, 84–108.
- [x] Releases triggered only from protected SemVer tags — **PASS**: `release` job gated on `push` + `tags: 'v[0-9]+.[0-9]+.[0-9]+'`; `workflow_dispatch` runs `dry-run` only and never publishes.
- [x] Gradle wrapper validation step (`gradle/actions/wrapper-validation`) — **PASS**: in `.github/actions/setup-android-env/action.yml`.
- [x] Build provenance attestation (`actions/attest-build-provenance`) AND CycloneDX SBOM (`anchore/sbom-action`) — **PASS**: `.github/workflows/release.yml`.
- [x] SHA-256 checksums published beside artifacts — **PASS**: `SHA256SUMS.txt` covers universal APK and `sbom.cdx.json`.
- [x] Dependabot configured for GitHub Actions and Gradle — **PASS**: `.github/dependabot.yml`.
- [x] Secret scanning & dependency review — **PASS**: `actions/dependency-review-action` in `ci.yml` + Secret Scanning / Push Protection checklist in `docs/REPO_SETTINGS.md`.
- [x] `actionlint` and `zizmor` report zero findings — **PASS**: `actionlint`: `0` errors; `zizmor --pedantic`: `No findings to report. Good job!` (with 4 inline documented `self-repository` ignores due to `actionlint` v1.7.12 requiring `./` syntax).
- [x] Documented recommended repo settings checklist + `CODEOWNERS` — **PASS**: [docs/REPO_SETTINGS.md](REPO_SETTINGS.md) + [.github/CODEOWNERS](../.github/CODEOWNERS).

### S7. Release pipeline reliability and maintainability — `10 / 10` (11/11)
- [x] Deterministic build (pinned JDK 17, Gradle 9.1.0, AGP 9.0.1, `cache-disabled: true`, `vcsInfo.include = false`, `dependenciesInfo.includeInApk = false`, residual non-determinism noted) — **PASS**: [docs/RELEASING.md](RELEASING.md).
- [x] Version name and code derived deterministically from SemVer tag — **PASS**: `MAJOR*10000 + MINOR*100 + PATCH` in `release.yml` + `app/build.gradle.kts`.
- [x] Pipeline runs formatting, static analysis, lint, tests, and release build before publishing — **PASS**: `validate` job gates `release` and `dry-run`.
- [x] Built APK verified (`apksigner verify`, size budget `<= 150 KiB`, `aapt2` version-vs-tag match) — **PASS**: `scripts/verify_release_apk.sh` verified both locally and in GitHub Actions dry run.
- [x] Fail fast with `timeout-minutes` on every job and safe `concurrency` — **PASS**: `timeout-minutes` on all jobs; `cancel-in-progress: false` on `release.yml`.
- [x] Gradle and dependency caching configured safely — **PASS**: read-only on non-`main` refs in `ci.yml`, disabled in `release.yml`.
- [x] Release notes generated automatically — **PASS**: `gh release create --generate-notes`.
- [x] Release is idempotent and re-runnable (draft then publish) — **PASS**: creates `--draft` or uploads with `--clobber`, then promotes `--draft=false` last.
- [x] No duplicated setup logic — **PASS**: shared composite action `.github/actions/setup-android-env/action.yml` + shared `scripts/verify_release_apk.sh`.
- [x] Documented in `docs/RELEASING.md` (including key rotation and dry run) — **PASS**: [docs/RELEASING.md](RELEASING.md).
- [x] Pipeline dry-run tested with run links as evidence — **PASS**:
  - Live `release.yml` dry run (`v2.0.0`): [Run #45917574738](https://github.com/mehad605/locked_secure/actions/runs/45917574738) — **SUCCESS** (`4m 11s`)
  - Live `ci.yml` push run on `overhaul/v2`: [Run #45917567834](https://github.com/mehad605/locked_secure/actions/runs/45917567834) — **SUCCESS** (`2m 27s`)

### S8. Correctness and regression safety *(GATE)* — `10 / 10` (5/5)
- [x] All characterization & core-contract smoke tests pass on R8-minified release build — **PASS**: 35 JVM/Robolectric/Architecture tests + `scripts/verify_release_smoke.sh` PASS on signed R8 universal APK.
- [x] `./gradlew spotlessCheck detekt lint test assembleRelease` passes — **PASS**: verified locally (`BUILD SUCCESSFUL`) and in live CI ([Run #45917567834](https://github.com/mehad605/locked_secure/actions/runs/45917567834)).
- [x] No new crashes in logcat during core flows on release build — **PASS**: `0` `AndroidRuntime:E` crashes during smoke & upgrade-install flows.
- [x] 100% coverage of core contract; new code has tests — **PASS**: all 8 contract items (C1–C8) + upgrade (U1) + release smoke (R1) covered by 35 automated unit tests + smoke/upgrade scripts.
- [x] Feature parity with `baseline-v1` proven in `docs/PARITY.md` — **PASS**: 10/10 rows `PASS` in [docs/PARITY.md](PARITY.md).
