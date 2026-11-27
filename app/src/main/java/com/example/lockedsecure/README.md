# Root Entry Points (`com.example.lockedsecure`)

Contains only the three thin Android manifest entry points required by the OS:

- `MainActivity.kt` — Launcher `Activity` hosting `DashboardUi` and delegating actions to `DashboardController`.
- `PowerMenuBlockerService.kt` — `AccessibilityService` forwarding `TYPE_WINDOW_STATE_CHANGED` events to `PowerMenuActionPolicy`.
- `AdminReceiver.kt` — `DeviceAdminReceiver` delegating `onEnabled` to `UninstallProtectionUseCase`.

These three classes remain in the root package so Android preserves Accessibility Service and Device Admin registration across app upgrades from `baseline-v1`.
