#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMPLATE="$ROOT_DIR/tools/benchmark/com.localmind.llm-benchmark.plist.template"
PLIST_DEST="$HOME/Library/LaunchAgents/com.localmind.llm-benchmark.plist"
SCRIPT_PATH="$ROOT_DIR/tools/benchmark/run_benchmark.sh"
REPORT_ROOT="${LOCALMIND_BENCH_REPORT_ROOT:-$ROOT_DIR/build/benchmark-reports}"
INTERVAL_SECONDS="${1:-86400}"
LOG_DIR="$ROOT_DIR/build/benchmark-reports/logs"

mkdir -p "$(dirname "$PLIST_DEST")" "$LOG_DIR"

sed \
  -e "s|__SCRIPT_PATH__|$SCRIPT_PATH|g" \
  -e "s|__REPO_ROOT__|$ROOT_DIR|g" \
  -e "s|__REPORT_ROOT__|$REPORT_ROOT|g" \
  -e "s|__INTERVAL_SECONDS__|$INTERVAL_SECONDS|g" \
  -e "s|__LOG_DIR__|$LOG_DIR|g" \
  "$TEMPLATE" > "$PLIST_DEST"

launchctl unload "$PLIST_DEST" >/dev/null 2>&1 || true
launchctl load "$PLIST_DEST"

echo "Installed launchd job: $PLIST_DEST"
echo "Interval seconds: $INTERVAL_SECONDS"
echo "Use tools/benchmark/uninstall_launchd_job.sh to remove it."
