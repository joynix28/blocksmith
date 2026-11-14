#!/usr/bin/env bash
# Banc d'essai : lance l'app desktop sous Xvfb et la pilote à la souris.
#   ./scripts/dev.sh start|stop|shot <fichier>|click <x> <y>|drag <x1> <y1> <x2> <y2>|win
set -uo pipefail
export DISPLAY=:99
export JAVA_HOME="$HOME/dev/jdk"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG=/tmp/opencode/desktop-run.log
PIDFILE=/tmp/opencode/desktop-run.pid

ensure_xvfb() {
  pgrep -x Xvfb >/dev/null || {
    setsid Xvfb :99 -screen 0 1200x1400x24 +extension GLX +render -noreset -nolisten tcp \
      >/tmp/opencode/xvfb.log 2>&1 < /dev/null &
    sleep 1
  }
}

window_id() { xdotool search --name "^Blocksmith$" 2>/dev/null | tail -1; }

stop() {
  if [ -f "$PIDFILE" ]; then
    local pid
    pid=$(cat "$PIDFILE")
    kill -TERM "-$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null
    rm -f "$PIDFILE"
  fi
  for _ in $(seq 1 25); do
    [ -z "$(window_id)" ] && break
    sleep 0.2
  done
  echo stopped
}

start() {
  ensure_xvfb
  [ -n "$(window_id)" ] && stop >/dev/null
  cd "$ROOT"
  setsid env JAVA_HOME="$JAVA_HOME" \
    LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe \
    JAVA_TOOL_OPTIONS="-Dskiko.renderApi=${SKIKO_API:-SOFTWARE}" \
    ./gradlew :desktop:run --no-daemon --console=plain \
    > "$LOG" 2>&1 < /dev/null &
  echo $! > "$PIDFILE"
  for _ in $(seq 1 150); do
    if [ -n "$(window_id)" ]; then sleep 1.5; echo "window up"; return 0; fi
    sleep 1
  done
  echo "TIMEOUT: no window. Log:"; tail -40 "$LOG"; return 1
}

shot() { local out="${1:-/tmp/opencode/shot.png}"; import -window "$(window_id)" "$out" && echo "$out"; }
click() { xdotool mousemove "$1" "$2" click 1; sleep 0.5; }
drag() {
  xdotool mousemove "$1" "$2" mousedown 1
  local steps=16
  for i in $(seq 1 $steps); do
    xdotool mousemove $(( $1 + ($3 - $1) * i / steps )) $(( $2 + ($4 - $2) * i / steps ))
    sleep 0.02
  done
  sleep 0.2
  xdotool mouseup 1
  sleep 0.6
}

case "${1:-}" in
  start) start ;;
  stop) stop ;;
  shot) shot "${2:-/tmp/opencode/shot.png}" ;;
  click) click "$2" "$3" ;;
  drag) drag "$2" "$3" "$4" "$5" ;;
  win) window_id ;;
  *) echo "usage: $0 start|stop|shot|click|drag|win"; exit 1 ;;
esac
