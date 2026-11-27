# LockedSecure — Overhaul Final Report (`baseline-v1` → `overhaul/v2`)

> Branch: `overhaul/v2` (branched from `main` tagged `baseline-v1` at `ac50c14`)
> Date: 2026-11-27
> Verification environment: Linux x86_64, JDK 17 toolchain, Gradle 9.1.0, AGP 9.0.1, Android SDK 36, local API 30 (`warden_api30`) x86_64 emulator, and live GitHub Actions (`ubuntu-latest`).

---

## 1. Scorecard Summary

| Sector | Baseline | Final | Evidence |
|---|---:|---:|---|
| **S1.** APK & installed size | `1 / 10` | **10 / 10** | [SCORECARD.md](SCORECARD.md#s1-apk-and-installed-size--10--10-99), [DEPENDENCIES.md](DEPENDENCIES.md) |
| **S2.** Runtime footprint | `4 / 10` | **10 / 10** | [SCORECARD.md](SCORECARD.md#s2-runtime-footprint--10--10-99), API 30 emulator benchmarks |
| **S3.** Code structure & maintainability | `1 / 10` | **10 / 10** | [SCORECARD.md](SCORECARD.md#s3-code-structure-and-maintainability--10--10-1111), `ArchitectureBoundaryTest`, Detekt/Spotless/Lint |
| **S4.** Readability & onboarding | `1 / 10` | **10 / 10** | [ARCHITECTURE.md](ARCHITECTURE.md), [HOW_TO.md](HOW_TO.md), [COLD_READ_TEST.md](COLD_READ_TEST.md) (10/10) |
| **S5.** Extensibility & replaceability | `0 / 10` | **10 / 10** | [EXTENSIBILITY_PROOF.md](EXTENSIBILITY_PROOF.md) |
| **S6.** Release pipeline security | `3 / 10` | **10 / 10** | [release.yml](../.github/workflows/release.yml), [ci.yml](../.github/workflows/ci.yml), [REPO_SETTINGS.md](REPO_SETTINGS.md), `actionlint` 0 / `zizmor` 0 |
| **S7.** Release pipeline reliability & maintainability | `1 / 10` | **10 / 10** | [RELEASING.md](RELEASING.md), [Dry-Run #45917574738](https://github.com/mehad605/locked_secure/actions/runs/45917574738), [CI #45917567834](https://github.com/mehad605/locked_secure/actions/runs/45917567834) |
| **S8.** Correctness & regression safety *(GATE)* | `0 / 10` *(10/10 after Phase 1 safety net)* | **10 / 10** | [PARITY.md](PARITY.md) (10/10 PASS), 35 unit tests, R8 smoke + upgrade-install PASS |

---

## 2. Metrics Comparison (`baseline-v1` vs. `overhaul/v2`)

Every figure below is **VERIFIED** from tool/emulator command output unless explicitly marked **ESTIMATED**.

| Metric | `baseline-v1` | `overhaul/v2` | Change | Status |
|---|---:|---:|---:|---|
| Release APK size (universal vs arm64 split) | `4,148,087 B` (`3.96 MiB`) | **`59,281 B`** (`59,581 B` at v2.0.0) | **`-98.6%`** | VERIFIED |
| Play/download size (`apkanalyzer`) | `3,756,688 B` (`3.58 MiB`) | **`52,262 B`** (`51.0 KiB`) | **`-98.6%`** | VERIFIED |
| Installed size (`/data/app/...` on API 30) | `3.9 MiB` | **`72 KiB`** | **`-98.2%`** | VERIFIED |
| DEX method references | `67,865` (2 DEX files) | **`279`** (1 DEX file) | **`-99.6%`** | VERIFIED |
| Defined DEX classes / methods | `9,061` / `64,307` | **`46`** / **`111`** | **`-99.5%`** / **`-99.8%`** | VERIFIED |
| `resources.arsc` size | `439,672 B` | **`4,000 B`** | **`-99.1%`** | VERIFIED |
| Native `.so` libraries | `1` (`libandroidx.graphics.path.so`, unstripped) | **`0`** | **`-100%`** | VERIFIED |
| Resolved runtime artifacts (`releaseRuntimeClasspath`) | `213` | **`2`** (`kotlin-stdlib` + `annotations`) | **`-99.1%`** | VERIFIED |
| Startup `ContentProvider` initializers | `3` (`androidx.startup`) | **`0`** | **`-100%`** | VERIFIED |
| Cold start (API 30, median of 10 runs) | `233.0 ms` (`208–262 ms`) | **`174.5 ms`** (`153–215 ms`) | **`-25.1%`** | VERIFIED |
| Active RAM (Total PSS, API 30) | `31,676 KB` (`30.9 MiB`) | **`20,383 KB`** (`19.9 MiB`) | **`-35.7%`** | VERIFIED |
| Idle RAM (Total PSS, 2 min idle, API 30) | `32,822 KB` (`32.1 MiB`) | **`20,507 KB`** (`20.0 MiB`) | **`-37.5%`** | VERIFIED |
| App `.dex mmap` PSS (API 30) | `8,509 KB` | **`68 KB`** | **`-99.2%`** | VERIFIED |
| Idle CPU / cumulative `TIME+` (API 30) | `0.0%` / `0:00.41` | **`0.0%` / `0:00.17`** | **`-58.5%` CPU time** | VERIFIED |
| Background wakeups / jobs / alarms / app wakelocks | `0` / `0` / `0` / `0` | **`0` / `0` / `0` / `0`** | Parity (`0/hr`) | VERIFIED |
| Debug `StrictMode` violations | Unchecked | **`0`** | Clean | VERIFIED |
| Largest production file | `311` lines (`MainActivity.kt`) | **`193`** lines (`DashboardComponents.kt`) | **`-37.9%`** | VERIFIED |
| Largest function | `187` lines (`MainScreen`) | **`39`** lines (`DashboardViewFactory.render`) | **`-79.1%`** | VERIFIED |
| Max / Avg cyclomatic complexity (`>10` count) | `14` / `3.58` (`1` fn `>10`) | **`6` / `1.70` (`0` fns `>10`)** | **`-57.1%` max** | VERIFIED |
| Duplication (`jscpd`) | `1.76%` (`1` clone) | **`0.00%`** (`0` clones) | **`-100%`** | VERIFIED |
| Automated tests (JVM + architecture) | `0` | **`35`** (`100%` core contract C1–C8) | **`+35` tests** | VERIFIED |
| Android Lint warnings / Detekt / Spotless | `15` / N/A / N/A | **`0` / `0` / `0`** | **`-100%`** | VERIFIED |
| KDoc coverage on public APIs | `0%` | **`100%`** (`45 / 45`) | **`+100 pp`** | VERIFIED |
| `zizmor --pedantic` / `actionlint` findings | `12` / `0` | **`0` / `0`** | **`-100%`** | VERIFIED |
| Physical Realme Narzo 20 jank & 30-min soak | Not attached | Not attached | **ESTIMATED** (0 leaks in 15x recreate stress on API 30; `aosp_atd` headless image disables display frame submission) | ESTIMATED |

### Explaining the Remaining Footprint Floor
- **APK (`59.3 KB`):** `33.3 KB` legacy WebP launcher icons for `minSdk 24–25`, `12.7 KB` R8-shrunk `classes.dex`, `4.0 KB` `resources.arsc`, `1.8 KB` XML configs/vectors, `1.2 KB` manifest, `~6.2 KB` APK v2 signature block + ZIP central directory. Nothing remains that can be cut without dropping `minSdk 24–25` launcher icons or core functionality.
- **Idle RAM (`20.5 MB` PSS on API 30):** `11.0 MB` Android Zygote/HWUI native heap + `6.9 MB` proportional share of OS system `.so`/`.jar`/`.art`/`.oat` + `1.1 MB` ART base Java heap + `0.66 MB` framework code/fonts. LockedSecure's own DEX contributes **`68 KB`** (`0.3%` of total PSS) — effectively the Android 11 Activity process floor.

---

## 3. Parity Summary & Enhancements

- **Feature parity:** **10 / 10 PASS** in [docs/PARITY.md](PARITY.md), including in-place upgrade install (`baseline-v1` `versionCode=1` → `overhaul/v2` `versionCode=20000`) preserving active Accessibility Service state with zero crashes.
- **Approved architectural optimizations:** Replacing Compose with pure Android Views and publishing a single universal APK were explicitly approved by the user after Phase 1 baseline review ([ADR-0001](adr/0001-incremental-strangler-and-native-views.md)).
- **Unrequested feature enhancements:** None added (strict parity + footprint discipline).

---

## 4. What Changed, What Was Rejected, and Why

### Kept Changes (see [docs/PROGRESS.md](PROGRESS.md) for commit-by-commit log)
- Extracted `feature/powerblocker`, `feature/deviceadmin`, and `feature/dashboard` with strict `ui` → `domain` ← `data` layering and lazy manual DI (`AppGraph`, `LockedSecureApp`).
- Replaced Compose + Material 3 + Navigation 3 + AndroidX with pure Android framework Views (`DashboardViewFactory`, `DashboardComponents`, `ShieldView`, `DashboardTheme`) maintaining pixel-identical dark matte UI.
- Hardened Accessibility Service status parsing to handle OEM short & full `ComponentName` formats without substring false positives.
- Removed release logging, blanket ProGuard rules, unused XML resources, ABI splits, and white splash flash.
- Added 35 automated tests (characterization, domain, data, UI, `ArchitectureBoundaryTest`), Spotless (`ktlint`), Detekt, 0-warning Android Lint, and `scripts/verify_release_smoke.sh`.
- Hardened CI & release workflows with SHA-pinned actions, composite setup action, wrapper validation, deterministic SemVer `versionCode`, `apksigner`+size+version verification (`scripts/verify_release_apk.sh`), CycloneDX SBOM, SLSA provenance, `SHA256SUMS.txt`, draft-then-publish idempotency, dry-run job, Dependabot, and `CODEOWNERS`.
- Produced full docs (`ARCHITECTURE.md`, `DEPENDENCIES.md`, `HOW_TO.md`, `RELEASING.md`, `REPO_SETTINGS.md`, 3 ADRs, 6 package READMEs), replaceability/extension proofs, and a 10/10 fresh-subagent cold-read audit.

### Rejected / Reverted Approaches
- **Keeping Jetpack Compose + trimming deps:** Rejected after Phase 1 review because a 1-screen utility does not justify ~1.2 MB APK, `libandroidx.graphics.path.so`, `androidx.startup` providers, and ~26+ MB RAM when pure Android Views achieve identical UI at `59 KB`.
- **Hilt / Koin DI:** Rejected ([ADR-0002](adr/0002-manual-dependency-injection.md)) due to KSP/reflection/library overhead for only 4 collaborators.
- **`androidx.profileinstaller` Baseline Profile:** Rejected because `classes.dex` is `26.5 KB` raw; `profileinstaller` would add ~40 KB + `InitializationProvider`.
- **`$/` self-repository syntax in GitHub workflows:** Tested and reverted to `./.github/actions/setup-android-env` with documented `# zizmor: ignore[self-repository]` because `actionlint` v1.7.12 rejects `$/` syntax.

---

## 5. Items You Must Apply Yourself

All manual GitHub repository settings are itemized as a checklist in [docs/REPO_SETTINGS.md](REPO_SETTINGS.md):
1. Create the **`release`** GitHub Environment with required reviewers and `v*` tag deployment restriction, and move `SIGNING_KEY`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` into it (then delete the old repo-level copies).
2. Enable `main` branch protection (require PR, Code Owner review, signed commits, required CI checks: `Format, Static Analysis, Lint & Test` and `Dependency Review`).
3. Enable `v*` tag protection (admin-only, signed tags).
4. Enable GitHub **Secret scanning**, **Push protection**, and **Dependabot alerts/security updates** under **Settings → Code security**.
5. Set default Actions `GITHUB_TOKEN` workflow permission to **Read repository contents** under **Settings → Actions → General**.
6. Review and merge the `overhaul/v2` → `main` PR, then push a signed tag (e.g., `git tag -s v2.0.0 -m "Release v2.0.0" && git push origin v2.0.0`) to verify a real signed production release.

---

## 6. Rollback Plan

`main` was never modified and its exact pre-overhaul state is tagged `baseline-v1` (`ac50c14`).

- **Before merging the PR:** Close the PR and delete branch `overhaul/v2`. Zero impact on `main`.
- **After merging the PR (code rollback):**
  ```bash
  git checkout main && git pull --ff-only
  git revert -m 1 <merge-commit-sha>
  git push origin main
  ```
  Or to restore the exact pre-overhaul tree on a hotfix branch: `git checkout -b rollback/v1 baseline-v1`.
- **User device rollback (if `v2.0.0` was already installed):** Because Android rejects downgrading `versionCode` in place (`20000` → `1`), either publish a rollback build from `baseline-v1` with a higher `APP_VERSION_CODE` (e.g. `20001`), or have the user uninstall `v2.0.0` (revoking Device Admin first) and sideload `v1.0.0`.

---

## 7. Remaining Opportunities & Tradeoffs

1. **Physical Realme Narzo 20 verification:** Running `scripts/verify_release_smoke.sh`, `dumpsys gfxinfo`, and a 30-minute soak on actual Realme UI 2.0 / Helio G85 hardware will confirm OEM power-menu dialog class names on Realme UI and real GPU frame timings.
2. **Legacy API 24–25 launcher WebPs (`33.3 KB` = 56% of the entire APK):** If `minSdk` were ever raised from `24` to `26` (Android 8.0, still well below Android 11 / API 30), the 10 legacy WebP launcher icons could be deleted in favor of `mipmap-anydpi-v26` adaptive XML vectors, dropping the entire signed universal APK from **`59 KB` → `~26 KB`** (`-56%` further). Kept `minSdk = 24` here to avoid an unrequested compatibility tradeoff.
3. **Gradle 9.9.x / AGP 9.5.x upgrade:** Dependabot (`.github/dependabot.yml`) will open automated PRs to track newer Gradle/AGP/test-library releases deterministically.
