#!/usr/bin/env bash
set -euo pipefail

PLIST_DEST="$HOME/Library/LaunchAgents/com.localmind.llm-benchmark.plist"

if [[ -f "$PLIST_DEST" ]]; then
  launchctl unload "$PLIST_DEST" >/dev/null 2>&1 || true
  rm -f "$PLIST_DEST"
  echo "Removed launchd job: $PLIST_DEST"
else
  echo "No launchd job found at: $PLIST_DEST"
fi
