# Releasing LockedSecure

This document describes how LockedSecure builds, verifies, signs, and publishes releases via [.github/workflows/release.yml](../.github/workflows/release.yml).

---

## 1. Versioning (Single Source of Truth)

Release versions are derived **strictly and deterministically** from SemVer Git tags matching `vMAJOR.MINOR.PATCH`:

- `versionName` = `MAJOR.MINOR.PATCH` (e.g., `2.0.0`)
- `versionCode` = `MAJOR * 10000 + MINOR * 100 + PATCH` (e.g., `20000`)

Neither value is hand-edited across files; `app/build.gradle.kts` reads `APP_VERSION_NAME` and `APP_VERSION_CODE` set by the workflow (defaulting to `1.0.0` / `1` for local developer builds).

### Determinism & Residual Non-Determinism
- **Pinned toolchain:** JDK 17 (Temurin), Gradle 9.1.0 (wrapper SHA-256 verified), AGP 9.0.1, `cache-disabled: true`, `vcsInfo.include = false`, `dependenciesInfo.includeInApk = false`.
- **Residual non-determinism:** Rebuilding the exact same tag produces identical `classes.dex`, `resources.arsc`, `AndroidManifest.xml`, and `versionCode`/`versionName`; minor byte differences in the APK signing block can occur only if an external timestamping authority or a different JKS key is used (APK Signature Scheme v2/v3 with local JKS is deterministic for a fixed key and ZIP entry ordering).

---

## 2. Standard Release Procedure

1. Ensure `main` is green in [.github/workflows/ci.yml](../.github/workflows/ci.yml) and `CHANGELOG.md` is updated.
2. Create and push a signed SemVer tag from `main`:
   ```bash
   git checkout main && git pull --ff-only
   git tag -s v2.0.0 -m "Release v2.0.0"
   git push origin v2.0.0
   ```
3. Approve the `release` GitHub Environment deployment (if required reviewers are enabled per [REPO_SETTINGS.md](REPO_SETTINGS.md)).
4. The workflow automatically executes:
   - `validate`: Gradle wrapper validation, `spotlessCheck`, `detekt`, `lint`, `test` (with Gradle cache disabled).
   - `release`: decodes `SIGNING_KEY` from the `release` Environment into `$RUNNER_TEMP` (`umask 077`), builds `app-release.apk`, shreds the temp keystore (`if: always()`), runs `scripts/verify_release_apk.sh` (`apksigner verify`, `<= 150 KiB` budget, `aapt2` version match), generates `sbom.cdx.json` + `SHA256SUMS.txt`, publishes SLSA build provenance (`actions/attest-build-provenance`), and idempotently creates/updates a draft GitHub Release before promoting it to published.

---

## 3. Running a Dry Run

You can test the entire release pipeline without touching production signing secrets or creating a GitHub Release:

```bash
gh workflow run release.yml --ref overhaul/v2 -f dry_run_tag=v2.0.0
```

The `dry-run` job generates an ephemeral single-use keystore inside `$RUNNER_TEMP`, runs the exact `assembleRelease` + `scripts/verify_release_apk.sh` + CycloneDX SBOM + `SHA256SUMS.txt` pipeline, shreds the ephemeral keystore, and uploads the verification outputs as a workflow artifact (`release-dry-run-artifacts`).

---

## 4. Rotating Signing Keys

> **Warning:** Changing the APK signing key prevents existing sideloaded users from upgrading in place (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) unless you use APK Signature Scheme v3 key rotation (`apksigner rotate`).

### Standard Key Rotation (with APK Signature Scheme v3 lineage) or Fresh Key
1. Generate a new 4096-bit RSA (or ECDSA) keystore offline:
   ```bash
   keytool -genkeypair -v \
     -keystore lockedsecure-release-v2.jks \
     -alias lockedsecure \
     -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Base64-encode without line wraps:
   ```bash
   base64 -w 0 lockedsecure-release-v2.jks > keystore.b64
   ```
3. Update the four secrets scoped strictly to the **`release` GitHub Environment** (Settings → Environments → `release`):
   - `SIGNING_KEY`
   - `SIGNING_STORE_PASSWORD`
   - `SIGNING_KEY_ALIAS`
   - `SIGNING_KEY_PASSWORD`
4. Securely delete local temporary files (`shred -u keystore.b64`) and store the offline backup keystore in an encrypted password manager / hardware backup.
5. Delete any legacy repository-level signing secrets once migrated to the `release` Environment.
6. If distributing via Google Play in the future, enroll in **Play App Signing** so the CI key acts strictly as a rotatable upload key.

---

## 5. Re-running a Failed Release

Because publishing creates a `--draft` release first (or uploads with `--clobber` if retrying a failed draft) and only sets `--draft=false` as the final step, a mid-run failure never leaves a half-published public release. Simply re-run the failed GitHub Actions workflow after resolving the cause.
