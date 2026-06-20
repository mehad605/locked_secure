# Contributing to LockedSecure

Thank you for your interest in contributing. Please follow these guidelines to keep the process smooth and transparent.

## How to contribute

1. **Fork** the repository.
2. **Create a feature branch** (`git checkout -b feat/my-feature`).
3. **Make your changes** — keep them scoped to a single feature or fix.
4. **Test** your changes (`./gradlew test assembleDebug`).
5. **Commit** with a clear message following [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` — new feature
   - `fix:` — bug fix
   - `refactor:` — code change that neither fixes nor adds
   - `docs:` — documentation only
   - `chore:` — tooling, CI, dependencies
6. **Push** to your fork and open a Pull Request against `main`.
7. **Describe** what your PR does and why it's needed.

## Pull Request guidelines

- Keep PRs small and focused (one concern per PR).
- Update `CHANGELOG.md` under `[Unreleased]` if your change is user-facing.
- Ensure the project builds cleanly before submitting.
- Ensure no hardcoded secrets or credentials are introduced.

## Code style

- This project uses Kotlin with Jetpack Compose. Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use the existing project formatting — 2-space indent, no wildcard imports.
- No internet permissions are allowed by design. Do not add networking dependencies.

## Code of conduct

Be respectful, constructive, and inclusive. Harassment or toxic behavior will not be tolerated.
