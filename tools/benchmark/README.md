# LocalMind LLM Benchmark

This benchmark targets LocalMind parsing quality for:
- 10 scheduled-task prompts
- 10 note prompts

It compares:
- `rule-based` parser baseline
- small LLM candidates (`gemma3-270m`, `qwen3-0.6`, and a third resolved candidate)

## Run Once (Local Mac)

```bash
tools/benchmark/run_benchmark.sh
```

To force a specific simulator/device UDID:

```bash
LOCALMIND_BENCH_DEVICE=<SIMULATOR_UDID> tools/benchmark/run_benchmark.sh
```

Reports are written to:
- `build/benchmark-reports/<timestamp>/results.json`
- `build/benchmark-reports/<timestamp>/summary.md`
- `build/benchmark-reports/<timestamp>/suggestions.md`
- `build/benchmark-reports/<timestamp>/detailed.md`

## Cache Behavior

Model downloads are reused from the local Cactus model directory. If a model is already downloaded, benchmark runs skip downloading it.

## Third Model Resolution

The benchmark uses this order:
1. `LOCALMIND_BENCH_THIRD_MODEL` env override (if set)
2. Any installed small model not in the primary two
3. Probe list in code (`BenchmarkModelCatalog.thirdModelProbeCandidates`)

If no third model can be resolved, the test fails with remediation instructions.

## Periodic Mode (launchd)

Install daily run (86400 seconds):

```bash
tools/benchmark/install_launchd_job.sh
```

Install with custom interval (example: every 6 hours):

```bash
tools/benchmark/install_launchd_job.sh 21600
```

Remove scheduled job:

```bash
tools/benchmark/uninstall_launchd_job.sh
```
