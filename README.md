# LockedSecure

<p align="center">
  <img src="logo.svg" width="128" height="128" alt="LockedSecure Logo" />
</p>

<p align="center">
  <strong>Anti-theft shield for Android — blocks the power menu when your device is locked.</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#prerequisites">Prerequisites</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#build">Build</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#license">License</a>
</p>

---

LockedSecure is a minimal Android security utility that prevents unauthorized device shutdown while the screen is locked. By intercepting the system power dialog via an Accessibility Service and enforcing Device Admin policies, it ensures your device stays powered on and trackable if lost or stolen. No internet permissions required — everything runs offline.

## Features

- **Power Menu Blocking** — Automatically dismisses the shutdown dialog when the keyguard is locked.
- **Uninstall Protection** — Device Owner mode can block ADB uninstallation.
- **Offline First** — Zero internet permissions; no data leaves your device.
- **Minimal UI** — Single-screen dashboard with real-time protection status.
- **Split APK Support** — Architecture-specific builds for smaller download sizes.

## Prerequisites

- **Android Studio** Ladybug (2024.2.1+) or later
- **JDK 17** (Temurin recommended)
- **Android SDK** with `platforms;36` and `build-tools;36+`
- **Gradle 9.1** (wrapper included)

## Installation

### Sideload (pre-built APKs)

Download the latest split APK for your device architecture from the [Releases](https://github.com/mehad605/locked_secure/releases) page:

```bash
adb install locked-secure-v1.0.0-arm64-v8a.apk
```

### Build from source

```bash
git clone https://github.com/mehad605/locked_secure.git
cd locked_secure
./gradlew assembleRelease
```

APKs will be in `app/build/outputs/apk/release/`.

## Usage

1. Open **LockedSecure** on your device.
2. Tap **Enable Power Blocker** to activate the Accessibility Service.
3. Tap **Enable Admin Protection** to activate Device Administrator.

> **Note for Android 13+:** If the Accessibility toggle appears disabled, go to **Settings > Apps > LockedSecure > three-dot menu > Allow restricted settings**, then retry.

### Set as Device Owner (optional, blocks ADB uninstall)

```bash
adb shell dpm set-device-owner com.example.lockedsecure/.AdminReceiver
```

## Build

### Debug

```bash
./gradlew assembleDebug
```

### Release (signed)

Create `app/keystore.properties`:

```properties
storeFile=../release-key.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

Then:

```bash
./gradlew assembleRelease
```

### CI/CD

The included GitHub Actions workflow (`.github/workflows/release.yml`) automatically builds and publishes signed split APKs when a tag matching `v*` is pushed. Configure the following repository secrets:

| Secret | Description |
|---|---|
| `SIGNING_KEY` | Base64-encoded `.jks` keystore |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Key alias |
| `SIGNING_KEY_PASSWORD` | Key password |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Single-activity, Compose-driven |
| Build | Gradle 9.1 with Kotlin DSL |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |
| CI/CD | GitHub Actions |
| Signing | APK Signature Scheme V2/V3 |

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.

---

<p align="center">
  Made with ❌ internet permissions — LockedSecure is offline by design.
</p>
