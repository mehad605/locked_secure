# LockedSecure

<p align="center">
  <img src="logo.svg" width="128" height="128" alt="LockedSecure Logo" />
</p>

<p align="center">
  <strong>Ultra-lightweight anti-theft shield for Android — blocks the power menu while your screen is locked.</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#build--verify">Build &amp; Verify</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#release">Release</a> •
  <a href="#license">License</a>
</p>

---

LockedSecure (`com.example.lockedsecure`) is a minimal, offline-only Android security utility that prevents unauthorized device shutdown while the lock screen is active. It intercepts the system power dialog via a least-privilege `AccessibilityService` (`canRetrieveWindowContent="false"`) and reinforces uninstall deterrence via a `DeviceAdminReceiver`.

Built strictly on Android platform APIs (`minSdk = 24`, zero third-party runtime dependencies), the signed universal release APK is **~59 KB**, runs zero startup `ContentProvider`s, schedules zero background jobs or alarms, and declares zero internet permissions.

## Features

- **Power Menu Blocking** — Dismisses the system power/global-actions dialog (`GLOBAL_ACTION_BACK`) whenever the keyguard is locked.
- **Uninstall Protection** — Registers as a Device Administrator; when provisioned as Device Owner, activates `setUninstallBlocked(true)`.
- **Offline by Design** — Zero network permissions, zero telemetry, zero external dependencies.
- **Minimal Matte-Dark Dashboard** — Single edge-to-edge screen displaying live protection status and direct shortcuts to system settings.
- **Universal ~59 KB APK** — Zero native `.so` libraries; one tiny signed APK works on all CPU architectures (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).

## Prerequisites

- **JDK 17** (Temurin recommended; Gradle toolchain resolver included)
- **Android SDK** with `platforms;android-36` and `build-tools;36.0.0`
- **Gradle 9.1.0** (wrapper included, SHA-256 verified)

## Installation

### Sideload (pre-built universal APK)

Download the latest signed universal APK and `SHA256SUMS.txt` from [GitHub Releases](https://github.com/mehad605/locked_secure/releases):

```bash
sha256sum -c SHA256SUMS.txt
adb install locked-secure-v2.0.0-universal.apk
```

### Build from source

```bash
git clone https://github.com/mehad605/locked_secure.git
cd locked_secure
./gradlew assembleRelease
```

The release APK is written to `app/build/outputs/apk/release/app-release.apk` (or `app-release-unsigned.apk` if no signing config is provided).

## Usage

1. Open **LockedSecure**.
2. Tap **Enable Power Blocker** and turn on LockedSecure in Android Accessibility settings.
3. Tap **Enable Admin Protection** and activate LockedSecure as a Device Administrator.

> **Android 13+ note:** If the Accessibility toggle is greyed out after sideloading, open **Settings → Apps → LockedSecure → ⋮ → Allow restricted settings**, then return to Accessibility settings.

### Optional: Device Owner mode (blocks ADB uninstall)

```bash
adb shell dpm set-device-owner com.example.lockedsecure/.AdminReceiver
```

## Build & Verify

### Run debug build

```bash
./gradlew assembleDebug
```

### Run full quality & test suite

```bash
./gradlew spotlessCheck detekt lint test assembleRelease
```

### Auto-format Kotlin & Gradle files

```bash
./gradlew spotlessApply
```

### Signed local release build

Create `app/keystore.properties` (git-ignored) or export the `ANDROID_SIGNING_*` variables documented in [.env.example](.env.example):

```properties
storeFile=../release-key.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

Then run `./gradlew assembleRelease`.

### Release smoke test (requires connected Android device or emulator)

```bash
bash scripts/verify_release_smoke.sh app/build/outputs/apk/release/app-release.apk
```

## Architecture

LockedSecure uses a clean **package-by-feature** layout (`ui` → `domain` ← `data`) with lazy manual DI (`AppGraph`) and automated boundary enforcement (`ArchitectureBoundaryTest`):

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Architecture diagram, package map, and data flows
- [docs/HOW_TO.md](docs/HOW_TO.md) — Tested recipes for adding features, screens, settings, or replacing layers
- [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md) — Complete dependency & APK byte breakdown
- [docs/adr/](docs/adr/) — Architecture Decision Records
- [CONTRIBUTING.md](CONTRIBUTING.md) — Contribution workflow and style rules

## Release

Automated releases are built and published via [.github/workflows/release.yml](.github/workflows/release.yml) from signed SemVer tags (`vMAJOR.MINOR.PATCH`). See [docs/RELEASING.md](docs/RELEASING.md) for the full release, dry-run, and key-rotation runbook, and [docs/REPO_SETTINGS.md](docs/REPO_SETTINGS.md) for recommended GitHub repository protections.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (JVM 17 toolchain) |
| UI | Pure Android Framework Views (`minSdk = 24`, `targetSdk = 36`) |
| Architecture | Package-by-feature Clean Architecture + manual lazy DI (`AppGraph`) |
| Build | Gradle 9.1.0 + AGP 9.0.1 + R8 full mode |
| Quality Gates | Spotless (ktlint), Detekt, Android Lint (`warningsAsErrors`), JUnit 4 + Robolectric |
| CI/CD | GitHub Actions with SHA-pinned actions, SBOM, SLSA provenance, SHA-256 checksums |

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
