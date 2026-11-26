# LockedSecure — Phase 0 Analysis (`baseline-v1`)

> Snapshot analyzed: commit `ac50c14` (tagged `baseline-v1` on `main`).
> Evidence status: Every fact below is **VERIFIED** directly from the repository source files.

---

## 1. Purpose, Target Users, and Core User Flows

### Purpose
LockedSecure (`com.example.lockedsecure`) is a minimal, offline-only Android anti-theft utility. Its goal is to prevent a thief from powering off a stolen device while the screen is locked, keeping the phone online and trackable. It requires zero internet permissions and stores no user data externally.

### Target Users
Android users (specifically targeting low-to-mid-range devices such as Realme Narzo 20, Helio G85, 4 GB RAM, Android 11 / Realme UI 2.0 with aggressive OEM background management; `minSdk = 24`, compatible with API 30+) who want a lightweight, offline lock-screen power-menu blocker with uninstall deterrence.

### Core User Flows
1. **Dashboard status inspection (`MainActivity`)**
   - User launches the app and sees a single matte-dark dashboard (`LOCKEDSECURE` / `ANTI-THEFT SHIELD`).
   - Shield badge and headline reflect combined protection state:
     - Both Accessibility Service AND Device Admin enabled → emerald green (`#10B981`), `"Shield Active"`.
     - Either disabled → muted crimson (`#EF4444`), `"Shield Offline"`.
   - Two individual status rows display live state:
     - `"Power Blocker: Protected"` / `"Power Blocker: Unprotected"`
     - `"Uninstall Protection: Protected"` / `"Uninstall Protection: Unprotected"`
   - Returning to the app (`onResume`) refreshes both statuses from Android system services.

2. **Enable Power Blocker (Accessibility Service)**
   - User taps `"Enable Power Blocker"` (or `"Blocker Service Enabled"` when active).
   - App launches `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
   - If the activity cannot be launched, app shows a short `Toast`: `"Could not open Accessibility settings"`.

3. **Enable Admin Protection (Device Administrator)**
   - User taps `"Enable Admin Protection"` (or `"Admin Protection Enabled"` when active).
   - App launches `DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN` with:
     - `EXTRA_DEVICE_ADMIN` = `ComponentName(context, AdminReceiver::class.java)`
     - `EXTRA_ADD_EXPLANATION` = `"Protects LockedSecure from deletion."`
   - If the activity cannot be launched, app shows a short `Toast`: `"Could not open Device Admin settings"`.

4. **Power Menu Interception (`PowerMenuBlockerService`)**
   - Runs as a bound Android `AccessibilityService` (`typeWindowStateChanged`, `canRetrieveWindowContent = false`).
   - On every `TYPE_WINDOW_STATE_CHANGED` event, inspects `event.packageName` and `event.className`.
   - Identifies the system power/global-actions menu when:
     - `className` contains `"GlobalActions"` (case-insensitive), OR
     - `packageName == "com.android.systemui"` AND `className` contains `"dialog"` (case-insensitive) AND `className` contains `"global"` (case-insensitive).
   - Queries `KeyguardManager.isKeyguardLocked`:
     - If locked → calls `performGlobalAction(GLOBAL_ACTION_BACK)` to dismiss the power dialog immediately.
     - If unlocked → takes no blocking action (allows normal power menu usage).

5. **Device Admin / Device Owner Uninstall Protection (`AdminReceiver`)**
   - Registered as a `DeviceAdminReceiver` with empty `<uses-policies>` (registering as Device Admin blocks normal Settings/launcher uninstall without first revoking admin).
   - In `onEnabled`, checks `DevicePolicyManager.isDeviceOwnerApp(context.packageName)`:
     - If Device Owner → calls `dpm.setUninstallBlocked(adminComponent, context.packageName, true)` and shows short Toast `"Uninstall protection activated"`.
     - If `setUninstallBlocked` throws → shows long Toast `"Could not restrict uninstall: ${e.message}"`.

---

## 2. Architecture Today

### Modules
Single Gradle module: `:app` (`com.android.application`).

### Source Files (6 Kotlin files, 454 lines total)
| File | Lines | Responsibility today |
|---|---:|---|
| `app/src/main/java/com/example/lockedsecure/MainActivity.kt` | 311 | Activity lifecycle, system-status queries, Settings intent launchers, entire Compose UI (`MainScreen`, `StatusRow`, custom Canvas shield) |
| `app/src/main/java/com/example/lockedsecure/PowerMenuBlockerService.kt` | 41 | AccessibilityService event filter, power-menu heuristic, Keyguard check, `GLOBAL_ACTION_BACK`, release `Log.d`/`Log.i` calls |
| `app/src/main/java/com/example/lockedsecure/AdminReceiver.kt` | 26 | DeviceAdminReceiver; Device Owner uninstall-block activation + Toast feedback |
| `app/src/main/java/com/example/lockedsecure/theme/Color.kt` | 15 | Compose `Color` palette constants |
| `app/src/main/java/com/example/lockedsecure/theme/Theme.kt` | 25 | Material 3 `darkColorScheme` wrapper (`LockedSecureTheme`) |
| `app/src/main/java/com/example/lockedsecure/theme/Type.kt` | 36 | Material 3 `Typography` (1 style defined, 16 lines commented-out template code) |

### Android Manifest Components & Entry Points
- **Permissions declared:** None (zero `<uses-permission>` elements — strictly offline).
- **Application flags:** `allowBackup="true"`, `supportsRtl="true"`, `windowSoftInputMode="adjustResize"`, `theme="@style/Theme.LockedSecure"`. No custom `Application` class declared.
- **Activity:** `.MainActivity` (`exported="true"`, `MAIN`/`LAUNCHER`).
- **Service:** `.PowerMenuBlockerService` (`exported="false"`, `permission="android.permission.BIND_ACCESSIBILITY_SERVICE"`, `@xml/accessibility_service_config`).
- **Receiver:** `.AdminReceiver` (`exported="true"`, `permission="android.permission.BIND_DEVICE_ADMIN"`, `DEVICE_ADMIN_ENABLED`, `@xml/device_admin`).
- **Workers:** None.
- **ContentProviders:** None declared in app manifest, **however** Jetpack dependencies merge `androidx.startup.InitializationProvider` (and Compose/Lifecycle/ProfileInstaller initializers if present) into the merged manifest — to be verified in `docs/BASELINE.md`.

### Dependency Graph & Data Flow
There is no layering (UI, domain, and Android framework queries are mixed inside `MainActivity`, `PowerMenuBlockerService`, and `AdminReceiver`):

```
MainActivity (UI + Android system queries + Intent launching)
  ├── reads Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES -> checks PowerMenuBlockerService component
  ├── reads DevicePolicyManager.isAdminActive(AdminReceiver)
  ├── launches Settings.ACTION_ACCESSIBILITY_SETTINGS
  └── launches DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN (AdminReceiver)

PowerMenuBlockerService (Android AccessibilityService + matching logic + logging)
  ├── receives AccessibilityEvent(TYPE_WINDOW_STATE_CHANGED)
  ├── reads KeyguardManager.isKeyguardLocked
  └── invokes performGlobalAction(GLOBAL_ACTION_BACK)

AdminReceiver (Android DeviceAdminReceiver + policy logic + Toast)
  └── onEnabled -> DevicePolicyManager.isDeviceOwnerApp -> setUninstallBlocked(true)
```

---

## 3. Tech Stack & Versions

| Layer | Technology | Version | Usage Status |
|---|---|---|---|
| Language | Kotlin | `2.3.20` | Active |
| Build system | Gradle Wrapper | `9.1.0` (`sha256` pinned) | Active |
| Android Gradle Plugin | `com.android.application` | `9.0.1` | Active |
| JVM Toolchain | JDK | `17` | Active |
| SDK Levels | `minSdk` / `targetSdk` / `compileSdk` | `24` / `36` / `36` | Active |
| UI Toolkit | Jetpack Compose BOM + Material 3 | `2026.03.01` | Active (heavy footprint for 1 screen) |
| Activity / Lifecycle | `activity-compose`, `lifecycle-runtime-ktx`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose` | `1.13.0` / `2.10.0` | `activity-compose` used; `lifecycle-*` & `viewmodel-compose` unused in source |
| Core KTX | `androidx.core:core-ktx` | `1.18.0` | Not directly imported anywhere |
| Navigation 3 | `navigation3-ui`, `navigation3-runtime`, `lifecycle-viewmodel-navigation3` | `1.0.1` / `2.10.0` | **Unused template residue** |
| Serialization | `org.jetbrains.kotlin.plugin.serialization` | `2.3.20` | **Unused plugin** (no `@Serializable` anywhere; dead ProGuard rules kept) |
| DI | None | — | Direct framework calls |
| Network / DB / Image loading | None | — | Offline by design |
| Testing | JUnit 4, coroutines-test, AndroidX Test, Espresso, Compose UI Test | `4.13.2`, `1.10.2`, `1.7.0`, `3.7.0` | Dependencies declared, **0 test files exist** |
| CI/CD | GitHub Actions (`.github/workflows/release.yml`) | — | Single workflow |

### Immediate Structural & Footprint Findings
1. **Blanket R8 keep rule:** `app/proguard-rules.pro` contains `-keep class androidx.compose.** { *; }`, defeating R8 shrinking across the entire Compose runtime and Material 3 library.
2. **Unused dependencies & plugins:** Navigation 3 (`navigation3-ui`, `navigation3-runtime`, `lifecycle-viewmodel-navigation3`), `kotlin-serialization` plugin, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `lifecycle-runtime-ktx`, and `core-ktx` are declared without direct usage.
3. **Unused XML resources:** `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml` exist as unreferenced template files (not referenced in `AndroidManifest.xml`), and `res/values/themes.xml` uses `Theme.Material.Light.NoActionBar` causing a white splash flash before the dark matte UI draws.
4. **Release logging:** `PowerMenuBlockerService` emits `Log.d` and `Log.i` on every window state change in release builds (logging active package/class names to logcat — privacy and overhead issue).
5. **Accessibility detection bug/edge case:** `MainActivity.isAccessibilityServiceEnabled` uses `enabled?.contains(expected) == true` (substring match on colon-separated list, which also misses flattened short-form component names like `com.example.lockedsecure/.PowerMenuBlockerService` on some Android/OEM versions where `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` stores the short name instead of the full class name!).
6. **Zero tests:** Neither `app/src/test` nor `app/src/androidTest` exists.
7. **Zero static analysis / formatting automation:** No detekt, ktlint/spotless, or architecture tests.
8. **UI toolkit footprint question:** The app has a single static dashboard screen with a 1-path vector shield, 2 status lines, and 2 buttons, yet ships Jetpack Compose + Material 3 + Kotlin coroutines/lifecycle/startup. Removing unused deps + fixing ProGuard will shrink Compose substantially; replacing Compose with pure Android framework Views (`android.app.Activity` + standard View hierarchy) would reduce APK size and idle RAM by an order of magnitude while keeping pixel-identical UI (to be evaluated quantitatively after Phase 1 baseline measurements).

---

## 4. Core Functionality Contract (Regression Contract)

Every item below **MUST** remain behaviorally intact across every iteration and pass verification on both `baseline-v1` and `overhaul/v2` (including R8-minified release builds):

| ID | Component | Exact Required Behavior |
|---|---|---|
| **C1** | Manifest / Security Surface | Package ID remains `com.example.lockedsecure`. `minSdk <= 30` (keep `24` unless approved). Zero internet or extraneous permissions added. Components: `.MainActivity` (launcher, exported), `.PowerMenuBlockerService` (`BIND_ACCESSIBILITY_SERVICE`, `exported="false"`, `canRetrieveWindowContent="false"`, `typeWindowStateChanged`), `.AdminReceiver` (`BIND_DEVICE_ADMIN`, `exported="true"`, `DEVICE_ADMIN_ENABLED`). |
| **C2** | `PowerMenuBlockerService` — Detection | On `TYPE_WINDOW_STATE_CHANGED`, matches power menu when: (a) `className` contains `"GlobalActions"` (case-insensitive), OR (b) `packageName == "com.android.systemui"` AND `className` contains both `"dialog"` and `"global"` (case-insensitive). Non-matching events and non-`TYPE_WINDOW_STATE_CHANGED` events are ignored. |
| **C3** | `PowerMenuBlockerService` — Lock-Screen Gating | When a power menu event matches AND `KeyguardManager.isKeyguardLocked == true`, invokes `performGlobalAction(GLOBAL_ACTION_BACK)`. When `isKeyguardLocked == false`, does **NOT** invoke `performGlobalAction`. |
| **C4** | `PowerMenuBlockerService` — Null Safety | Handles null `packageName` or null `className` in `AccessibilityEvent` without crashing. `onInterrupt()` is a safe no-op. |
| **C5** | `AdminReceiver` — Device Owner Uninstall Block | On `onEnabled(context, intent)`: if `dpm.isDeviceOwnerApp(context.packageName)` is true, calls `dpm.setUninstallBlocked(adminComponent, context.packageName, true)` and displays short Toast `"Uninstall protection activated"`. If `setUninstallBlocked` throws, catches safely and displays long Toast `"Could not restrict uninstall: ${e.message}"`. If not Device Owner, does not call `setUninstallBlocked` and shows no error Toast. |
| **C6** | `MainActivity` — Protection Status Detection | On `onResume()`, refreshes: (a) whether `PowerMenuBlockerService` is enabled in `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`, and (b) whether `AdminReceiver` is active via `DevicePolicyManager.isAdminActive`. |
| **C7** | `MainActivity` — Dashboard UI & Visual Parity | Single scrollable edge-to-edge matte-dark screen (`#0D0D11` background, `#17171C` card, `#22222B` border, `#10B981` active green, `#EF4444` inactive crimson). Displays `"LOCKEDSECURE"`, `"ANTI-THEFT SHIELD"`, shield outline graphic (green iff both active, else red), `"Shield Active"` iff both active else `"Shield Offline"`, `"Power Blocker: Protected|Unprotected"`, `"Uninstall Protection: Protected|Unprotected"`, button 1 (`"Blocker Service Enabled"` / `"Enable Power Blocker"`), and button 2 (`"Admin Protection Enabled"` / `"Enable Admin Protection"`). |
| **C8** | `MainActivity` — Settings Navigation & Fallback | Clicking Power Blocker button starts `Settings.ACTION_ACCESSIBILITY_SETTINGS`; on failure shows short Toast `"Could not open Accessibility settings"`. Clicking Admin Protection button starts `DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN` with `EXTRA_DEVICE_ADMIN = AdminReceiver` and `EXTRA_ADD_EXPLANATION = "Protects LockedSecure from deletion."`; on failure shows short Toast `"Could not open Device Admin settings"`. |

---

## 5. Current Release Workflow Inventory

### File: `.github/workflows/release.yml`
- **Triggers:**
  - `push` on tags matching `v*`
  - `workflow_dispatch` with required string input `version_number`
- **Concurrency:** `${{ github.workflow }}-${{ github.ref }}` with `cancel-in-progress: true` (risky for releases if a concurrent run cancels a mid-publish release!).
- **Top-level permissions:** `contents: read`.
- **Jobs:**
  1. `validate` (`ubuntu-latest`, timeout 15m):
     - `actions/checkout@11bd7190...` (v4.2.2) — missing `persist-credentials: false`.
     - `actions/setup-java@c5195efe...` (v4.7.1, Temurin JDK 17).
     - `gradle/actions/setup-gradle@d9c87d48...` (v3.5.0, `cache-read-only: ${{ github.event_name != 'push' }}`).
     - Runs `./gradlew lint --no-daemon` and `./gradlew test --no-daemon`.
     - Missing: Gradle wrapper validation (`gradle/actions/wrapper-validation`), formatting/static analysis checks, PR trigger (`pull_request` CI workflow does not exist at all!).
  2. `release` (`needs: validate`, `ubuntu-latest`, timeout 30m):
     - Permissions: `contents: write`, `id-token: write`, `attestations: write` (no protected GitHub `environment:` configured; secrets are repository-level!).
     - Duplicated checkout/JDK/Gradle setup (no composite action or reusable workflow).
     - `Determine Version`: computes `VERSION_CODE=$((100 + ${{ github.run_number }}))` — **non-deterministic** (re-running the same tag changes `versionCode`!), and interpolates `${{ github.event_name }}` and `${{ github.run_number }}` directly into `run:`.
     - `Decode Keystore`: uses third-party `timheuer/base64-to-file@ca9e30ba...` writing `signing-key.jks`, **never deletes the decoded keystore file** after build (no `if: always()` cleanup step).
     - `Build Split Release APKs`: passes `ANDROID_SIGNING_*` and `APP_VERSION_*` env vars to `./gradlew assembleRelease`.
     - `Rename Artifacts`: interpolates `${{ env.VERSION }}` directly in shell `run:` (zizmor `template-injection` finding; `workflow_dispatch` input `version_number` is unvalidated!).
     - `Generate SHA-256 Checksums`: `sha256sum locked-secure-*.apk > SHA256SUMS.txt`.
     - `Attest Build Provenance`: `actions/attest-build-provenance@c074443f...` (v1.4.3). Missing SBOM generation!
     - `Publish GitHub Release`: `softprops/action-gh-release@c95fe148...` (v2.2.1), `draft: false` directly — no APK verification (`apksigner verify`), no APK size budget check, no version/tag check, no idempotent staging (draft-then-publish) against partial failures.
- **Secrets used (repo-level, no GitHub Environment):** `SIGNING_KEY`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`.
- **Missing security/supply-chain hygiene:** No Dependabot/Renovate config, no `pull_request` validation workflow, no dependency review action, no Gradle wrapper validation, no SBOM, no `apksigner verify`, no `CODEOWNERS`, no `docs/RELEASING.md`.

---

## 6. Incremental Strangler vs. From-Scratch Rewrite Assessment

**Decision recommendation: Incremental strangler migration on `overhaul/v2`.**

**Rationale:**
- The codebase is only **6 Kotlin files (454 lines)** with a clean, tiny surface area (1 Activity, 1 AccessibilityService, 1 DeviceAdminReceiver) and zero tangled persistence/network/database layers.
- Every piece of core logic (`PowerMenuBlockerService` event classification, `AdminReceiver` policy handling, protection status inspection, settings navigation, and UI rendering) can be extracted behind clean, testable feature interfaces (`domain` / `data` / `ui` boundaries) one small, behavior-preserving commit at a time while keeping the app buildable and passing all characterization tests at every single commit.
- Even a major UI/dependency optimization (such as shrinking Compose vs. migrating the single screen to pure Android Views behind a stable UI boundary) can be executed incrementally behind clean interfaces without discarding the existing package structure, manifest components, or tests.
- Therefore, a full from-scratch rewrite is **not** justified; we will follow the default **incremental strangler** approach on `overhaul/v2` and document this in an ADR (`docs/adr/`).
