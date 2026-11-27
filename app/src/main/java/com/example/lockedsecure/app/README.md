# Composition Root (`com.example.lockedsecure.app`)

Application lifecycle and explicit manual dependency injection:

- `LockedSecureApp.kt` — Minimal `Application` class. Performs zero work in release `onCreate()`; enables `StrictMode` (thread + VM policies) in debug builds only.
- `AppGraph.kt` — Lazy manual DI container wiring all feature `domain` interfaces to their `data` implementations without Hilt/Koin runtime overhead.
