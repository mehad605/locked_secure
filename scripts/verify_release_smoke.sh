#!/usr/bin/env bash
# End-to-end smoke test for the R8-minified signed release APK on a connected Android device/emulator.
# Verifies Core Contracts C6, C7, C8, and R1 (zero crashes in logcat during core flows).
set -euo pipefail

APK="${1:-app/build/outputs/apk/release/app-release.apk}"
PKG="com.example.lockedsecure"
ACT="$PKG/.MainActivity"

if [ ! -f "$APK" ]; then
  echo "ERROR: APK not found at $APK" >&2
  exit 1
fi

echo "==> Installing release APK: $APK"
adb install -r "$APK"

echo "==> Clearing logcat & launching $ACT"
adb logcat -c
adb shell am force-stop "$PKG"
adb shell settings delete secure enabled_accessibility_services >/dev/null 2>&1 || true
adb shell settings put secure accessibility_enabled 0
adb shell am start -W -n "$ACT" >/dev/null
sleep 2

dump_ui() {
  adb shell uiautomator dump /sdcard/lockedsecure_smoke_ui.xml >/dev/null
  adb shell cat /sdcard/lockedsecure_smoke_ui.xml
  adb shell rm -f /sdcard/lockedsecure_smoke_ui.xml
}

assert_contains() {
  local haystack="$1"
  local needle="$2"
  if ! printf '%s' "$haystack" | grep -Fq "$needle"; then
    echo "ASSERTION FAILED: expected UI to contain '$needle'" >&2
    exit 1
  fi
}

echo "==> Verifying initial UI state (unprotected)"
UI_UNPROTECTED="$(dump_ui)"
assert_contains "$UI_UNPROTECTED" "LOCKEDSECURE"
assert_contains "$UI_UNPROTECTED" "ANTI-THEFT SHIELD"
assert_contains "$UI_UNPROTECTED" "Shield Offline"
assert_contains "$UI_UNPROTECTED" "Power Blocker: Unprotected"
assert_contains "$UI_UNPROTECTED" "Uninstall Protection: Unprotected"
assert_contains "$UI_UNPROTECTED" "Enable Power Blocker"
assert_contains "$UI_UNPROTECTED" "Enable Admin Protection"
echo "    PASS: All unprotected dashboard elements verified"

echo "==> Verifying Power Blocker state reflection on onResume"
adb shell settings put secure enabled_accessibility_services "$PKG/$PKG.PowerMenuBlockerService"
adb shell settings put secure accessibility_enabled 1
adb shell input keyevent KEYCODE_HOME
sleep 1
adb shell am start -W -n "$ACT" >/dev/null
sleep 1
UI_ACC="$(dump_ui)"
assert_contains "$UI_ACC" "Power Blocker: Protected"
assert_contains "$UI_ACC" "Blocker Service Enabled"
assert_contains "$UI_ACC" "Shield Offline"
echo "    PASS: Accessibility enabled state reflected on resume"

echo "==> Restoring accessibility settings"
adb shell settings delete secure enabled_accessibility_services >/dev/null 2>&1 || true
adb shell settings put secure accessibility_enabled 0
adb shell input keyevent KEYCODE_HOME
sleep 1

echo "==> Verifying Settings navigation buttons (C8)"
adb shell am start -W -n "$ACT" >/dev/null
sleep 1
UI_NOW="$(dump_ui)"
tap_text() {
  local label="$1"
  local coords
  coords="$(python3 - "$label" "$UI_NOW" <<'PY'
import re, sys
label, xml = sys.argv[1], sys.argv[2]
m = re.search(r'text="' + re.escape(label) + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if not m:
    sys.exit(1)
x1, y1, x2, y2 = map(int, m.groups())
print((x1 + x2) // 2, (y1 + y2) // 2)
PY
)"
  echo "    Tapping '$label' at $coords"
  adb shell input tap $coords
}
tap_text "Enable Power Blocker"
sleep 1
adb shell am start -W -n "$ACT" >/dev/null
sleep 1
UI_NOW="$(dump_ui)"
tap_text "Enable Admin Protection"
sleep 1
echo "    PASS: Both settings buttons tapped without crash"

echo "==> Checking logcat for crashes in $PKG"
CRASHES="$(adb logcat -d -s AndroidRuntime:E | grep -F "$PKG" || true)"
if [ -n "$CRASHES" ]; then
  echo "CRASH DETECTED IN LOGCAT:" >&2
  echo "$CRASHES" >&2
  exit 1
fi
echo "    PASS: Zero crashes in logcat"
echo "==> ALL RELEASE SMOKE CHECKS PASSED"
