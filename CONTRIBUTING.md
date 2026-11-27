# Contributing to LockedSecure

Thank you for contributing to LockedSecure. This project prioritizes **correctness**, **security**, **clarity**, and **minimal footprint** on resource-constrained Android devices (`minSdk = 24`, target API 30+ hardware with 4 GB RAM).

---

## 1. Local Setup

1. Install **JDK 17** (Temurin) and **Android SDK** (`platforms;android-36`, `build-tools;36.0.0`).
2. Clone the repository and verify the Gradle wrapper works:
   ```bash
   ./gradlew --version
   ```
3. Run the full verification suite before making edits:
   ```bash
   ./gradlew spotlessCheck detekt lint test assembleRelease
   ```

---

## 2. Branch & Pull Request Workflow

1. Create a focused branch from `main` (e.g., `feat/<topic>`, `fix/<topic>`, `refactor/<topic>`).
2. Keep commits small, single-purpose, and buildable, using [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` — new user-visible feature
   - `fix:` — bug fix
   - `refactor:` — behavior-preserving structural change
   - `opt:` — size, RAM, CPU, or startup optimization
   - `test:` — tests only
   - `docs:` — documentation only
   - `ci:` / `chore:` — CI workflows, build tooling, dependencies
3. Update `CHANGELOG.md` under `[Unreleased]` for user-facing changes.
4. Open a Pull Request targeting `main`. CI (`.github/workflows/ci.yml`) automatically runs wrapper validation, Spotless formatting, Detekt, Android Lint, unit + architecture tests, and dependency review.

---

## 3. Code Style & Architecture Rules

- **Formatting:** 4-space indentation, 120-character max line length, no wildcard imports, LF line endings (`.editorconfig`). Run `./gradlew spotlessApply` to auto-format.
- **Static analysis:** Detekt (`config/detekt/detekt.yml`) and Android Lint (`app/lint.xml`, `warningsAsErrors = true`) must pass with **0 errors and 0 warnings**.
- **Size limits:** Max **300 lines** per production file and max **40 lines** per function; cyclomatic complexity `<= 10`.
- **Layering:** Enforced automatically by `ArchitectureBoundaryTest`:
  - `*.domain` packages are pure Kotlin (zero `android.*`, `*.data.*`, or `*.ui.*` imports).
  - `*.ui` packages never import `*.data.*`.
  - Features communicate only via public `*.domain` interfaces.
  - Zero circular package dependencies, zero `!!`, zero `lateinit` in production code.
- **Documentation:** Every public class and function requires KDoc explaining what it does and why. Complex logic must include comments explaining *why*, not *what*.
- **Offline-first & footprint rules:**
  - Zero internet or network permissions (`ManifestContractTest` fails if any are added).
  - Do not add runtime dependencies without justifying them in `docs/DEPENDENCIES.md` and staying within the `150 KB` release APK budget.
  - Never commit keystores, `.env`, `keystore.properties`, or credentials.

---

## 4. Running All Checks Locally

```bash
# Auto-format Kotlin and Gradle files
./gradlew spotlessApply

# Full CI verification (formatting, static analysis, lint, unit/architecture tests, release build)
./gradlew spotlessCheck detekt lint test assembleRelease

# Optional: verify R8-minified release APK on a connected device/emulator
bash scripts/verify_release_smoke.sh app/build/outputs/apk/release/app-release.apk
```

See [docs/HOW_TO.md](docs/HOW_TO.md) for step-by-step recipes on adding features, screens, settings, or replacing layers.
