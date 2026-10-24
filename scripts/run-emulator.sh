#!/usr/bin/env bash
#
# Start the "brigadeiro" Android emulator (created by README/setup) in the
# background and wait until Android finishes booting. Uses the KVM accelerator,
# software GPU (safe under WSLg) and a window when a display is available.
#
# Usage: scripts/run-emulator.sh
#
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/.android-toolchain/sdk}}"
export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$SDK/platform-tools:$SDK/emulator:$PATH"

AVD="${AVD_NAME:-brigadeiro}"
LOG="/tmp/brigadeiro-emulator.log"

if ! "$SDK/emulator/emulator" -list-avds | grep -qx "$AVD"; then
    echo "AVD '$AVD' não encontrado. Crie com:" >&2
    echo "  avdmanager create avd -n $AVD -k 'system-images;android-34;google_apis;x86_64' -d pixel_6" >&2
    exit 1
fi

if ! adb devices | grep -q "^emulator-"; then
    args=(-avd "$AVD" -gpu swiftshader_indirect -no-boot-anim
          -netdelay none -netspeed full -no-snapshot-save)
    # Headless by default (more stable under WSL2). Set EMU_WINDOW=1 to open a
    # window through WSLg and EMU_AUDIO=1 to expose a microphone to the guest.
    [[ "${EMU_WINDOW:-0}" == "1" ]] || args+=(-no-window)
    [[ "${EMU_AUDIO:-0}" == "1" ]] || args+=(-no-audio)
    echo "==> Iniciando emulador '$AVD' (log: $LOG)"
    setsid "$SDK/emulator/emulator" "${args[@]}" >"$LOG" 2>&1 </dev/null &
else
    echo "==> Emulador já em execução"
fi

echo "==> Aguardando o boot do Android..."
adb wait-for-device
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
    sleep 3
done
echo "==> Emulador pronto. Agora rode: scripts/install-debug.sh"
