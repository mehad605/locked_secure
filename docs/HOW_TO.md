# LockedSecure — Developer How-To Recipes

Every recipe below is tested against this repository and verified by `./gradlew spotlessCheck detekt lint test assembleRelease`.

---

## 1. Add a New Feature

Adding a self-contained feature touches at most **3 documented locations**:
1. **Create the feature package** under `app/src/main/java/com/example/lockedsecure/feature/<name>/` with `domain/` (public interfaces + pure Kotlin logic), `data/` (Android platform adapters), optional `ui/`, and a package `README.md`.
2. **Wire it in `AppGraph`** (`app/src/main/java/com/example/lockedsecure/app/AppGraph.kt`) using a lazy property or factory method.
3. **Consume its public `domain` interface** from the relevant Android entry point (`MainActivity`, `PowerMenuBlockerService`, or `AdminReceiver`) and add unit tests under `app/src/test/java/com/example/lockedsecure/feature/<name>/`.

### Step-by-Step
1. Create `app/src/main/java/com/example/lockedsecure/feature/<name>/domain/<Feature>UseCase.kt` with a public interface, an `internal` default implementation, and a public factory object (no `android.*` imports!).
2. If Android APIs are required, create `app/src/main/java/com/example/lockedsecure/feature/<name>/data/Android<Gateway>.kt` implementing a `domain` gateway interface using `context.applicationContext`.
3. Add a `val <feature>UseCase by lazy { ... }` property to `AppGraph.kt`.
4. Add `app/src/main/java/com/example/lockedsecure/feature/<name>/README.md`.
5. Add unit tests under `app/src/test/java/com/example/lockedsecure/feature/<name>/domain/<Feature>UseCaseTest.kt`.
6. Run:
   ```bash
   ./gradlew spotlessApply spotlessCheck detekt lint test assembleRelease
   ```
   `ArchitectureBoundaryTest` automatically verifies layering, isolation, cycle freedom, and size limits.

---

## 2. Add a Screen

1. Create a feature UI package `feature/<name>/ui/` with:
   - A public UI interface (mirroring `feature/dashboard/ui/DashboardUi.kt`) exposing `val rootView: View` and `fun render(state: ...)`.
   - A `ViewFactory` object creating pure Android Views (keep files <= 300 lines, functions <= 40 lines; put reusable widgets in a sibling `*Components.kt`).
2. Add user-visible copy to `app/src/main/res/values/strings.xml` (never hardcode user-facing strings).
3. Create a thin `android.app.Activity` entry point in `com.example.lockedsecure` (declared with `android:exported="false"` unless externally launchable in `AndroidManifest.xml`), obtain its controller from `LockedSecureApp.graph(this)`, attach `ui.rootView`, and clear references in `onDestroy()`.
4. Add a Robolectric UI test (mirroring `DashboardViewFactoryTest.kt`) and run `./gradlew spotlessCheck detekt lint test`.

---

## 3. Add a Setting

LockedSecure currently has zero persistent settings (all state is read directly from Android's `Settings.Secure` and `DevicePolicyManager`). To add a persistent user preference cleanly:
1. Define a pure Kotlin `SettingsRepository` interface and immutable `AppSettings` data class in `feature/<name>/domain/`.
2. Implement `SharedPreferencesSettingsRepository` in `feature/<name>/data/` backed by `context.applicationContext.getSharedPreferences(...)` (note: perform writes with `.apply()`, never `.commit()` on the main thread, so `StrictMode` remains clean!).
3. Wire `val settingsRepository: SettingsRepository by lazy { ... }` in `AppGraph.kt`.
4. Inject `SettingsRepository` into the feature's controller/use case via constructor parameter.
5. Add unit tests using a fake in-memory `SettingsRepository` for domain logic plus a Robolectric test for `SharedPreferencesSettingsRepository`.

---

## 4. Replace the Network / Platform Data Layer

> **Important:** LockedSecure is **offline-first by design** — it declares zero internet permissions and has no network layer (`ManifestContractTest` enforces zero `INTERNET` / `ACCESS_NETWORK_STATE` permissions).

To replace **any** platform data layer (e.g., swapping `AndroidProtectionStatusReader`, `AndroidLockStateProvider`, or `AndroidDevicePolicyGateway` with an alternative implementation or a mock/OEM-specific adapter):
1. Implement the feature's public `domain` interface (e.g., `ProtectionStatusReader`, `LockStateProvider`, `DevicePolicyGateway`) in `feature/<name>/data/`.
2. Swap the single constructor call inside `app/src/main/java/com/example/lockedsecure/app/AppGraph.kt`.
3. Zero changes are required in `domain/`, `ui/`, `MainActivity`, `PowerMenuBlockerService`, or `AdminReceiver`.
4. Run `./gradlew spotlessCheck detekt lint test` to confirm all domain, architecture, and characterization tests pass unchanged.

---

## 5. Add a Dependency

Because LockedSecure targets minimal footprint (`<= 150 KB` release APK budget on low-RAM devices), every new dependency must earn its place:
1. First verify no Android SDK API (`minSdk = 24`) can solve the problem directly.
2. Add the version and library coordinate to `gradle/libs.versions.toml` (never hardcode versions in `app/build.gradle.kts`).
3. Declare it in `app/build.gradle.kts` (`testImplementation` for test-only libs; avoid runtime `implementation` unless strictly necessary).
4. Document the library in `docs/DEPENDENCIES.md`: **why it exists, why a platform API cannot replace it, and its measured release APK size delta**.
5. Run `./gradlew assembleRelease` and check `apkanalyzer apk file-size app/build/outputs/apk/release/app-release.apk` against the `150 KB` release budget, and inspect the merged manifest (`app/build/intermediates/merged_manifest/release/.../AndroidManifest.xml`) to confirm no unwanted permissions, services, or `androidx.startup` ContentProviders were pulled in.
