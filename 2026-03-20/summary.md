# LocalMind LLM Benchmark

- Generated: 2026-03-20T16:28:54.340911
- Fixture suite: v1.0.0
- Baseline: rule-based

## Winner
rule-based (composite=0.9648)

## Ranking
| Rank | Model | Composite | Classification | Task Fields | Note Fields | Fallback | P50 (ms) | Cache Hit |
|---|---|---:|---:|---:|---:|---:|---:|---|
| 1 | rule-based | 0.9648 | 0.95 | 1.0 | 1.0 | 0.0 | 2.0 | true |
| 2 | gemma3-1b | 0.725 | 0.85 | 0.9 | 1.0 | 0.55 | 7929.0 | true |
| 3 | gemma3-270m | 0.686 | 0.8 | 0.94 | 1.0 | 0.65 | 2729.0 | true |
| 4 | qwen3-0.6 | 0.504 | 0.5 | 0.95 | 0.91 | 0.25 | 7233.0 | true |

## Routing Recommendations
| Model | Strategy | LLM Coverage | Utility Delta vs Rule | Mean Latency Delta (ms) | Summary |
|---|---|---:|---:|---:|---|
| gemma3-1b | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
| gemma3-270m | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
| qwen3-0.6 | rule_only | 0.0 | 0.0 | 0.0 | Keep all prompts on rule-based parser for this model. |
