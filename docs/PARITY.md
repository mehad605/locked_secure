# LockedSecure — Feature Parity Matrix (`baseline-v1` vs `overhaul/v2`)

> Gate requirement (Section 5b): Every row is `PASS`, proven by automated characterization tests run on **both** `baseline-v1` and `overhaul/v2`, including on the R8-minified release build, plus a verified in-place upgrade install (`adb install -r`) from `baseline-v1` to `overhaul/v2`.

| ID | Feature / Behavior | Old (`baseline-v1`) | New (`overhaul/v2`) | Test That Proves It | Status |
|---|---|---|---|---|---|
| **C1** | Manifest identity & security surface (`com.example.lockedsecure`, `minSdk = 24 <= 30`, zero internet permissions, exact Activity/Service/Receiver components & permissions; zero unwanted startup providers) | PASS (4 tests) | PASS (4 tests + merged manifest clean) | `ManifestContractTest` | **PASS** |
| **C2** | `PowerMenuBlockerService` power-menu detection (`GlobalActions` case-insensitive OR `com.android.systemui` + `dialog` + `global` case-insensitive; ignores other events/windows) | PASS (5 tests) | PASS (8 tests incl. pure domain tests) | `PowerMenuBlockerCharacterizationTest`, `PowerMenuActionPolicyTest` | **PASS** |
| **C3** | `PowerMenuBlockerService` lock-screen gating (`GLOBAL_ACTION_BACK` called iff `KeyguardManager.isKeyguardLocked == true`) | PASS (4 tests) | PASS (5 tests + lazy lock query verified) | `PowerMenuBlockerCharacterizationTest`, `PowerMenuActionPolicyTest` | **PASS** |
| **C4** | `PowerMenuBlockerService` null safety (`packageName == null`, `className == null`, `onInterrupt()` no-op) | PASS (2 tests) | PASS (2 tests) | `PowerMenuBlockerCharacterizationTest` | **PASS** |
| **C5** | `AdminReceiver` Device Owner uninstall blocking (`setUninstallBlocked(true)` + `"Uninstall protection activated"` Toast; catch error + `"Could not restrict uninstall: ..."` Toast; no-op when not Device Owner) | PASS (2 tests) | PASS (5 tests incl. `Failed` branch) | `AdminReceiverCharacterizationTest`, `UninstallProtectionUseCaseTest` | **PASS** |
| **C6** | `MainActivity` protection status detection on `onResume` (Accessibility Service enabled check — supporting both full & OEM short `ComponentName` — + Device Admin active check) | PASS | PASS (4 unit tests + API 30 smoke test) | `MainActivityCharacterizationTest`, `AndroidProtectionStatusReaderTest`, `scripts/verify_release_smoke.sh` | **PASS** |
| **C7** | `MainActivity` dashboard UI & visual parity (`LOCKEDSECURE`, `ANTI-THEFT SHIELD`, shield state/color, `Shield Active`/`Shield Offline`, both `StatusRow` texts, both button labels/states, matte dark palette `#0D0D11`) | PASS (`uiautomator`) | PASS (2 UI unit tests + API 30 `uiautomator`, buttons at `(540,1562)`/`(540,1731)` vs `(540,1562)`/`(540,1741)`) | `DashboardViewFactoryTest`, `MainActivityCharacterizationTest`, `scripts/verify_release_smoke.sh` | **PASS** |
| **C8** | `MainActivity` settings navigation & error fallback (`ACTION_ACCESSIBILITY_SETTINGS` + Toast fallback; `ACTION_ADD_DEVICE_ADMIN` with `AdminReceiver` & explanation + Toast fallback) | PASS (smoke tap) | PASS (exact intent/extras + `ActivityNotFoundException` tests + smoke tap) | `AndroidSettingsNavigatorTest`, `DashboardControllerTest`, `scripts/verify_release_smoke.sh` | **PASS** |
| **U1** | Upgrade install (`baseline-v1` `versionCode=1` → `overhaul/v2` `versionCode=20000` via `adb install -r`, preserving Accessibility Service enabled state & launching cleanly with 0 crashes) | `baseline-v1` installed & Accessibility enabled (`Power Blocker: Protected`) | Upgraded in-place to `2.0.0`/`20000`; `enabled_accessibility_services` preserved; UI shows `Power Blocker: Protected` & `Blocker Service Enabled`; 0 crashes | API 30 emulator upgrade-install verification | **PASS** |
| **R1** | Release (R8-minified) build passes smoke & parity checks with zero crashes in logcat | PASS | PASS (`app-release.apk` `59,581 B`, `0` crashes in logcat) | `scripts/verify_release_smoke.sh` | **PASS** |

---

## Manifest & Exported Surface Diff (`baseline-v1` → `overhaul/v2`)
- **Added:** `android:name="com.example.lockedsecure.app.LockedSecureApp"` (minimal `Application` enabling debug-only `StrictMode` and lazy `AppGraph`).
- **Removed (security & footprint win):**
  - `com.example.lockedsecure.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (unneeded AndroidX permission)
  - `androidx.startup.InitializationProvider` (3 unwanted startup initializers: `EmojiCompatInitializer`, `ProcessLifecycleInitializer`, `ProfileInstallerInitializer`)
  - `androidx.profileinstaller.ProfileInstallReceiver` (unneeded exported broadcast receiver)
  - `androidx.window.extensions` / `androidx.window.sidecar` `<uses-library>` entries
- **Unchanged:** Package ID (`com.example.lockedsecure`), `minSdk = 24`, `targetSdk = 36`, zero `<uses-permission>`, `.MainActivity` (`exported="true"`), `.PowerMenuBlockerService` (`exported="false"`, `BIND_ACCESSIBILITY_SERVICE`, `canRetrieveWindowContent="false"`), `.AdminReceiver` (`exported="true"`, `BIND_DEVICE_ADMIN`).
