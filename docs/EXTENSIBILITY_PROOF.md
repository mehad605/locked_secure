# LockedSecure — Extensibility & Replaceability Proofs (S5)

All proofs below were executed on temporary throwaway branches created from `overhaul/v2` (`02b9efe`), verified with `./gradlew spotlessApply spotlessCheck detekt lint test assembleRelease`, and cleaned up afterward.

---

## 1. Replaceability Proof (2 Components Rewritten Behind Interfaces)

On throwaway branch `proof/replaceability`, two production components were rewritten from scratch behind their public `domain` interfaces:

1. **Data Layer (`ProtectionStatusReader`):** Created `AccessibilityManagerStatusReader` (`feature/dashboard/data/`), which queries `AccessibilityManager.getEnabledAccessibilityServiceList()` first with a `Settings.Secure` fallback instead of `AndroidProtectionStatusReader`'s direct `Settings.Secure` parser. Wired by changing 1 constructor call in `app/AppGraph.kt`.
2. **Feature Logic (`PowerMenuDetector`):** Created `RegexPowerMenuDetector` (`feature/powerblocker/domain/`), replacing substring matching with compiled `Regex` patterns. Wired by changing 1 default parameter in `PowerBlockerFactory`.

### Files Touched Outside New Implementations
- `app/src/main/java/com/example/lockedsecure/app/AppGraph.kt` (1 line)
- `app/src/main/java/com/example/lockedsecure/feature/powerblocker/domain/PowerMenuActionPolicy.kt` (1 line)

### Result (**VERIFIED**)
- `MainActivity`, `PowerMenuBlockerService`, `AdminReceiver`, `DashboardController`, `DashboardUi`, and **all 35 existing tests** (including all characterization and architecture boundary tests) required **zero modifications** and passed `./gradlew spotlessCheck detekt lint test assembleRelease` (`BUILD SUCCESSFUL`).

---

## 2. Extension Proof (Following `docs/HOW_TO.md` Section 1 Verbatim)

On throwaway branch `proof/extension-dummy`, the exact recipe in `docs/HOW_TO.md` ("Add a New Feature") was executed word-for-word to add a trivial dummy feature `feature/shieldsummary`:

1. Created `app/src/main/java/com/example/lockedsecure/feature/shieldsummary/domain/ShieldSummaryUseCase.kt` (public `fun interface`, `internal` default implementation, public `ShieldSummaryFactory`).
2. Created `app/src/main/java/com/example/lockedsecure/feature/shieldsummary/README.md`.
3. Wired `val shieldSummaryUseCase: ShieldSummaryUseCase by lazy { ShieldSummaryFactory.create() }` in `app/AppGraph.kt`.
4. Added unit test `app/src/test/java/com/example/lockedsecure/feature/shieldsummary/domain/ShieldSummaryUseCaseTest.kt`.
5. Ran `./gradlew spotlessApply spotlessCheck detekt lint test assembleRelease`.

### Result (**VERIFIED**)
- Every check passed on the first run (`BUILD SUCCESSFUL in 12s`), including `ArchitectureBoundaryTest`, `detekt`, `spotlessCheck`, and 0-warning Android `lint`.
- Total existing files modified: **1** (`AppGraph.kt`). No documentation fixes were needed; the throwaway branch was removed cleanly.

---

## 3. Documented Touch Points for Adding a Feature

As documented in [docs/HOW_TO.md](HOW_TO.md#1-add-a-new-feature), adding a feature touches at most **3 places**:
1. New package under `feature/<name>/` (`domain/`, optional `data/`/`ui/`, and `README.md`)
2. One lazy wiring property/factory in `app/AppGraph.kt`
3. Consuming the feature's `domain` interface from the relevant entry point (`MainActivity`, `PowerMenuBlockerService`, or `AdminReceiver`) + tests under `app/src/test/.../feature/<name>/`
