# LocalMind Routing Recommendations

- Generated: 2026-03-20T16:46:33.489589
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
| task/short | rule-based | 3 | 0.8 | 1.0 | -0.2 | 5789.6667 | 1.0 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.8029 | 1.0 | -0.1971 | 4820.1429 | 1.8571 | 0.5714 | 0.0 | Utility delta -0.1971 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.8444 | 0.9333 | -0.0889 | 4276.2222 | 2.4444 | 0.7778 | 0.0 | Utility delta -0.0889 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 6005.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: gemma3-270m

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 1936.0 | 1.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.9771 | 1.0 | -0.0229 | 2137.5714 | 1.8571 | 0.7143 | 0.0 | Utility delta -0.0229 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.8 | 0.9333 | -0.1333 | 2481.6667 | 2.4444 | 0.6667 | 0.0 | Utility delta -0.1333 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 3017.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

- Strategy: rule_only
- LLM coverage: 0.0
- Utility delta vs rule: 0.0
- Mean latency delta vs rule (ms): 0.0
- Thresholds: minUtilityGain=0.03, maxFallbackRate=0.2, maxTimeoutRate=0.1, maxP50LatencyMs=4500.0

Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 1.0 | 1.0 | 0.0 | 4047.3333 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | 0.8657 | 1.0 | -0.1343 | 3880.4286 | 1.8571 | 0.5714 | 0.0 | Utility delta -0.1343 below threshold +0.03 |
| note/medium | rule-based | 9 | 0.7133 | 0.9333 | -0.22 | 3430.5556 | 2.4444 | 0.5556 | 0.0 | Utility delta -0.22 below threshold +0.03 |
| note/long | rule-based | 1 | 1.0 | 1.0 | 0.0 | 4653.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

