#!/usr/bin/env bash
#
# Build the debug APK and install it straight onto a connected device
# (USB or wireless ADB), then launch it. No manual APK handling.
#
# Usage:
#   scripts/install-debug.sh
#
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-$HOME/.android-toolchain/jdk}"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/.android-toolchain/sdk}}"
export PATH="$JAVA_HOME/bin:$SDK/platform-tools:$PATH"

cd "$(dirname "$0")/.."

echo "==> Dispositivos conectados"
adb devices -l

echo "==> Compilando e instalando (:app:installDebug)"
./gradlew :app:installDebug

echo "==> Abrindo o Brigadeiro"
adb shell am start -n com.brigadeiro.app/.MainActivity >/dev/null

echo "Pronto."
