# ADR-0001: Incremental Strangler Migration & Pure Android Framework Views

- **Status:** Accepted (approved by user after Phase 1 baseline review)
- **Date:** 2026-11-27

## Context

LockedSecure (`baseline-v1`) is a 454-line, single-screen Android anti-theft utility targeting resource-constrained devices (Realme Narzo 20, Helio G85, 4 GB RAM, Android 11 / Realme UI 2.0, `minSdk = 24`).

Phase 1 baseline measurements (`docs/BASELINE.md`) showed:
- Release APK (arm64-v8a): `4,148,087` bytes (`3.96 MiB`), `67,865` DEX method refs across 2 DEX files (`9,061` classes)
- `91.6%` of DEX bytecode (`5.93 MB` uncompressed) is `androidx.compose.*` + Material 3
- `213` resolved runtime artifacts, `1` unstripped native `.so` (`libandroidx.graphics.path.so`), and `3` `androidx.startup` ContentProvider initializers executed on every process start
- Idle RAM (API 30 emulator): `32,822 KB` PSS; Cold start median: `233.0 ms`

Meanwhile, the entire UI consists of one static scrollable matte-dark screen: two header labels, one rounded card containing a 1-path cubic-Bézier shield outline, one status title, two status rows, and two rounded action buttons.

## Decision

1. **Migration strategy:** Use an **incremental strangler** approach on `overhaul/v2` (not a full from-scratch rewrite). Extract `powerblocker`, `deviceadmin`, and `dashboard` behind clean `domain`/`data`/`ui` boundaries one commit at a time while keeping the manifest entry points (`MainActivity`, `PowerMenuBlockerService`, `AdminReceiver`) and package name (`com.example.lockedsecure`) unchanged for seamless upgrade installs.
2. **UI toolkit:** Replace Jetpack Compose + Material 3 + AndroidX runtime dependencies with **pure Android framework Views** (`android.app.Activity`, `ScrollView`, `LinearLayout`, `TextView`, `Button`, custom `ShieldView` using `android.graphics.Canvas`/`Path`, `GradientDrawable`/`RippleDrawable`, and platform `WindowInsets` for edge-to-edge on API 24–36).
3. **APK packaging:** Once zero native `.so` libraries remain in the APK, publish a **single universal APK** rather than four identical ABI-split APKs.

## Consequences

### Positive
- Eliminates all third-party runtime dependencies (213 → 0 external libraries beyond R8-shrunk Kotlin stdlib).
- Eliminates `androidx.startup.InitializationProvider` startup overhead and `libandroidx.graphics.path.so` native library.
- Dramatically reduces APK size (~98%+), DEX count (~99%), cold start time, and idle RAM on 4 GB OEM devices.
- Single universal APK simplifies sideloading and eliminates ABI mismatch errors.
- Preserves 100% visual and behavioral parity with `baseline-v1` (verified via characterization tests and API 30 release smoke tests).

### Negative / Tradeoffs
- Future screens are built with Android Views (or a separate UI implementation behind `DashboardUi` if Compose is ever desired for complex multi-screen flows).
- Edge-to-edge padding and rounded button ripples are handled directly via standard platform APIs (`View.setOnApplyWindowInsetsListener`, `GradientDrawable`, `RippleDrawable`).
