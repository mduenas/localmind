# LocalMind Routing Recommendations

- Generated: 2026-03-20T18:20:13.296766
- Baseline: rule-based

## Model: gemma3-1b

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 4849.6667 | 1.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.8886 | 1.0 | -0.1114 | 4441.0 | 2.0 | 0.5714 | 0.0 | Utility delta -0.1114 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.8667 | 0.9333 | -0.0667 | 4761.0 | 2.4444 | 0.4444 | 0.0 | Utility delta -0.0667 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 5903.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: gemma3-270m

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 2177.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.9657 | 1.0 | -0.0343 | 1667.1429 | 2.0 | 0.5714 | 0.0 | Utility delta -0.0343 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.7333 | 0.9333 | -0.2 | 1813.3333 | 2.4444 | 0.4444 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 1633.0 | 3.0 | 0.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.9467 | 1.0 | -0.0533 | 4198.3333 | 1.0 | 0.6667 | 0.0 | Utility delta -0.0533 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.8771 | 1.0 | -0.1229 | 3424.1429 | 2.0 | 0.5714 | 0.0 | Utility delta -0.1229 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.7244 | 0.9333 | -0.2089 | 4489.0 | 2.4444 | 0.3333 | 0.0 | Utility delta -0.2089 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 3788.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

