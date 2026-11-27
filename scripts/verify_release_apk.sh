#!/usr/bin/env bash
# Verifies a built release APK before publishing:
#   1. Signature validity via apksigner verify
#   2. Strict APK size budget (<= MAX_APK_BYTES, default 153600 B = 150 KiB)
#   3. Embedded versionName and versionCode match the expected SemVer tag
set -euo pipefail

APK_PATH="${1:?Usage: verify_release_apk.sh <apk-path> <expected-version-name> <expected-version-code> [max-bytes]}"
EXPECTED_VERSION_NAME="${2:?Missing expected versionName}"
EXPECTED_VERSION_CODE="${3:?Missing expected versionCode}"
MAX_APK_BYTES="${4:-153600}"

if [ ! -f "$APK_PATH" ]; then
  echo "ERROR: APK not found at $APK_PATH" >&2
  exit 1
fi

SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK_ROOT" ] || [ ! -d "$SDK_ROOT/build-tools" ]; then
  echo "ERROR: ANDROID_HOME / ANDROID_SDK_ROOT build-tools not found" >&2
  exit 1
fi

BUILD_TOOLS_DIR="$(find "$SDK_ROOT/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)"
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"
AAPT2="$BUILD_TOOLS_DIR/aapt2"

echo "==> [1/3] Verifying APK signature ($APKSIGNER)"
"$APKSIGNER" verify --verbose "$APK_PATH"

echo "==> [2/3] Checking APK size budget (<= ${MAX_APK_BYTES} bytes)"
ACTUAL_BYTES="$(wc -c < "$APK_PATH" | tr -d ' ')"
echo "    Actual APK size: ${ACTUAL_BYTES} bytes (budget: ${MAX_APK_BYTES} bytes)"
if [ "$ACTUAL_BYTES" -gt "$MAX_APK_BYTES" ]; then
  echo "ERROR: APK size ${ACTUAL_BYTES} exceeds budget ${MAX_APK_BYTES} bytes" >&2
  exit 1
fi

echo "==> [3/3] Verifying APK version metadata against tag"
BADGING="$("$AAPT2" dump badging "$APK_PATH" | head -n 1)"
echo "    $BADGING"
ACTUAL_CODE="$(printf '%s\n' "$BADGING" | sed -n "s/.*versionCode='\([^']*\)'.*/\1/p")"
ACTUAL_NAME="$(printf '%s\n' "$BADGING" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")"

if [ "$ACTUAL_NAME" != "$EXPECTED_VERSION_NAME" ] || [ "$ACTUAL_CODE" != "$EXPECTED_VERSION_CODE" ]; then
  echo "ERROR: Version mismatch: got versionName='$ACTUAL_NAME' versionCode='$ACTUAL_CODE', expected '$EXPECTED_VERSION_NAME' / '$EXPECTED_VERSION_CODE'" >&2
  exit 1
fi

echo "==> ALL APK VERIFICATION GATES PASSED"
