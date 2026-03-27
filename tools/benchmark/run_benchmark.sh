#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TASK="com.markduenas.localmind.ai.benchmark.IosLlmBenchmarkTest.runBenchmarkSuite"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
PERIODIC=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --periodic)
      PERIODIC=true
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      echo "Usage: $0 [--periodic]" >&2
      exit 1
      ;;
  esac
done

REPORT_ROOT="${LOCALMIND_BENCH_REPORT_ROOT:-$ROOT_DIR/build/benchmark-reports}"
OUT_DIR="$REPORT_ROOT/$RUN_ID"
mkdir -p "$OUT_DIR"
LOG_FILE="$OUT_DIR/benchmark.log"

export LOCALMIND_RUN_LLM_BENCHMARK=true
if [[ "$PERIODIC" == "true" ]]; then
  export LOCALMIND_BENCH_PERIODIC=true
fi

GRADLE_ARGS=(
  ":composeApp:iosSimulatorArm64Test"
  "--tests" "$TASK"
  "--rerun"
  "--console=plain"
  "-PlocalmindRunLlmBenchmark=true"
)

if [[ "$PERIODIC" == "true" ]]; then
  GRADLE_ARGS+=("-PlocalmindBenchPeriodic=true")
fi

if [[ -n "${LOCALMIND_BENCH_THIRD_MODEL:-}" ]]; then
  GRADLE_ARGS+=("-PlocalmindBenchThirdModel=${LOCALMIND_BENCH_THIRD_MODEL}")
fi

if [[ -n "${LOCALMIND_BENCH_DEVICE:-}" ]]; then
  GRADLE_ARGS+=("--device" "${LOCALMIND_BENCH_DEVICE}")
fi

set +e
(
  cd "$ROOT_DIR"
  ./gradlew "${GRADLE_ARGS[@]}"
) | tee "$LOG_FILE"
GRADLE_EXIT=${PIPESTATUS[0]}
set -e

XML_RESULT_FILE="$ROOT_DIR/composeApp/build/test-results/iosSimulatorArm64Test/TEST-com.markduenas.localmind.ai.benchmark.IosLlmBenchmarkTest.xml"

extract_block_from_file() {
  local file="$1"
  local name="$2"
  awk "/LOCALMIND_BENCH_${name}_BEGIN/{flag=1;next}/LOCALMIND_BENCH_${name}_END/{flag=0}flag" "$file"
}

extract_block() {
  local name="$1"
  if [[ -f "$XML_RESULT_FILE" ]]; then
    extract_block_from_file "$XML_RESULT_FILE" "$name"
  else
    extract_block_from_file "$LOG_FILE" "$name"
  fi
}

extract_block "JSON" > "$OUT_DIR/results.json"
extract_block "SUMMARY_MD" > "$OUT_DIR/summary.md"
extract_block "SUGGESTIONS_MD" > "$OUT_DIR/suggestions.md"
extract_block "DETAILED_MD" > "$OUT_DIR/detailed.md"
extract_block "ROUTING_MD" > "$OUT_DIR/routing.md"

if [[ ! -s "$OUT_DIR/results.json" || ! -s "$OUT_DIR/summary.md" || ! -s "$OUT_DIR/suggestions.md" || ! -s "$OUT_DIR/detailed.md" || ! -s "$OUT_DIR/routing.md" ]]; then
  echo "Failed to extract benchmark report blocks from test output. Check $LOG_FILE" >&2
  exit 2
fi

if [[ $GRADLE_EXIT -ne 0 ]]; then
  echo "Benchmark task failed. Reports may be partial. See $LOG_FILE" >&2
  exit $GRADLE_EXIT
fi

echo "Benchmark reports written to: $OUT_DIR"
echo "- $OUT_DIR/results.json"
echo "- $OUT_DIR/summary.md"
echo "- $OUT_DIR/suggestions.md"
echo "- $OUT_DIR/detailed.md"
echo "- $OUT_DIR/routing.md"
