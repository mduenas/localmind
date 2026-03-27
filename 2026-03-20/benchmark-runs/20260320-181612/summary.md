# LocalMind LLM Benchmark

- Generated: 2026-03-20T18:20:13.296766
- Fixture suite: v1.0.0
- Baseline: rule-based

## Winner
rule-based (composite=0.9648)

## Ranking
| Rank | Model | Composite | Classification | Task Fields | Note Fields | Fallback | P50 (ms) | Cache Hit |
|---|---|---:|---:|---:|---:|---:|---:|---|
| 1 | rule-based | 0.9648 | 0.95 | 1.0 | 1.0 | 0.0 | 2.0 | true |
| 2 | gemma3-1b | 0.7407 | 0.9 | 0.955 | 0.85 | 0.6 | 5074.0 | true |
| 3 | gemma3-270m | 0.701 | 0.8 | 0.94 | 1.0 | 0.5 | 1823.0 | true |
| 4 | qwen3-0.6 | 0.6562 | 0.75 | 0.895 | 0.98 | 0.5 | 3865.0 | true |

## Routing Recommendations
| Model | Strategy | LLM Coverage | Utility Delta vs Rule | Mean Latency Delta (ms) | Summary |
|---|---|---:|---:|---:|---|
| gemma3-1b | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
| gemma3-270m | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
| qwen3-0.6 | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
