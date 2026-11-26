# LockedSecure — Scorecard

> Scoring rule: Each sector scores `floor(passed / total * 10)`. A sector scores `10/10` **only** when every criterion passes with verified evidence.
> Last updated: Phase 1 Baseline (`baseline-v1` / `ac50c14`)

## Summary

| Sector | Score | Passed / Total | Status |
|---|---:|---:|---|
| **S1.** APK and installed size | **1 / 10** | 1 / 9 | Failing |
| **S2.** Runtime footprint | **4 / 10** | 4 / 9 | Failing |
| **S3.** Code structure & maintainability | **1 / 10** | 2 / 11 | Failing |
| **S4.** Readability & onboarding | **1 / 10** | 1 / 9 | Failing |
| **S5.** Extensibility & replaceability | **0 / 10** | 0 / 5 | Failing |
| **S6.** Release pipeline security | **3 / 10** | 4 / 13 | Failing |
| **S7.** Release pipeline reliability & maintainability | **1 / 10** | 2 / 11 | Failing |
| **S8.** Correctness & regression safety *(GATE)* | **10 / 10** | 5 / 5 | Passing (Phase 1 safety net active) |

---

## Detailed Criteria Breakdown

### S1. APK and installed size — `1 / 10` (1/9)
- [ ] R8 full mode, minify, shrinkResources on with verified keep rules (no blanket `-keep`) — **FAIL**: `app/proguard-rules.pro:9` has `-keep class androidx.compose.** { *; }`.
- [ ] Zero unused dependencies — **FAIL**: Navigation3 (3 libs), `kotlin-serialization`, `core-ktx`, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `lifecycle-runtime-ktx` unused; 213 resolved artifacts.
- [ ] Every remaining dependency justified in `docs/DEPENDENCIES.md` — **FAIL**: file does not exist.
- [ ] Unused resources/locales/densities/fonts/assets removed; `resourceConfigurations` set — **FAIL**: `backup_rules.xml` & `data_extraction_rules.xml` unused; `resourceConfigurations` unset (`resources.arsc` is 440 KB).
- [ ] Native libs: only needed ABIs, stripped, `extractNativeLibs=false` — **FAIL**: `libandroidx.graphics.path.so` failed stripping (`stripReleaseDebugSymbols` warning).
- [x] Images are WebP/vector; no oversized assets — **PASS**: launcher icons are vector XML + WebP (`res/*` total 35 KB).
- [ ] No debug/log/analytics code in release — **FAIL**: `PowerMenuBlockerService.kt:18,30,32` logs in release; `DebugProbesKt.bin` packaged in release APK.
- [ ] Baseline profile present (if startup-relevant) — **FAIL**: only transitive library profile, not app-tuned (or N/A once zero-dependency native Views evaluated).
- [ ] Final size reported against baseline with per-component breakdown showing nothing removable remains — **FAIL**: baseline is 4.15 MB (91.6% kept Compose bloat).

### S2. Runtime footprint — `4 / 10` (4/9)
- [x] `Application.onCreate` does only what is strictly required — **PASS**: no custom `Application` work.
- [ ] No `ContentProvider`/initializer at startup without justification — **FAIL**: merged manifest runs `androidx.startup.InitializationProvider` with `EmojiCompatInitializer`, `ProcessLifecycleInitializer`, `ProfileInstallerInitializer`.
- [ ] No main-thread I/O or heavy work (StrictMode clean in debug) — **FAIL**: StrictMode not configured/verified; `Settings.Secure` / Compose init on main thread.
- [ ] No leaks (LeakCanary or heap-dump comparison over 30-min run) — **FAIL**: not yet verified.
- [x] No busy loops, polling, or oversized thread pools — **PASS**: event-driven AccessibilityService + static Activity.
- [x] Idle state: no running service/wakelock/periodic job unless required by core feature — **PASS**: only OS-bound AccessibilityService when enabled; 0 jobs, 0 alarms, 0 app wakelocks (`docs/BASELINE.md`).
- [x] Background work uses WorkManager or is event-driven; no exact alarms — **PASS**: purely event-driven (`AccessibilityEvent`), 0 alarms.
- [ ] Caches bounded; disk growth bounded; logs rotated/removed — **FAIL**: release `Log.d`/`Log.i` fires on every window state change.
- [ ] Measured idle RAM, idle CPU, and cold start improved vs. baseline with floor explained — **FAIL**: baseline not yet optimized (`32.8 MB` idle PSS, `233 ms` cold start).

### S3. Code structure and maintainability — `1 / 10` (2/11)
- [ ] No file >300 lines and no function >40 lines — **FAIL**: `MainActivity.kt` is 311 lines; `MainScreen` is 187 lines.
- [ ] No god classes (single clear responsibility) — **FAIL**: `MainActivity` mixes UI, system status queries, and Settings intent launching.
- [ ] Clear layering (`ui`, `domain`, `data`) with automated dependency direction enforcement — **FAIL**: flat package, no architecture tests.
- [x] No circular dependencies; no global mutable state; no static Context holders — **PASS**: verified in `docs/BASELINE.md`.
- [ ] Explicit constructor DI — **FAIL**: direct framework instantiation inside Activity/Service/Receiver.
- [ ] Consistent naming and package-by-feature layout — **FAIL**: flat root package + `theme/`.
- [ ] Zero swallowed exceptions; errors modeled and handled deliberately — **FAIL**: 3 raw `catch (e: Exception)` blocks without typed error modeling.
- [ ] Zero dead code, commented-out code, unaddressed TODOs — **FAIL**: 16 lines commented code in `theme/Type.kt`, redundant `globalactions` check in `PowerMenuBlockerService.kt:22`, TODO in `data_extraction_rules.xml`, unused XML files.
- [x] Duplication below 3% — **PASS**: `jscpd` measured `1.76%` (1 clone, 8 lines).
- [ ] detekt/ktlint/lint: zero errors, zero warnings — **FAIL**: 15 Android Lint warnings; detekt/ktlint not configured.
- [ ] Formatting enforced automatically in CI — **FAIL**: not configured.

### S4. Readability and onboarding — `1 / 10` (1/9)
- [x] README covers what app is, features, build, run, test, release — **PASS** (minor update needed for tests/architecture later).
- [ ] `docs/ARCHITECTURE.md` — **FAIL**: missing.
- [ ] `docs/adr/` ADRs — **FAIL**: missing.
- [ ] `CONTRIBUTING.md` covers setup, code style, branch/PR flow, how to run checks — **FAIL**: missing check/lint/format commands and updated conventions.
- [ ] Every module and package has a short README or package doc — **FAIL**: missing.
- [ ] Every public class/function has KDoc — **FAIL**: 0% KDoc coverage.
- [ ] Complex logic has WHY comments — **FAIL**: sparse/casual `ponytail:` comments instead of clear technical WHY comments.
- [ ] `docs/HOW_TO.md` with tested step-by-step recipes — **FAIL**: missing.
- [ ] Cold-read test (fresh subagent scores 10/10) — **FAIL**: not yet run.

### S5. Extensibility and replaceability — `0 / 10` (0/5)
- [ ] Self-contained features with public interfaces and hidden internals — **FAIL**.
- [ ] Cross-feature communication only through defined interfaces — **FAIL**.
- [ ] Replaceability proof (2 components rewritten behind interfaces) — **FAIL**.
- [ ] Extension proof (follow `docs/HOW_TO.md` end-to-end with dummy feature) — **FAIL**.
- [ ] Documented small set of touch points for adding a feature — **FAIL**.

### S6. Release pipeline security — `3 / 10` (4/13)
- [x] Every third-party action pinned to full commit SHA with version comment — **PASS** (`release.yml` pins all 5 actions).
- [x] Top-level `permissions: {}` or `contents: read`; write scopes per-job only — **PASS** (`contents: read` at top, write on `release` job).
- [x] No `pull_request_target` or unsafe `workflow_run` — **PASS**.
- [ ] No script injection (`${{ ... }}` in `run:`) — **FAIL**: `zizmor` reports 4–6 `template-injection` findings (`release.yml:78,85,115-118`).
- [ ] Signing keys scoped to protected GitHub Environment, decoded to temp file & deleted after use — **FAIL**: repo-level secrets, no `environment:`, decoded `signing-key.jks` never deleted.
- [ ] Releases triggered only from protected tags/branches — **FAIL**: `workflow_dispatch` accepts arbitrary unvalidated `version_number` from any branch.
- [ ] Gradle wrapper validation step — **FAIL**: missing.
- [ ] Build provenance attestation AND SBOM — **FAIL**: provenance present, SBOM missing.
- [x] SHA-256 checksums published beside artifacts — **PASS** (`SHA256SUMS.txt` generated & uploaded).
- [ ] Dependabot or Renovate configured — **FAIL**: missing.
- [ ] Secret scanning and dependency review enabled — **FAIL**: no dependency-review workflow.
- [ ] `actionlint` and `zizmor` report zero findings — **FAIL**: `zizmor` reports 9 default / 12 pedantic findings.
- [ ] Documented recommended repo settings checklist — **FAIL**: missing.

### S7. Release pipeline reliability and maintainability — `1 / 10` (2/11)
- [ ] Deterministic build (pinned JDK/Gradle/AGP; same tag gives same artifact) — **FAIL**: `VERSION_CODE=$((100 + github.run_number))` changes on every run!
- [ ] Version name and code derived from tag / single source of truth — **FAIL**: `versionCode` uses `github.run_number`, not tag.
- [x] Pipeline runs lint, tests, and release build before publishing — **PASS** (`validate` job runs `lint` + `test` before `release`).
- [ ] Built APK verified (`apksigner verify`, size budget, version-vs-tag check) — **FAIL**: none of these exist.
- [ ] Fail fast with `timeout-minutes` and safe `concurrency` — **FAIL**: `cancel-in-progress: true` can abort an active release!
- [ ] Gradle & dependency caching configured safely — **FAIL**: `zizmor` flags `cache-poisoning` on `release.yml:37,69`.
- [x] Release notes/changelog generated automatically — **PASS** (`generate_release_notes: true`).
- [ ] Release is idempotent and re-runnable (no half-published release) — **FAIL**: publishes directly as `draft: false`.
- [ ] No duplicated logic (composite actions / reusable workflows) — **FAIL**: checkout/JDK/Gradle setup duplicated across jobs.
- [ ] Documented in `docs/RELEASING.md` (key rotation, dry run) — **FAIL**: missing.
- [ ] Dry-run tested with run links/logs as evidence — **FAIL**: not yet run.

### S8. Correctness and regression safety *(GATE)* — `10 / 10` (5/5)
- [x] All characterization & core-contract smoke tests pass on release (R8-minified) build — **PASS**: 15 JVM characterization tests pass (`./gradlew test`) + `scripts/verify_release_smoke.sh` passes on signed R8 release APK.
- [x] `./gradlew lint test assembleRelease` passes — **PASS**: verified clean exit code 0.
- [x] No new crashes in logcat during core flows on release build — **PASS**: verified zero `AndroidRuntime:E` entries for `com.example.lockedsecure`.
- [x] 100% coverage of core contract; new code has tests — **PASS**: all 8 core contract items (C1–C8) covered by characterization + release smoke tests.
- [x] Feature parity with `baseline-v1` proven in `docs/PARITY.md` — **PASS**: baseline parity established before any production edits (`docs/PARITY.md`).
