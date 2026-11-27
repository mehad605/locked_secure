# Feature: Device Admin (`feature.deviceadmin`)

Enforces uninstall protection when LockedSecure is activated as a Device Administrator / Device Owner.

- `domain/` — Pure Kotlin contracts (`DevicePolicyGateway`, `UninstallProtectionUseCase`, typed `UninstallProtectionResult`) and `DeviceAdminFactory`. Default implementation is `internal`.
- `data/` — `AndroidDevicePolicyGateway`, wrapping `DevicePolicyManager.isDeviceOwnerApp` and `setUninstallBlocked`.
