#!/usr/bin/env bash
#
# Wireless ADB helper (Android 11+ "Depuração sem fio").
#
# On the phone: Settings > Developer options > Wireless debugging.
#   1st time:  "Pair device with pairing code" -> use `pair`
#   afterwards: the IP:port on the main wireless-debugging screen -> use `connect`
#
# Usage:
#   scripts/wireless-adb.sh pair    <ip:port> <pairing-code>
#   scripts/wireless-adb.sh connect <ip:port>
#   scripts/wireless-adb.sh devices
#   scripts/wireless-adb.sh disconnect <ip:port>
#
# Tip (WSL2): the phone must be on the same Wi-Fi. WSL2 reaches the LAN through
# NAT, so connecting from inside WSL usually works. If it does not, run the ADB
# server on the Windows side and point this script at it:
#   (Windows) adb.exe -a nodaemon server start
#   (WSL)     export ADB_SERVER_SOCKET=tcp:<windows-host-ip>:5037
#
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/.android-toolchain/sdk}}"
ADB="$SDK/platform-tools/adb"

if [[ ! -x "$ADB" ]]; then
    echo "adb não encontrado em: $ADB" >&2
    echo "Defina ANDROID_SDK_ROOT/ANDROID_HOME para o seu SDK." >&2
    exit 1
fi

cmd="${1:-}"
case "$cmd" in
    pair)
        [[ $# -eq 3 ]] || { echo "uso: $0 pair <ip:port> <código>" >&2; exit 1; }
        "$ADB" pair "$2" "$3"
        ;;
    connect)
        [[ $# -eq 2 ]] || { echo "uso: $0 connect <ip:port>" >&2; exit 1; }
        "$ADB" connect "$2"
        "$ADB" devices -l
        ;;
    disconnect)
        [[ $# -eq 2 ]] || { echo "uso: $0 disconnect <ip:port>" >&2; exit 1; }
        "$ADB" disconnect "$2"
        ;;
    devices|"")
        "$ADB" devices -l
        ;;
    *)
        echo "comando desconhecido: $cmd" >&2
        sed -n '2,20p' "$0"
        exit 1
        ;;
esac
