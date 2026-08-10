#!/usr/bin/env bash
set -euo pipefail

apk_path="${1:?usage: android_runtime_smoke.sh APK [ARTIFACT_DIR]}"
artifact_dir="${2:-runtime-smoke-artifacts}"
package_name="com.example.bloodmoonnightfall"
activity_name="${package_name}/.MainActivity"

mkdir -p "${artifact_dir}"
adb wait-for-device
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb install -r -t "${apk_path}"
adb logcat -c
adb shell am force-stop "${package_name}"

set +e
adb shell am start -W -n "${activity_name}" >"${artifact_dir}/activity-start.txt" 2>&1
start_status=$?
set -e
if [[ ${start_status} -ne 0 ]]; then
    adb logcat -d -v threadtime >"${artifact_dir}/logcat.txt" || true
    cat "${artifact_dir}/activity-start.txt"
    echo "Activity launch command failed with exit ${start_status}." >&2
    exit 1
fi

ready=0
for _ in $(seq 1 240); do
    adb logcat -d -v brief >"${artifact_dir}/logcat-brief.txt"
    if grep -qE "VaylornBoot.*GAME_READY|VAYLORN_GAME_READY" \
            "${artifact_dir}/logcat-brief.txt"; then
        ready=1
        break
    fi
    if ! adb shell pidof "${package_name}" >/dev/null 2>&1; then
        break
    fi
    sleep 1
done

adb logcat -d -v threadtime >"${artifact_dir}/logcat.txt" || true
adb shell dumpsys activity activities >"${artifact_dir}/activity-dump.txt" || true
adb exec-out screencap -p >"${artifact_dir}/first-frame.png" || true

if [[ ${ready} -ne 1 ]]; then
    grep -E "VaylornBoot|AndroidRuntime|FATAL EXCEPTION|Fatal signal|>>> ${package_name} <<<|Godot" \
        "${artifact_dir}/logcat.txt" || true
    echo "The game scene did not report GAME_READY within 240 seconds." >&2
    exit 1
fi

# A single frame is not enough: keep the process alive through initial audio, wave spawning,
# autosave scheduling, and the first automatic skill decisions.
sleep 15
if ! adb shell pidof "${package_name}" >"${artifact_dir}/pid.txt" 2>&1; then
    adb logcat -d -v threadtime >"${artifact_dir}/logcat.txt" || true
    echo "The game process died after reporting GAME_READY." >&2
    exit 1
fi

if grep -qE "Process: ${package_name}|>>> ${package_name} <<<" "${artifact_dir}/logcat.txt"; then
    grep -E "AndroidRuntime|FATAL EXCEPTION|Fatal signal|>>> ${package_name} <<<" \
        "${artifact_dir}/logcat.txt" || true
    echo "A Java or native crash was recorded for the game process." >&2
    exit 1
fi

echo "ANDROID_RUNTIME_SMOKE_PASS"
