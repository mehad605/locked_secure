# Feature: Dashboard (`feature.dashboard`)

Single-screen matte-dark status dashboard and settings launcher.

- `domain/` — Pure Kotlin `ProtectionStatus`, `ProtectionStatusReader`, `SettingsNavigator` (with typed `NavigationResult`), and `DashboardController`.
- `data/` — `AndroidProtectionStatusReader` (parses full & short `ComponentName` entries from `Settings.Secure` + queries `DevicePolicyManager`) and `AndroidSettingsNavigator`.
- `ui/` — Public `DashboardUi` boundary plus pure Android framework View implementation (`DashboardViewFactory`, `DashboardComponents`, `ShieldView`, `DashboardTheme`).
