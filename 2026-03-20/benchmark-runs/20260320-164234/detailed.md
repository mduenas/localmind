# LocalMind LLM Benchmark Detailed Report

- Generated: 2026-03-20T16:46:33.489589
- Fixture suite: v1.0.0
- Baseline: rule-based

## Model Metrics
| Model | Composite | Prompts | Classification | Task Fields | Note Fields | Valid JSON | Fallback | Timeout | Mean (ms) | P50 (ms) | P95 (ms) | Cache Hit |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| rule-based | 0.9648 | 20 | 0.95 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 2.05 | 2.0 | 3.0 | true |
| gemma3-270m | 0.714 | 20 | 0.85 | 0.96 | 1.0 | 0.25 | 0.75 | 0.0 | 2306.15 | 2217.0 | 3127.0 | true |
| qwen3-0.6 | 0.6455 | 20 | 0.75 | 0.915 | 0.955 | 0.4 | 0.6 | 0.0 | 3741.65 | 3596.0 | 4653.0 | true |
| gemma3-1b | 0.6407 | 20 | 0.75 | 0.955 | 0.95 | 0.3 | 0.7 | 0.0 | 4780.05 | 3817.0 | 7421.0 | true |

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
| task_09 | task | task | true | 1.0 | 1 | true | false |  |
| task_10 | task | task | true | 1.0 | 2 | true | false |  |

### Invalid JSON Raw Output Samples
No invalid JSON samples.

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 1.6 | 1.0 | 2.0 |
| note | 2.5 | 2.0 | 3.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 1.0 |
| medium | 16 | 2.1875 | 2.0 | 3.0 | 0.0 | 0.0 | 0.9625 |
| long | 1 | 3.0 | 3.0 | 3.0 | 0.0 | 0.0 | 1.0 |

## Model: gemma3-270m

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 8 |
| Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 22: Expected colon ':', but had '"' instead at path: ... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 62: EOF at path: $ JSON input: {  'type': "task",  "t... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 18: Expected colon ':', but had 'n' instead at path: ... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 57: Expected colon ':', but had 't' instead at path: ... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 288: Expected colon ':', but had '"' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 162: Expected end of the object or comma at path: $ J... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 1454 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 18: Expected colon ':', but had 'n' instead at path: ... |
| note_02 | note | note | true | 1.0 | 2401 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 57: Expected colon ':', but had 't' instead at path: ... |
| note_03 | note | note | true | 1.0 | 3017 | false | true | No JSON object found in LLM response |
| note_04 | note | note | true | 1.0 | 2141 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 288: Expected colon ':', but had '"' instead at path:... |
| note_05 | note | task | false | 1.0 | 3217 | true | false |  |
| note_06 | note | note | true | 1.0 | 3127 | false | true | No JSON object found in LLM response |
| note_07 | note | task | false | 1.0 | 2012 | true | false |  |
| note_08 | note | task | false | 1.0 | 3071 | true | false |  |
| note_09 | note | note | true | 1.0 | 2695 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 162: Expected end of the object or comma at path: $ J... |
| note_10 | note | note | true | 1.0 | 2217 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 0.6 | 2985 | true | false |  |
| task_02 | task | task | true | 1.0 | 856 | true | false |  |
| task_03 | task | task | true | 1.0 | 1137 | false | true | No JSON object found in LLM response |
| task_04 | task | task | true | 1.0 | 2692 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... |
| task_05 | task | task | true | 1.0 | 1622 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 22: Expected colon ':', but had '"' instead at path: ... |
| task_06 | task | task | true | 1.0 | 1755 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 1.0 | 2916 | false | true | No JSON object found in LLM response |
| task_08 | task | task | true | 1.0 | 2111 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 62: EOF at path: $ JSON input: {  'type': "task",  "t... |
| task_09 | task | task | true | 1.0 | 1589 | false | true | No JSON object found in LLM response |
| task_10 | task | task | true | 1.0 | 3108 | false | true | No JSON object found in LLM response |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | Failed to decode JSON response: Unexpected JSON token at offset 18: Expected colon ':',... |  | ```json {"type": true, "" name": null, """title": "San Francisco Bay Area on a Friday evening scene"}, {"title": "Sushi Place on Main Street (Food Fantasiya)... |  |
| note_02 | Failed to decode JSON response: Unexpected JSON token at offset 57: Expected colon ':',... |  | ```json { "type":"task", "title":"Requests for summarization", # title is unclear in the provided input text but it indicates `task` type.   For instance, we... |  |
| note_03 | No JSON object found in LLM response |  | ```json {   "type": "string",   "title": "plant watering sensor with raspberry pi would be a cool weekend project",   "body": null,   "due_date": null,   "?d... |  |
| note_04 | Failed to decode JSON response: Unexpected JSON token at offset 288: Expected colon ':'... |  | ```json { "type": "task", title: "Atomic Habits has great advice on habit stacking really insightful", body: "Atomic Habits has great advice on habit stackin... |  |
| note_06 | No JSON object found in LLM response |  | ```json {   "type": "task",   "title": null,   "body": null,   "due_date": null,   "due_time": null,   "priority": "medium", // default medium - hard to tell... |  |
| note_09 | Failed to decode JSON response: Unexpected JSON token at offset 162: Expected end of th... |  | ```json {     "type": "product",     "title": "Boise in Early Fall (YYYY-MM-DD/null)",     "body": "Travel thought about Boise in early fall has perfect runn... |  |
| note_10 | No JSON object found in LLM response |  | ```json {("type": "task", title:"request to do / reminder / schedule / update -  Request to complete a task on March 20th at approximately 18:00. Please prov... |  |
| task_03 | No JSON object found in LLM response |  | ```json {   "type": "task",   "title": null,\n"body":null,\n"due_date": null,\n"}<end_of_turn> |  |
| task_04 | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... |  | ```json { ... } --> {'type":"task', 'title': \"fix production database bug\", \"body\": null, \"due\_date\": null, \"due\_time\": null , \"priority\":\"mediu... |  |
| task_05 | Failed to decode JSON response: Unexpected JSON token at offset 22: Expected colon ':',... |  | ```json {"type": "task", title": "meeting summary", body": "team standup meeting next week at 9:30am, due_date = "2026-3-21", due_time = null, priority": "me... |  |
| task_06 | No JSON object found in LLM response |  | One JSON object containing only two fields: type, title, body, due_date, due_time and priority. All keys are same from any string. However all the values wil... |  |
| task_07 | No JSON object found in LLM response |  | {   "type": "task",   "title": "",   "body": "",   "?due_date": null , get_due_date() ? true, show requested by date at 2026-03-20 & get valid time format ? ... |  |
| task_08 | Failed to decode JSON response: Unexpected JSON token at offset 62: EOF at path: $ JSON... |  | ```json {  'type': "task",  "title": "recheck task" ,  "body": "" ,  "// Note: The content requires only one input text to understand." },  """   ... except ... |  |
| task_09 | No JSON object found in LLM response |  | ```json {"type":"task","title":"Proposal Request to Client","body":"Please submit your proposal by Friday, March 20, 2026.", due_date": null,"due_time": null... |  |
| task_10 | No JSON object found in LLM response |  | ```json { "type":"", "title":"","body":null,"due_date":null,"due_time":null ,"priority":{"medium","default":"", "tags":[], "confidence"":0.5} Required keys (... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 2077.1 | 1755.0 | 2985.0 |
| note | 2535.2 | 2401.0 | 3127.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 1936.0 | 1755.0 | 1755.0 | 1.0 | 0.0 | 1.0 |
| medium | 16 | 2331.125 | 2217.0 | 3127.0 | 0.6875 | 0.0 | 0.8775 |
| long | 1 | 3017.0 | 3017.0 | 3017.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 1755.0 | 1.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.0229 | 2111.0 | 1.0 | 0.7143 | 0.0 | Utility delta -0.0229 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.1333 | 2401.0 | 2.0 | 0.6667 | 0.0 | Utility delta -0.1333 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 3017.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 5 |
| Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 33: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 100: Expected colon ':', but had 'EOF' instead at pat... | 1 |
| Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 106: Expected colon ':', but had 'd' instead at path:... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | task | false | 1.0 | 3877 | true | false |  |
| note_02 | note | task | false | 1.0 | 3513 | true | false |  |
| note_03 | note | note | true | 1.0 | 4653 | false | true | No JSON object found in LLM response |
| note_04 | note | note | true | 1.0 | 3596 | false | true | No JSON object found in LLM response |
| note_05 | note | note | true | 1.0 | 2503 | false | true | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... |
| note_06 | note | task | false | 0.8 | 4201 | true | false |  |
| note_07 | note | task | false | 1.0 | 2887 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 1.0 | 2997 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 106: Expected colon ':', but had 'd' instead at path:... |
| note_09 | note | note | true | 0.75 | 4223 | true | false |  |
| note_10 | note | note | true | 1.0 | 3078 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 1.0 | 3560 | false | true | No JSON object found in LLM response |
| task_02 | task | task | true | 1.0 | 3919 | false | true | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... |
| task_03 | task | task | true | 1.0 | 4073 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 33: Expected end of the object or comma at path: $ JS... |
| task_04 | task | note | false | 0.75 | 3860 | true | false |  |
| task_05 | task | task | true | 1.0 | 2750 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 100: Expected colon ':', but had 'EOF' instead at pat... |
| task_06 | task | task | true | 1.0 | 3904 | true | false |  |
| task_07 | task | task | true | 1.0 | 4165 | false | true | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... |
| task_08 | task | task | true | 0.6 | 3125 | true | false |  |
| task_09 | task | task | true | 1.0 | 6407 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', but had 'EOF' instead at path:... |
| task_10 | task | task | true | 0.8 | 3542 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_03 | No JSON object found in LLM response |  | <think>  </think>  {   "type":"",   "title":"",   "body":[     {       "due_date":"2026-03-20",       "due_time":"null",       "priority\":\"medium\",       ... |  |
| note_04 | No JSON object found in LLM response |  | <think> kiện  ```json { 	"type": "", 	"title": "", 	"body": "", 	"due_date": null, 	"due_time": null, 	priority: \"medium\", 	tags:[], 	confidence:"", } ```<... |  |
| note_05 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... |  | <think>  </think>  {"type":"","title":"","body":[],"due_date":["","","",""],"due_time":["","","",""],"priority":"medium",tags:[],confidence:.}<\|im_end\|> |  |
| note_07 | No JSON object found in LLM response |  | <think>  </think>  { 	"type": "", 	"title": "", 	"body": null, 	"due_date": null, "due_time"": "" , 	priority"": "" , tags: [], -confidence: NaN }<\|im_end\|> |  |
| note_08 | Failed to decode JSON response: Unexpected JSON token at offset 106: Expected colon ':'... |  | <think>  </think>   {"type":"","title":"","body":[["customer interview takeaway: onboarding confusion around reminders"]],Due date:null,Due time:null,"priori... |  |
| note_10 | No JSON object found in LLM response |  | <think>  </think>  {"type": "", "title": null, "body": null, "@due_date ": null, "@due_time ": null, "$priority".key_value_mapping.1 : media", tags: [], conf... |  |
| task_01 | No JSON object found in LLM response |  | <think>  </think><\|endoftext\|>###{'type': 'task', 'title': 'Remind me to call mom tomorrow at 6pm', 'body': null, 'due_date': null, "due_time"": null, "pri... |  |
| task_02 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... |  | ())) <\|im_start\|>   { 	"type":"", 	"title":[ 	"book dentist appointment March 20 at 10am" 	], 	body:"", 	due_date:"", 	due_time:"", 	priority:"",// medium,... |  |
| task_03 | Failed to decode JSON response: Unexpected JSON token at offset 33: Expected end of the... |  | <think>  </think>  ```json {     type: "note",     title: "",     body: {         input_text:             pick up groceries this weekend,     },     due_date... |  |
| task_05 | Failed to decode JSON response: Unexpected JSON token at offset 100: Expected colon ':'... |  | <think>  </think>  {"type":"","title":"","body":[],"due-date":null,"due-time":null,"priority":"","tags":[],"confidence"}<\|im_end\|> |  |
| task_07 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... |  | <think>  </think>   {"type":"task","title":"submit quarterly report today before5pm","body":["submit quarterly report today before5pm"],"due_date":"2026-03-2... |  |
| task_09 | Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', ... |  | <think>  </think>      ```json {   "type": "task",   "title": "email the proposal to the client day after tomorrow",   "body\": \"email the proposal to the c... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 3930.5 | 3860.0 | 4165.0 |
| note | 3552.8 | 3513.0 | 4223.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 4047.3333 | 4073.0 | 4073.0 | 0.6667 | 0.0 | 1.0 |
| medium | 16 | 3627.375 | 3542.0 | 4223.0 | 0.5625 | 0.0 | 0.78 |
| long | 1 | 4653.0 | 4653.0 | 4653.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 4073.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.1343 | 3560.0 | 1.0 | 0.5714 | 0.0 | Utility delta -0.1343 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.22 | 3513.0 | 2.0 | 0.5556 | 0.0 | Utility delta -0.22 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 4653.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: gemma3-1b

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 9 |
| Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 40: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 208: Expected colon ':', but had ',' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 122: Expected end of the object or comma at path: $ J... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 1997 | false | true | No JSON object found in LLM response |
| note_02 | note | note | true | 1.0 | 7686 | false | true | No JSON object found in LLM response |
| note_03 | note | note | true | 1.0 | 6005 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 208: Expected colon ':', but had ',' instead at path:... |
| note_04 | note | note | true | 0.5 | 2715 | true | false |  |
| note_05 | note | note | true | 1.0 | 3725 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 122: Expected end of the object or comma at path: $ J... |
| note_06 | note | task | false | 1.0 | 3817 | true | false |  |
| note_07 | note | task | false | 1.0 | 3680 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 1.0 | 2494 | false | true | No JSON object found in LLM response |
| note_09 | note | note | true | 1.0 | 7247 | false | true | No JSON object found in LLM response |
| note_10 | note | note | true | 1.0 | 5125 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 1.0 | 5014 | false | true | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serialized body of string at element... |
| task_02 | task | task | true | 1.0 | 7421 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 40: Expected end of the object or comma at path: $ JS... |
| task_03 | task | task | true | 1.0 | 7185 | false | true | No JSON object found in LLM response |
| task_04 | task | note | false | 0.75 | 2336 | true | false |  |
| task_05 | task | task | true | 1.0 | 7116 | false | true | No JSON object found in LLM response |
| task_06 | task | task | true | 1.0 | 6711 | false | true | No JSON object found in LLM response |
| task_07 | task | note | false | 1.0 | 3473 | true | false |  |
| task_08 | task | task | true | 1.0 | 5833 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the object or comma at path: $ JS... |
| task_09 | task | note | false | 1.0 | 2859 | true | false |  |
| task_10 | task | task | true | 0.8 | 3162 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | No JSON object found in LLM response |  | Nearby restaurants offer delicious sushi and definitely go back!<end_of_turn> |  |
| note_02 | No JSON object found in LLM response |  | {“type”: “note”, “title”: “Kotlin coroutines for structured concurrency”, “body”: „Structured concurrency simplifies development with coroutines, allowing yo... |  |
| note_03 | Failed to decode JSON response: Unexpected JSON token at offset 208: Expected colon ':'... |  | ```json {   "type": "note",   "title": "",   "body": "'a plant watering sensor with raspberry pi would be a cool weekend project\nBuild the final JSON in one... |  |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 122: Expected end of th... |  | ```json {   “type”: “task”,   “title”: “migrate to Postgres”,   “body”: “the team agreed we should migrate to Postgres before Q3”，，，，“build the final JSON in... |  |
| note_07 | No JSON object found in LLM response |  | ```json {  "type":"note"  "title":"simple is prerequisite for reliability"  "body":"book quote: simplicity is prerequisite for reliability”  } ```їнstant َدह... |  |
| note_08 | No JSON object found in LLM response |  | ```json {"type":"note","title":"","body:"much onboarding confusion around reminders"}`</empty>`<end_of_turn> |  |
| note_09 | No JSON object found in LLM response |  | {   "type ":"task"" ,  # Removed this line as it was not needed and unnecessary. The output format required exactly one JSON object. Now, the response will b... |  |
| note_10 | No JSON object found in LLM response |  | ```json {   "type": "note", 7202, 100, "priority": "medium",   "tags":[], 0.95, false46873798469123456789012, null255567839431289"   }<end_of_turn> |  |
| task_01 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... |  | ```json {   "type": "",    "title": "[remind me to call mom tomorrow at 6pm]",    "body":"I need to call my mother tomorrow at 6 PM.",    "due_date":["2026-0... |  |
| task_02 | Failed to decode JSON response: Unexpected JSON token at offset 40: Expected end of the... |  | ```json {     utl: { type_hint: task, input_text: 'book dentist appointment March 20 at 10am' } ,     title: "Dentist Appointment Request",     body:"Please ... |  |
| task_03 | No JSON object found in LLM response |  | ```json { 	"type": "note", 	"title": "Pick up groceries this weekend", 	"body": "Pick up groceries this weekend.", 	"due_date": null, 	"due_time": null, // o... |  |
| task_05 | No JSON object found in LLM response |  | ```json {   "type": "task", 	"title": "schedule team standup meeting next week at 9:30am",   "body": "Schedule team standup meeting next week at 9:30am",    ... |  |
| task_06 | No JSON object found in LLM response |  | ```json {     "type": "note",     “title”: “Pay electricity bill in 3 days”,     “body”: “Pay electricity bill in 3 days”,      “due_date”: null,      “due_t... |  |
| task_08 | Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the... |  | ```json  {   'type': 'note',   'title': 'clean out the garage no rush #home',   'body': 'Clean out the garage. It’s good to do this so I won’t be stuck with ... |  |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 5111.0 | 5014.0 | 7185.0 |
| note | 4449.1 | 3725.0 | 7247.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 5789.6667 | 6711.0 | 6711.0 | 0.6667 | 0.0 | 0.8 |
| medium | 16 | 4514.1875 | 3725.0 | 7421.0 | 0.6875 | 0.0 | 0.8262 |
| long | 1 | 6005.0 | 6005.0 | 6005.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | -0.2 | 6711.0 | 1.0 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.1971 | 5014.0 | 1.0 | 0.5714 | 0.0 | Utility delta -0.1971 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.0889 | 3725.0 | 2.0 | 0.7778 | 0.0 | Utility delta -0.0889 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 6005.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
