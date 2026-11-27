# LockedSecure — Dependencies & APK Size Justification

> Every runtime, test, and build dependency in LockedSecure is accounted for below.
> Last verified: `overhaul/v2` (`0eecbf1`), universal release APK `59,281` bytes (`52,262` B download).

---

## 1. Runtime Dependencies (`releaseRuntimeClasspath`)

LockedSecure has **zero third-party runtime dependencies**. Only the Kotlin standard library (automatically added by the Kotlin compiler plugin and shrunk by R8 full mode) appears on `releaseRuntimeClasspath`:

| Artifact | Scope | Why It Exists | Why Platform API Can't Replace It | Release APK Cost |
|---|---|---|---|---:|
| `org.jetbrains.kotlin:kotlin-stdlib` | Implicit (`implementation`) | Kotlin language intrinsics (`Intrinsics`, `Lazy`, collection helpers) | Required by Kotlin bytecode | Shrunk by R8 to minimal used helpers inside `12,738 B` compressed `classes.dex` |
| `org.jetbrains:annotations` | Transitive of `kotlin-stdlib` | Compile-time nullability annotations (`@NotNull`/`@Nullable`) | Compile-time metadata only | `0 B` (stripped by R8) |

### Removed Dependencies (vs. `baseline-v1`: 213 → 2 resolved artifacts)
- `androidx.compose.*` (`bom`, `ui`, `material3`, `foundation`, `runtime`, `ui-tooling-preview`): Replaced by pure Android framework Views (`DashboardViewFactory`, `ShieldView`) — eliminated `5.93 MB` of uncompressed DEX, `libandroidx.graphics.path.so`, and `androidx.startup` overhead.
- `androidx.navigation3:*` & `lifecycle-viewmodel-navigation3`: Unused template residue.
- `androidx.activity:activity-compose`, `androidx.lifecycle:*`, `androidx.core:core-ktx`: Unused once `MainActivity` extends platform `android.app.Activity`.
- `org.jetbrains.kotlin.plugin.serialization`: Unused compiler plugin.

---

## 2. Test-Only Dependencies (0 Bytes in Release APK)

| Artifact | Scope | Why It Exists |
|---|---|---|
| `junit:junit:4.13.2` | `testImplementation` | Standard JVM unit test runner for domain and architecture tests |
| `org.robolectric:robolectric:4.14.1` | `testImplementation` | Fast JVM simulation of Android SDK 30 (`KeyguardManager`, `DevicePolicyManager`, `Settings.Secure`, `Activity`) for characterization tests in CI without requiring an emulator |
| `androidx.test:core:1.7.0` | `testImplementation` | `ApplicationProvider.getApplicationContext()` helper for Robolectric tests |

---

## 3. Build & Static Analysis Plugins (0 Bytes in Release APK)

| Plugin | Version | Purpose |
|---|---|---|
| `com.android.application` (AGP) | `9.0.1` | Android compilation, R8 full-mode shrinking, resource shrinking, signing |
| `io.gitlab.arturbosch.detekt` | `1.23.8` | Static analysis (complexity, function length, exception handling, unused code) |
| `com.diffplug.spotless` + `ktlint` | `7.0.2` / `1.5.0` | Automated Kotlin & Gradle formatting enforced in CI |

---

## 4. Per-Component Release APK Breakdown (`59,281` bytes)

Verified via `apkanalyzer` and ZIP entry inspection on `app/build/outputs/apk/release/app-release.apk`:

| Component | Compressed (bytes) | Raw (bytes) | Removable Without Losing a Core Feature? |
|---|---:|---:|---|
| `classes.dex` (46 classes, 111 methods, 279 refs) | `12,738` | `26,556` | No — R8 full-mode shrunk app bytecode (3 entry points + 3 features + DI) |
| Legacy WebP launcher icons (`mipmap-{mdpi..xxxhdpi}/*.webp`, 10 files) | `33,294` | `33,294` | No — required for crisp launcher icons on `minSdk = 24..25` (API 26+ uses adaptive XML) |
| `resources.arsc` (`localeFilters = ["en"]`) | `4,000` | `4,000` | No — compiled string table, theme, and resource IDs |
| XML resources (adaptive icon vectors, `accessibility_service_config.xml`, `device_admin.xml`, 6 files) | `1,775` | `3,896` | No — required by launcher, `AccessibilityService`, and `DeviceAdminReceiver` |
| `AndroidManifest.xml` | `1,222` | `3,872` | No — required by Android OS |
| `META-INF/com/android/build/gradle/app-metadata.properties` | `51` | `56` | AGP standard metadata (`51 B`) |
| APK Signature Block (v2/v3) & ZIP central directory | `~6,201` | — | No — required by Android package installer |
| **Total** | **`59,281`** | — | **Nothing remaining is removable without losing a core feature or `minSdk = 24` support.** |

### Baseline Profile Note (S1)
A Compose/ART Baseline Profile (`assets/dexopt/baseline.prof`) is **not needed** because `classes.dex` is only `26.5 KB` (`46` classes, `111` methods — `99.6%` smaller than `baseline-v1`). Adding `androidx.profileinstaller` would re-introduce `androidx.startup.InitializationProvider`, an exported broadcast receiver, and ~40 KB of DEX/profile overhead larger than the entire app's bytecode while providing negligible startup benefit.
