# LocalMind LLM Benchmark Detailed Report

- Generated: 2026-03-20T18:20:13.296766
- Fixture suite: v1.0.0
- Baseline: rule-based

## Model Metrics
| Model | Composite | Prompts | Classification | Task Fields | Note Fields | Valid JSON | Fallback | Timeout | Mean (ms) | P50 (ms) | P95 (ms) | Cache Hit |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| rule-based | 0.9648 | 20 | 0.95 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 2.1 | 2.0 | 3.0 | true |
| gemma3-1b | 0.7407 | 20 | 0.9 | 0.955 | 0.85 | 0.4 | 0.6 | 0.0 | 4719.4 | 5074.0 | 5903.0 | true |
| gemma3-270m | 0.701 | 20 | 0.8 | 0.94 | 1.0 | 0.5 | 0.5 | 0.0 | 1807.7 | 1823.0 | 2309.0 | true |
| qwen3-0.6 | 0.6562 | 20 | 0.75 | 0.895 | 0.98 | 0.5 | 0.5 | 0.0 | 4037.65 | 3865.0 | 5201.0 | true |

## Model: rule-based

### Error Breakdown
No errors.

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 2 | true | false |  |
| note_02 | note | note | true | 1.0 | 3 | true | false |  |
| note_03 | note | note | true | 1.0 | 3 | true | false |  |
| note_04 | note | note | true | 1.0 | 3 | true | false |  |
| note_05 | note | note | true | 1.0 | 2 | true | false |  |
| note_06 | note | note | true | 1.0 | 2 | true | false |  |
| note_07 | note | task | false | 1.0 | 2 | true | false |  |
| note_08 | note | note | true | 1.0 | 3 | true | false |  |
| note_09 | note | note | true | 1.0 | 3 | true | false |  |
| note_10 | note | note | true | 1.0 | 2 | true | false |  |
| task_01 | task | task | true | 1.0 | 5 | true | false |  |
| task_02 | task | task | true | 1.0 | 1 | true | false |  |
| task_03 | task | task | true | 1.0 | 1 | true | false |  |
| task_04 | task | task | true | 1.0 | 1 | true | false |  |
| task_05 | task | task | true | 1.0 | 2 | true | false |  |
| task_06 | task | task | true | 1.0 | 1 | true | false |  |
| task_07 | task | task | true | 1.0 | 1 | true | false |  |
| task_08 | task | task | true | 1.0 | 1 | true | false |  |
| task_09 | task | task | true | 1.0 | 2 | true | false |  |
| task_10 | task | task | true | 1.0 | 2 | true | false |  |

### Invalid JSON Raw Output Samples
No invalid JSON samples.

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 1.7 | 1.0 | 2.0 |
| note | 2.5 | 2.0 | 3.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 1.0 |
| medium | 16 | 2.25 | 2.0 | 3.0 | 0.0 | 0.0 | 0.9625 |
| long | 1 | 3.0 | 3.0 | 3.0 | 0.0 | 0.0 | 1.0 |

## Model: gemma3-1b

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 9 |
| Failed to decode JSON response: Unexpected JSON token at offset 18: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 69: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 21: Expected end of the object or comma at path: $ JS... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 5586 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 21: Expected end of the object or comma at path: $ JS... |
| note_02 | note | note | true | 1.0 | 5917 | false | true | No JSON object found in LLM response |
| note_03 | note | note | true | 1.0 | 5903 | false | true | No JSON object found in LLM response |
| note_04 | note | note | true | 1.0 | 3248 | true | false |  |
| note_05 | note | note | true | 1.0 | 5862 | false | true | No JSON object found in LLM response |
| note_06 | note | note | true | 0.5 | 3415 | true | false |  |
| note_07 | note | task | false | 1.0 | 5683 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 0.5 | 4732 | true | false |  |
| note_09 | note | note | true | 0.75 | 3111 | true | false |  |
| note_10 | note | note | true | 0.75 | 5295 | true | false |  |
| task_01 | task | task | true | 1.0 | 3143 | false | true | No JSON object found in LLM response |
| task_02 | task | task | true | 1.0 | 5196 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 18: Expected end of the object or comma at path: $ JS... |
| task_03 | task | task | true | 1.0 | 5796 | false | true | No JSON object found in LLM response |
| task_04 | task | note | false | 0.75 | 3688 | true | false |  |
| task_05 | task | task | true | 1.0 | 5103 | true | false |  |
| task_06 | task | task | true | 1.0 | 5823 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 1.0 | 2930 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 69: Expected end of the object or comma at path: $ JS... |
| task_08 | task | task | true | 1.0 | 4979 | false | true | No JSON object found in LLM response |
| task_09 | task | task | true | 1.0 | 5074 | false | true | No JSON object found in LLM response |
| task_10 | task | task | true | 0.8 | 3904 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | Failed to decode JSON response: Unexpected JSON token at offset 21: Expected end of the... |  | ```json {   "type":"note"   "title":"Sushi Place on Main Street - Amazing!" , 2026-03-20，   “the sushi place on Main Street was amazing definitely go back” ,... |  |
| note_02 | No JSON object found in LLM response |  | {"type": "note", "title": "Kotlin Corutinnes Structured Concurrency", 'body': "Kotlin Corutinnes Structured Concurrency prevents leaked jobs really well\n\nT... |  |
| note_03 | No JSON object found in LLM response |  | ```json { "type": "", "title": null, "body": "a plant watering sensor with raspberry pi would be a cool weekend project", "due_date": null,  // No due date s... |  |
| note_05 | No JSON object found in LLM response |  | ```json {   "type": "note", - "title": "Migrate to Postgres",   "- "body: The team agreed we should migrate to Postgres before Q3",   "- due_date": null,   "... |  |
| note_07 | No JSON object found in LLM response |  | ```json {     "type": "note",     "title": "simpleness is prerequisite for reliability",     "body":"book quote: simplicity is prerequisite for reliability",... |  |
| task_01 | No JSON object found in LLM response |  | ```json</h1>λ∂d=null∂t=null∂p=null∂d='2026-03-20' ∂l='task'</div><end_of_turn> |  |
| task_02 | Failed to decode JSON response: Unexpected JSON token at offset 18: Expected end of the... |  | ```json ago { status: task، title：book dentist appointment March 20 at 10am， body：book dentist appointment March 20 at 1 p m， due_date : "2026-年3月"， due_time... |  |
| task_03 | No JSON object found in LLM response |  | ```json {     "type": "note",     "title": null,  // Note is initially empty. To complete the JSON object, a title would be needed. Alternatively, one could ... |  |
| task_06 | No JSON object found in LLM response |  | ```json {   "type huzetype":[     "task"*   ],   “input_text”: “pay electricity bill in 3 days”,    “title”: “Pay electricity bill in 3 days” ,     “body”: "... |  |
| task_07 | Failed to decode JSON response: Unexpected JSON token at offset 69: Expected end of the... |  | ```json {"type": "note", "title": "submit quarterly report", short: submit quarterly report today before 5pm} null}<end_of_turn> |  |
| task_08 | No JSON object found in LLM response |  | ```json autobús-t1476079096811220** ```bubbleshade-gato-c1f7a0bf8a8b4fc3e2d58e2f3b6fa3c9  holiday   ```bubbleshow  holiday to bring peace to the children and... |  |
| task_09 | No JSON object found in LLM response |  | Rationale: The provided input text is a simple instruction to formulate a proposal for a client, focusing on meeting a deadline after tomorrow. It does not c... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 4563.6 | 4979.0 | 5796.0 |
| note | 4875.2 | 5295.0 | 5903.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 4849.6667 | 5796.0 | 5796.0 | 1.0 | 0.0 | 1.0 |
| medium | 16 | 4621.0 | 4979.0 | 5862.0 | 0.5 | 0.0 | 0.8762 |
| long | 1 | 5903.0 | 5903.0 | 5903.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 5796.0 | 1.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.1114 | 4979.0 | 2.0 | 0.5714 | 0.0 | Utility delta -0.1114 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.0667 | 5295.0 | 2.0 | 0.4444 | 0.0 | Utility delta -0.0667 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 5903.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: gemma3-270m

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 8 |
| Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':', but had ',' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 115: Expected colon ':', but had ',' instead at path:... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | task | false | 1.0 | 1384 | true | false |  |
| note_02 | note | note | true | 1.0 | 2330 | false | true | No JSON object found in LLM response |
| note_03 | note | note | true | 1.0 | 1633 | true | false |  |
| note_04 | note | task | false | 1.0 | 2100 | true | false |  |
| note_05 | note | task | false | 1.0 | 2240 | true | false |  |
| note_06 | note | note | true | 1.0 | 2261 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 115: Expected colon ':', but had ',' instead at path:... |
| note_07 | note | task | false | 1.0 | 1092 | true | false |  |
| note_08 | note | note | true | 1.0 | 759 | true | false |  |
| note_09 | note | note | true | 1.0 | 2275 | false | true | No JSON object found in LLM response |
| note_10 | note | note | true | 1.0 | 1879 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 1.0 | 1823 | true | false |  |
| task_02 | task | task | true | 1.0 | 1544 | false | true | No JSON object found in LLM response |
| task_03 | task | task | true | 1.0 | 1999 | true | false |  |
| task_04 | task | task | true | 1.0 | 1079 | false | true | No JSON object found in LLM response |
| task_05 | task | task | true | 0.8 | 1791 | true | false |  |
| task_06 | task | task | true | 1.0 | 2232 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 1.0 | 2300 | false | true | No JSON object found in LLM response |
| task_08 | task | task | true | 0.6 | 1472 | true | false |  |
| task_09 | task | task | true | 1.0 | 1652 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':', but had ',' instead at path:... |
| task_10 | task | task | true | 1.0 | 2309 | false | true | No JSON object found in LLM response |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_02 | No JSON object found in LLM response |  | ```json {   "name": "semantic_rule",   "type": "subject",   "title": "semantic_rule-about-request",   "body": null, // Error: String used instead of an objec... |  |
| note_06 | Failed to decode JSON response: Unexpected JSON token at offset 115: Expected colon ':'... |  | ```json {  "type": "subject",  "title": "shopping list summary of the morning routine by household from 2026-03-20." ,false,1.5,null,false,lead_date: 2026-08... |  |
| note_09 | No JSON object found in LLM response |  | ```json { "type": "task", "title": "travel thought", "body": "Travel thoughts are interesting and enjoyable," // Not necessary at this point as the core of t... |  |
| note_10 | No JSON object found in LLM response |  | ```json {   "format_string__": {"type"": "task"},   "title"": ""critical issue in rapid data extraction"", "", "", "" body":null,"due_date": null,"due_time":... |  |
| task_02 | No JSON object found in LLM response |  | The provided JSON object shows that the type is "task" or an explanation of a note, and no other keys are specified, except for type_hint being "text". Howev... |  |
| task_04 | No JSON object found in LLM response |  | ```json {"type":"task","title":"fix production database bug","body":"The issue causes the following problems on our servers:" negative"}<end_of_turn> |  |
| task_06 | No JSON object found in LLM response |  | The JSON object should have `style` = string, `title` = None, and `body` =  None. The due_date is null and due_time is null. For each element in the array of... |  |
| task_07 | No JSON object found in LLM response |  | ```json {  "type": "task",  "title": "quarterly report submission query",  "body": null, // Missing any number of lines inside this body ษδιο YYYY-MM-DDD 202... |  |
| task_09 | Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':'... |  | ```json { "type": "task", "title": "proposal to the client day after tomorrow", "body": "", "due_date":["2026-3-20"], "\"priority\": \"low\"", "tags\": [], "... |  |
| task_10 | No JSON object found in LLM response |  | ```json {   "type": {     "title": "Title of a presentation for an investor pitch in 8 weeks.",     "body": null, // Should be completely blank or close to i... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 1820.1 | 1791.0 | 2300.0 |
| note | 1795.3 | 1879.0 | 2275.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 2177.0 | 2232.0 | 2232.0 | 0.6667 | 0.0 | 1.0 |
| medium | 16 | 1749.375 | 1791.0 | 2309.0 | 0.5 | 0.0 | 0.835 |
| long | 1 | 1633.0 | 1633.0 | 1633.0 | 0.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 2232.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.0343 | 1652.0 | 2.0 | 0.5714 | 0.0 | Utility delta -0.0343 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.2 | 2100.0 | 2.0 | 0.4444 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 1633.0 | 3.0 | 0.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 4 |
| Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 74: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 33: Expected colon ':', but had '"' instead at path: ... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':', but had 'EOF' instead at pat... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 8: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 172: Expected colon ':', but had ',' instead at path:... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 5369 | false | true | No JSON object found in LLM response |
| note_02 | note | task | false | 0.8 | 5201 | true | false |  |
| note_03 | note | note | true | 1.0 | 3788 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':', but had 'EOF' instead at pat... |
| note_04 | note | task | false | 1.0 | 4605 | true | false |  |
| note_05 | note | note | true | 1.0 | 4657 | true | false |  |
| note_06 | note | note | true | 1.0 | 4925 | true | false |  |
| note_07 | note | task | false | 1.0 | 3659 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 8: Expected colon ':', but had 'EOF' instead at path:... |
| note_08 | note | note | true | 1.0 | 2682 | true | false |  |
| note_09 | note | task | false | 1.0 | 4732 | true | false |  |
| note_10 | note | note | true | 1.0 | 4571 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 172: Expected colon ':', but had ',' instead at path:... |
| task_01 | task | task | true | 1.0 | 4047 | true | false |  |
| task_02 | task | task | true | 1.0 | 2704 | false | true | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... |
| task_03 | task | task | true | 1.0 | 5110 | false | true | No JSON object found in LLM response |
| task_04 | task | note | false | 0.75 | 3865 | true | false |  |
| task_05 | task | task | true | 1.0 | 3576 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 74: Expected end of the object or comma at path: $ JS... |
| task_06 | task | task | true | 1.0 | 3494 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 0.6 | 3991 | true | false |  |
| task_08 | task | task | true | 0.6 | 2937 | true | false |  |
| task_09 | task | task | true | 1.0 | 3484 | false | true | No JSON object found in LLM response |
| task_10 | task | task | true | 1.0 | 3356 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 33: Expected colon ':', but had '"' instead at path: ... |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | No JSON object found in LLM response |  | <think>  </think>   ```json {   "type": "note",   "title": "the sushi place on Main Street was amazing definitely go back",   "body": ". The sushi place on M... |  |
| note_03 | Failed to decode JSON response: Unexpected JSON token at offset 133: Expected colon ':'... |  | <think>  </think>      {   "type": "task",   "title": "",   "body": "",   "due_date": null,   "due_time": null,   "priority": "",   "tags":[], "confidence" }... |  |
| note_07 | Failed to decode JSON response: Unexpected JSON token at offset 8: Expected colon ':', ... |  | <think>  </think>  {"type":"","title":"","body":[],"due_date":"2026-03-21","due_time":{"18:45"},"priority":"medium","tags":["book"],"confidence":0.95}<\|im_e... |  |
| note_10 | Failed to decode JSON response: Unexpected JSON token at offset 172: Expected colon ':'... |  | <think>  </think>   {"type":"note","title":"retro note: focus on reducing friction in quick capture flow","body":"focus on reducing friction in quick capture... |  |
| task_02 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... |  | <think>  </think> {  	"type":"","title":"","body":[],  	due_date:null, due_time:null,  	priority:"medium", tags:[], confidence:0.0 }<\|im_end\|> |  |
| task_03 | No JSON object found in LLM response |  | <think>  </think>   {"type"": ""note"", "title"": ""pick up groceries this weekend"", "body"": ""pick up groceries this weekend"", "due-date"\": \"2026-03-20... |  |
| task_05 | Failed to decode JSON response: Unexpected JSON token at offset 74: Expected end of the... |  | <think>  </think>  {   type: "",   title: "",   body: input_text,   due_date: today_date.format("YYYY-MM-DD"),   due_time: today_date.format("HH:mm"),   prio... |  |
| task_06 | No JSON object found in LLM response |  | <think>  </think> { 	"type":"", 	"title":[], 	"body":[], 	due-date":["2026-03-20"], 	due-time:null, 	priority:"medium", 	tags:[], 	confidence:.0 }<\|im_end\|> |  |
| task_09 | No JSON object found in LLM response |  | <think>  </think>  { 	"type":"" task", 	"title":"email the proposal to the client day after tomorrow", 	"body":"", 	due_date:"", 	due_time:"", 	priority:"med... |  |
| task_10 | Failed to decode JSON response: Unexpected JSON token at offset 33: Expected colon ':',... |  | <\|im_start\|>  </think>  {"type":"","title":"","body":[],("due_date","2026-13-14"),("due_time",null),("priority": "medium"), ("tags":[],"confidence":1.0}<\|... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 3656.4 | 3494.0 | 4047.0 |
| note | 4418.9 | 4605.0 | 5201.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 4198.3333 | 3991.0 | 3991.0 | 0.6667 | 0.0 | 0.9467 |
| medium | 16 | 4023.125 | 3865.0 | 5201.0 | 0.4375 | 0.0 | 0.7912 |
| long | 1 | 3788.0 | 3788.0 | 3788.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | -0.0533 | 3991.0 | 1.0 | 0.6667 | 0.0 | Utility delta -0.0533 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.1229 | 3484.0 | 2.0 | 0.5714 | 0.0 | Utility delta -0.1229 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.2089 | 4657.0 | 2.0 | 0.3333 | 0.0 | Utility delta -0.2089 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 3788.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
