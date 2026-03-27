# LocalMind Routing Recommendations

- Generated: 2026-03-20T16:28:54.340911
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
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 10298.6667 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.9429 | 1.0 | -0.0571 | 8213.2857 | 1.5714 | 0.2857 | 0.0 | Utility delta -0.0571 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.8667 | 0.9333 | -0.0667 | 8072.3333 | 2.4444 | 0.7778 | 0.0 | Utility delta -0.0667 below threshold +0.03 |
| note/long | rule-based | 1 | 0.4 | 1.0 | -0.6 | 7559.0 | 3.0 | 0.0 | 0.0 | Utility delta -0.6 below threshold +0.03 |

## Model: gemma3-270m

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 3079.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.9657 | 1.0 | -0.0343 | 2223.0 | 1.5714 | 0.5714 | 0.0 | Utility delta -0.0343 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.7333 | 0.9333 | -0.2 | 3136.2222 | 2.4444 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 4829.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.8 | 1.0 | -0.2 | 7109.3333 | 1.0 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.6286 | 1.0 | -0.3714 | 6710.7143 | 1.5714 | 0.1429 | 0.0 | Utility delta -0.3714 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.6267 | 0.9333 | -0.3067 | 8085.0 | 2.4444 | 0.2222 | 0.0 | Utility delta -0.3067 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 2664.0 | 3.0 | 0.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

