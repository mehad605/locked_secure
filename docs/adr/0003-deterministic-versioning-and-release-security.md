# ADR-0003: Deterministic Tag-Derived Versioning & Hardened Release Pipeline

- **Status:** Accepted
- **Date:** 2026-11-27

## Context

In `baseline-v1`, `.github/workflows/release.yml` derived `VERSION_CODE=$((100 + github.run_number))`, which broke build reproducibility (re-running the same tag produced a different `versionCode` and APK digest). It also stored signing secrets at repository scope without a protected GitHub Environment, never deleted the decoded keystore file, enabled Gradle caching during release publishing (flagged by `zizmor` `cache-poisoning`), interpolated expressions into `run:` blocks (`template-injection`), and published directly without `apksigner verify` or size/version gates.

## Decision

1. **Single source of truth for version:** Derive both `APP_VERSION_NAME` and `APP_VERSION_CODE` deterministically from a strict SemVer tag `vMAJOR.MINOR.PATCH` (`versionName = "MAJOR.MINOR.PATCH"`, `versionCode = MAJOR * 10000 + MINOR * 100 + PATCH`). For example, `v2.0.0` always produces `versionCode = 20000` (strictly greater than `v1.0.0`'s `versionCode = 1` / `100+` CI runs, guaranteeing monotonic Android upgrades).
2. **Release isolation:** Scope signing secrets to a protected GitHub Environment (`release`), decode the keystore into `$RUNNER_TEMP` with `umask 077`, and shred/remove it in an `if: always()` step. Disable Gradle cache in the release job (`cache-disabled: true`) to prevent cache poisoning.
3. **Verification before publishing:** Run `apksigner verify`, verify `versionName` and `versionCode` inside the APK match the tag, enforce a strict APK size budget (`<= 150 KB`), generate CycloneDX SBOM + `SHA256SUMS.txt` + SLSA build provenance attestation (`actions/attest-build-provenance`), and publish idempotently via draft-then-publish using built-in `gh release`.

## Consequences

- Re-running a tag builds the same `versionName` and `versionCode` and produces a reproducible release artifact (residual non-determinism noted in `docs/RELEASING.md`: APK signature timestamp / ZIP ordering is normalized by AGP `vcsInfo.include = false` and `dependenciesInfo.includeInApk = false`).
- `actionlint` and `zizmor --pedantic` both pass with zero findings.
- Repository owner must create the `release` GitHub Environment and move the 4 signing secrets into it (documented in `docs/REPO_SETTINGS.md`).
