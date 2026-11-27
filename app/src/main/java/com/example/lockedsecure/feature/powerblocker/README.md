# Feature: Power Blocker (`feature.powerblocker`)

Detects system power/global-actions dialogs and decides whether they must be dismissed while the lock screen is active.

- `domain/` — Pure Kotlin contracts (`PowerMenuEvent`, `PowerMenuDetector`, `LockStateProvider`, `PowerMenuActionPolicy`, `PowerMenuDecision`) and `PowerBlockerFactory`. Default implementations are `internal`.
- `data/` — `AndroidLockStateProvider`, a lazy `KeyguardManager` adapter.
