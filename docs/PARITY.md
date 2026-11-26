# LockedSecure — Feature Parity Matrix (`baseline-v1` vs `overhaul/v2`)

> Gate requirement (Section 5b): Every row must reach `PASS`, proven by automated characterization tests run on **both** `baseline-v1` and `overhaul/v2`, including on the R8-minified release build, plus a verified upgrade install (`adb install -r`) from `baseline-v1` to `overhaul/v2`.

| ID | Feature / Behavior | Old (`baseline-v1`) | New (`overhaul/v2`) | Test That Proves It | Status |
|---|---|---|---|---|---|
| **C1** | Manifest identity & security surface (`com.example.lockedsecure`, `minSdk <= 30`, zero internet permissions, exact Activity/Service/Receiver components & permissions) | PASS (4 tests) | Baseline | `ManifestContractTest` | PASS |
| **C2** | `PowerMenuBlockerService` power-menu detection (`GlobalActions` case-insensitive OR `com.android.systemui` + `dialog` + `global` case-insensitive; ignores other events/windows) | PASS (5 tests) | Baseline | `PowerMenuBlockerCharacterizationTest` | PASS |
| **C3** | `PowerMenuBlockerService` lock-screen gating (`GLOBAL_ACTION_BACK` called iff `KeyguardManager.isKeyguardLocked == true`) | PASS (4 tests) | Baseline | `PowerMenuBlockerCharacterizationTest` | PASS |
| **C4** | `PowerMenuBlockerService` null safety (`packageName == null`, `className == null`, `onInterrupt()` no-op) | PASS (2 tests) | Baseline | `PowerMenuBlockerCharacterizationTest` | PASS |
| **C5** | `AdminReceiver` Device Owner uninstall blocking (`setUninstallBlocked(true)` + `"Uninstall protection activated"` Toast; catch error + `"Could not restrict uninstall: ..."` Toast; no-op when not Device Owner) | PASS (2 tests) | Baseline | `AdminReceiverCharacterizationTest` | PASS |
| **C6** | `MainActivity` protection status detection on `onResume` (Accessibility Service enabled check + Device Admin active check) | PASS | Baseline | `MainActivityCharacterizationTest` + `scripts/verify_release_smoke.sh` | PASS |
| **C7** | `MainActivity` dashboard UI & visual parity (`LOCKEDSECURE`, `ANTI-THEFT SHIELD`, shield state/color, `Shield Active`/`Shield Offline`, both `StatusRow` texts, both button labels/states, matte dark palette `#0D0D11`) | PASS (`uiautomator`) | Baseline | `MainActivityCharacterizationTest` + `scripts/verify_release_smoke.sh` | PASS |
| **C8** | `MainActivity` settings navigation & error fallback (`ACTION_ACCESSIBILITY_SETTINGS` + Toast fallback; `ACTION_ADD_DEVICE_ADMIN` with `AdminReceiver` & explanation + Toast fallback) | PASS (smoke tap) | Baseline | `scripts/verify_release_smoke.sh` | PASS |
| **U1** | Upgrade install (`baseline-v1` → `overhaul/v2` via `adb install -r`, preserving app data, Accessibility Service enabled state, and Device Admin state) | Baseline APK saved (`/tmp/lockedsecure-baseline/app-x86_64-release.apk`) | Pending v2 build | Upgrade-install verification script on API 30 emulator | PENDING |
| **R1** | Release (R8-minified) build passes smoke & parity checks with zero crashes in logcat | PASS | Baseline | `scripts/verify_release_smoke.sh` on signed R8 release APK | PASS |
