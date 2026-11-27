# LockedSecure — Phase 1 Baselines (`baseline-v1`)

> Tag: `baseline-v1` (commit `ac50c14` on `main`)
> Measurement date: 2026-11-27
> Build environment: Linux x86_64, OpenJDK 21 runtime / JDK 17 toolchain, Gradle 9.1.0, AGP 9.0.1, Android SDK 36
> Runtime test device: Local Android 11 (API 30) `x86_64` AOSP ATD emulator (`warden_api30`, KVM enabled, 4 vCPU, 2 GB RAM).
> Note: Target hardware (Realme Narzo 20, Helio G85, 4 GB RAM, Android 11 / Realme UI 2.0) is not physically attached; all runtime metrics below are **VERIFIED** on the API 30 emulator unless explicitly marked **ESTIMATED**.

---

## 1. Build, APK, and Dependency Baselines (Section 3a)

### Release APK Sizes (R8 minified, shrinkResources enabled, signed with test key)
Command: `ANDROID_SIGNING_* ./gradlew assembleRelease --no-daemon` + `apkanalyzer apk file-size` / `download-size` / `dex references`

| Artifact | File Size (bytes) | File Size (MiB) | Download Size (bytes) | Download Size (MiB) | DEX Method Refs |
|---|---:|---:|---:|---:|---:|
| `app-arm64-v8a-release.apk` | `4,148,087` | `3.96 MiB` | `3,756,688` | `3.58 MiB` | `67,865` (`classes.dex` 65,485 + `classes2.dex` 2,380) |
| `app-armeabi-v7a-release.apk` | `4,145,245` | `3.95 MiB` | `3,756,336` | `3.58 MiB` | `67,865` |
| `app-x86_64-release.apk` | `4,148,748` | `3.96 MiB` | `3,756,954` | `3.58 MiB` | `67,865` |
| `app-x86-release.apk` | `4,147,269` | `3.96 MiB` | `3,757,218` | `3.58 MiB` | `67,865` |
| Universal APK | N/A (`isUniversalApk = false`) | — | — | — | — |

### APK Component Breakdown (`app-arm64-v8a-release.apk`)
Command: `apkanalyzer files list` + ZIP entry accounting

| Component | Compressed (bytes) | Uncompressed (bytes) | Notes |
|---|---:|---:|---|
| `classes.dex` | `3,462,180` | `8,928,036` | 91.6% is `androidx.compose.*` kept by blanket `-keep class androidx.compose.** { *; }` |
| `resources.arsc` | `439,672` | `439,672` | Unfiltered locales & AndroidX/Material3 resources (`resourceConfigurations` not set) |
| `classes2.dex` | `120,888` | `266,096` | Multidex overflow caused by blanket Compose keep rule |
| `res/*` | `35,433` | `37,830` | Launcher mipmap WebPs + XMLs + vector tint colors |
| `META-INF/*` | `18,594` | `51,537` | Signatures, version files, Compose/AndroidX metadata |
| `kotlin/*` metadata | `12,350` | `53,396` | Kotlin builtins metadata |
| `lib/arm64-v8a/libandroidx.graphics.path.so` | `10,096` | `10,096` | Unstripped native lib pulled by Compose (`stripReleaseDebugSymbols` warned unable to strip) |
| `assets/*` | `8,154` | `8,154` | `dexopt/baseline.prof` + `baseline.profm` |
| `AndroidManifest.xml` | `1,956` | `7,084` | Includes injected `androidx.startup.InitializationProvider` & `ProfileInstallReceiver` |
| `DebugProbesKt.bin` | `777` | `1,728` | Coroutines debug probes leaked into release APK |

### DEX Class & Method Counts (`app-arm64-v8a-release.apk`)
Command: `apkanalyzer dex packages --defined-only`
- **Defined classes:** `9,061`
- **Defined methods:** `64,307`
- **Total DEX bytecode size:** `6,474,285` bytes
  - `androidx.compose.*`: `5,933,777` bytes (`59,972` methods — **91.6%** of all bytecode)
  - `androidx.compose.material3`: `1,771,287` bytes (`13,376` methods)
  - `androidx.compose.ui`: `1,747,267` bytes (`20,881` methods)
  - `androidx.compose.foundation`: `1,582,112` bytes (`16,356` methods)
  - `androidx.compose.runtime`: `519,376` bytes (`6,035` methods)

### Dependency Count (`releaseRuntimeClasspath`)
Command: `./gradlew :app:dependencies --configuration releaseRuntimeClasspath --no-daemon`
- **Direct `implementation` dependencies declared:** 12 (including Compose BOM, 3 unused Navigation3 libs, 3 unused Lifecycle libs, unused `core-ktx`)
- **Total unique resolved artifacts (direct + transitive):** **213**

### Merged Release Manifest Surprises
Command: inspected `app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml`
- Adds custom signature permission `com.example.lockedsecure.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`
- Adds `androidx.startup.InitializationProvider` executing **3 startup initializers** on every process start:
  - `androidx.emoji2.text.EmojiCompatInitializer`
  - `androidx.lifecycle.ProcessLifecycleInitializer`
  - `androidx.profileinstaller.ProfileInstallerInitializer`
- Adds exported receiver `androidx.profileinstaller.ProfileInstallReceiver`
- `extractNativeLibs="false"` (set automatically by AGP for `minSdk >= 23`)

---

## 2. Runtime Baselines on API 30 Emulator (`warden_api30`, x86_64)

| Metric | Value (VERIFIED) | Command / Methodology |
|---|---|---|
| Installed size (`/data/app/...`) | `3.9 MiB` (`4,148,748 B` `base.apk` + `oat`/`lib`) | `adb shell du -sh "$CODE_PATH"` |
| Cold start (median of 10 runs) | **233.0 ms** (min `208 ms`, max `262 ms`; `[208, 212, 220, 223, 232, 234, 235, 250, 253, 262]`) | `adb shell am force-stop` + `adb shell am start -W -n com.example.lockedsecure/.MainActivity` (10 runs) |
| Active RAM (Total PSS) | **31,676 KB** (`30.9 MiB`) — Native Heap `10,896 KB`, Code `8,864 KB` (`.dex mmap` `8,509 KB`), System `7,388 KB`, Java Heap `2,132 KB`, Private Other `2,068 KB`, Stack `328 KB` | `adb shell dumpsys meminfo com.example.lockedsecure` (2s after launch) |
| Idle RAM (Total PSS, 2 min background) | **32,822 KB** (`32.1 MiB`) — Native Heap `11,552 KB`, Code `9,040 KB` (`.dex mmap` `8,673 KB`), System `7,810 KB`, Java Heap `2,184 KB`, Private Other `1,928 KB`, Stack `308 KB` | `KEYCODE_HOME` + 120s idle + `adb shell dumpsys meminfo com.example.lockedsecure` |
| Idle CPU | `0.0%` (`TIME+ 0:00.41`), `dumpsys cpuinfo`: `+0%` | `adb shell top -b -n 1 -p <pid>` & `dumpsys cpuinfo` |
| Background jobs | **0** scheduled jobs | `adb shell dumpsys jobscheduler` |
| Alarms | **0** scheduled alarms | `adb shell dumpsys alarm` |
| Battery / wake locks | `0` app wake locks (only OS `*dexopt*` and `*launch*`); `(nothing executed)` in `Apk com.example.lockedsecure` | `adb shell dumpsys batterystats` |
| Jank (`gfxinfo framestats`) | `Total frames rendered: 0`, `Janky frames: 0 (0.00%)` *(Note: `aosp_atd` headless emulator disables display frame submission; jank on physical Realme Narzo 20 is **ESTIMATED** unverified)* | `adb shell dumpsys gfxinfo com.example.lockedsecure framestats` |

---

## 3. Code-Health Baselines (Section 3b)

### File & Function Size / Complexity
Measured via Python AST/line scanner across `app/src/main/java/**/*.kt`:
- **Total Kotlin files:** `6` (`454` total lines)
- **Files over 300 lines:** **1** (`MainActivity.kt` — `311` lines)
- **Functions over 40 lines:** **1** (`MainScreen` — `187` lines)
- **Functions over cyclomatic complexity 10:** **1** (`MainScreen` — `~CC=14`; `PowerMenuBlockerService.onAccessibilityEvent` is at `~CC=10`)
- **Max / Average cyclomatic complexity:** Max `14`, Average `3.58` across 12 functions

| File | Lines | Largest Function (lines, ~CC) |
|---|---:|---|
| `MainActivity.kt` | 311 | `MainScreen` (187 lines, ~CC=14) |
| `PowerMenuBlockerService.kt` | 41 | `onAccessibilityEvent` (26 lines, ~CC=10) |
| `theme/Type.kt` | 36 | *(top-level property, 16 lines commented-out template code)* |
| `AdminReceiver.kt` | 26 | `onEnabled` (14 lines, ~CC=4) |
| `theme/Theme.kt` | 25 | `LockedSecureTheme` (9 lines, ~CC=1) |
| `theme/Color.kt` | 15 | *(top-level constants)* |

### Code Smells & Hygiene
- **Classes with >10 public methods or >5 constructor deps:** `0`
- **Layering / dependency direction violations:** `MainActivity`, `PowerMenuBlockerService`, and `AdminReceiver` mix UI, business logic, and Android system APIs directly (no domain/data/ui boundaries).
- **Global mutable singletons / static Context holders:** `0`
- **`lateinit` count:** `0`
- **`!!` non-null assertions:** `0`
- **Broad/generic `catch (e: Exception)`:** `3` (`MainActivity.kt:80`, `MainActivity.kt:93`, `AdminReceiver.kt:21`) — none silently swallowed (all show Toast), but overly broad exception catching without modeling error types.
- **Wildcard imports:** `3` (`MainActivity.kt:16, 21, 32` — violates `CONTRIBUTING.md` "no wildcard imports")
- **TODO/FIXME:** `1` (in `res/xml/data_extraction_rules.xml:8`)
- **Commented-out code:** `16` lines in `theme/Type.kt:20-35`, plus placeholder comments in `backup_rules.xml` & `data_extraction_rules.xml`
- **Duplication (`jscpd`):** `1` exact clone, `8` lines (`1.76%`) across `6` files (`MainActivity.kt` duplicated button styling blocks)
- **Android Lint (`./gradlew lint`):** `0` errors, **`15` warnings** (`2` `UnusedResources`: `backup_rules.xml`, `data_extraction_rules.xml`; `13` outdated dependency/AGP/Gradle version warnings)
- **detekt / ktlint / spotless:** Not configured (`0` checks in CI)

### Testing & Documentation Inventory
- **Unit tests (`app/src/test`):** **0 tests** (`NO-SOURCE`)
- **Instrumented tests (`app/src/androidTest`):** **0 tests**
- **Code coverage:** **0.0%** (all 8 core contract items C1–C8 have zero automated test coverage)
- **KDoc coverage on public APIs:** **0%** (`0` KDoc blocks across all 6 Kotlin files)
- **Docs present:** `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, `.env.example`, `LICENSE`
- **Docs missing:** `docs/ARCHITECTURE.md`, `docs/adr/`, `docs/DEPENDENCIES.md`, `docs/HOW_TO.md`, `docs/RELEASING.md`, package/module docs

---

## 4. Pipeline Baseline (Section 3d)

### `actionlint` (v1.7.12) on `.github/workflows/release.yml`
- **Findings:** `0` parse/type errors (note: `shellcheck` was not installed in PATH, so shellcheck sub-rule was skipped).

### `zizmor` (v1.33.1) on `.github/workflows/release.yml`
- **Default mode:** **9 active findings** (2 high, 2 medium, 4 low, 1 informational; 7 suppressed)
- **Pedantic mode (`--pedantic`):** **12 active findings** (2 high, 2 medium, 7 low, 1 informational; 4 suppressed):
  1. `error[cache-poisoning]` (High) ×2 — lines 37, 69: `gradle/actions/setup-gradle` caching enabled in release workflow without `cache-disabled` / cache-isolation mitigation.
  2. `warning[artipacked]` (Medium) ×2 — lines 27, 57: `actions/checkout` omits `persist-credentials: false`.
  3. `help[template-injection]` (Low) ×4 (default) / ×6 (pedantic) — lines 78, 85, 115–118: `${{ github.event_name }}`, `${{ github.run_number }}`, and `${{ env.VERSION }}` interpolated directly in `run:` scripts.
  4. `help[undocumented-permissions]` (Low, pedantic) ×3 — lines 53–55: write permissions lack explanatory comments.
  5. `info[superfluous-actions]` (Info) ×1 — line 130: `softprops/action-gh-release` replaceable with built-in `gh release` CLI.

### Additional Pipeline Gaps vs. S6 & S7 Criteria
- **Secrets:** 4 repo-level secrets (`SIGNING_KEY`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`) — NOT scoped to a protected GitHub Environment with required reviewers.
- **Keystore cleanup:** Decoded `signing-key.jks` is never shredded/deleted after build.
- **Triggers:** `push: tags: v*` and unvalidated `workflow_dispatch: version_number` (allows arbitrary string injection into filenames/tags, no protected-tag enforcement).
- **Determinism:** `VERSION_CODE=$((100 + github.run_number))` is non-deterministic across reruns of the same tag!
- **Missing security controls:** No `gradle/actions/wrapper-validation`, no SBOM generation, no Dependabot/Renovate, no dependency-review workflow on PRs, no `CODEOWNERS`, no repo-settings checklist.
- **Missing reliability controls:** `concurrency.cancel-in-progress: true` on release job (unsafe!), no `apksigner verify`, no APK size budget gate, no version-vs-tag verification, no draft-then-publish idempotency, duplicated setup steps across `validate` and `release`, no `docs/RELEASING.md`.
