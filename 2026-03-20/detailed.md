# LocalMind LLM Benchmark Detailed Report

- Generated: 2026-03-20T16:28:54.340911
- Fixture suite: v1.0.0
- Baseline: rule-based

## Model Metrics
| Model | Composite | Prompts | Classification | Task Fields | Note Fields | Valid JSON | Fallback | Timeout | Mean (ms) | P50 (ms) | P95 (ms) | Cache Hit |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| rule-based | 0.9648 | 20 | 0.95 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 1.95 | 2.0 | 3.0 | true |
| gemma3-1b | 0.725 | 20 | 0.85 | 0.9 | 1.0 | 0.45 | 0.55 | 0.0 | 8429.95 | 7929.0 | 11973.0 | true |
| gemma3-270m | 0.686 | 20 | 0.8 | 0.94 | 1.0 | 0.35 | 0.65 | 0.0 | 2892.65 | 2729.0 | 4949.0 | true |
| qwen3-0.6 | 0.504 | 20 | 0.5 | 0.95 | 0.91 | 0.75 | 0.25 | 0.0 | 7186.6 | 7233.0 | 11440.0 | true |

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
| task_01 | task | task | true | 1.0 | 3 | true | false |  |
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
| task | 1.4 | 1.0 | 2.0 |
| note | 2.5 | 2.0 | 3.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 1.0 | 1.0 | 1.0 | 0.0 | 0.0 | 1.0 |
| medium | 16 | 2.0625 | 2.0 | 3.0 | 0.0 | 0.0 | 0.9625 |
| long | 1 | 3.0 | 3.0 | 3.0 | 0.0 | 0.0 | 1.0 |

## Model: gemma3-1b

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 7 |
| Failed to decode JSON response: Unexpected JSON token at offset 55: Trailing comma before the end of JSON object at p... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 193: Expected colon ':', but had ',' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 136: Expected colon ':', but had ',' instead at path:... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 10525 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 193: Expected colon ':', but had ',' instead at path:... |
| note_02 | note | note | true | 1.0 | 8666 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the object or comma at path: $ JS... |
| note_03 | note | task | false | 1.0 | 7559 | true | false |  |
| note_04 | note | note | true | 1.0 | 10804 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 136: Expected colon ':', but had ',' instead at path:... |
| note_05 | note | note | true | 1.0 | 7483 | false | true | No JSON object found in LLM response |
| note_06 | note | note | true | 1.0 | 7929 | false | true | No JSON object found in LLM response |
| note_07 | note | task | false | 1.0 | 4503 | true | false |  |
| note_08 | note | task | false | 1.0 | 5779 | true | false |  |
| note_09 | note | note | true | 1.0 | 11797 | false | true | No JSON object found in LLM response |
| note_10 | note | note | true | 1.0 | 5165 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 1.0 | 4007 | true | false |  |
| task_02 | task | task | true | 1.0 | 11048 | true | false |  |
| task_03 | task | task | true | 1.0 | 11928 | false | true | No JSON object found in LLM response |
| task_04 | task | task | true | 0.6 | 7558 | true | false |  |
| task_05 | task | task | true | 1.0 | 6288 | true | false |  |
| task_06 | task | task | true | 1.0 | 10940 | true | false |  |
| task_07 | task | task | true | 1.0 | 8028 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 55: Trailing comma before the end of JSON object at p... |
| task_08 | task | task | true | 1.0 | 13619 | false | true | No JSON object found in LLM response |
| task_09 | task | task | true | 1.0 | 11973 | false | true | No JSON object found in LLM response |
| task_10 | task | task | true | 0.4 | 3000 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 193: Expected colon ':'... | ```json {   “title”: “Sushi Place on Main Street – Review”,   “body”: “The sushi place on Main Street was amazing. Definitely go back.” ,\n\nreasoning:\n - d... | ```json {   "title": "Sushi Place on Main Street",    "due_date": "2026-03-20",    "priority": "medium",    "tags": [["food", "restaurant"], ["main street"]]... |
| note_02 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 39: Expected end of the... | Suggestion: 5pm-19:59pm<end_of_turn> | ```json {    ”title”: “structured concurrency”、“    ”priority”: “high”,    ”tags”: null,    :”Kotlin coroutines、structured concurrency 、 prevents leaked jobs... |
| note_04 | Failed to decode JSON response: Unexpected JSON token at offset 89: Trailing comma befo... | Failed to decode JSON response: Unexpected JSON token at offset 136: Expected colon ':'... | ```json {   "priority": "medium",   "tags": [     "habit stacking",     "productivity",      // Added for context and potential further analysis if necessary... | ```json {   "type": "task",   "due_date": null,   "priority": "medium",    ”Atomic Habits has great advice on habit stacking really insightful” ,     “Previo... |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 78: Expected end of the... | No JSON object found in LLM response | ```json {   "title": "Team Discussion - Postgres Migration", closeToQ3 : null, 9:00->"18:00\"", priority="medium", tags=["migrate","postgres", "team"] , conf... | ```json {  	"due_date": "\"21st March 29th\""	,  	"due_time": "\"18:45\"",  	"priority": "high","   } ```<end_of_turn> |
| note_06 | No JSON object found in LLM response | No JSON object found in LLM response | null<end_of_turn> | ```json {   “title”: “grocery history list synced by household”,   “body”: “One-tap grocery history list synced by household”,   “due_date”: null, // No due ... |
| note_09 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {   "priority": "medium",   "tags": [ "travel", ". autumn" ],   "due_date": null, // or "YYYY-MM-DD" if needed for specific dates. Default is 2026-03... | ```json {   "title": "Travel Thought: Boise in Early Fall",   "body": "Boise in early fall has perfect running weather",   "due_date": null,    "due_time": n... |
| note_10 | Failed to decode JSON response: Unexpected JSON token at offset 8: Expected colon ':', ... | No JSON object found in LLM response | RaR,  ```json {     """Retro note: focus on reducing friction in quick capture flow""" }<end_of_turn> | future_focus should reduce friction within the quick capture flow. focus on iterative improvement loop to achieve peak efficiency and reduce time wasted. dea... |
| task_03 | Failed to decode JSON response: Unexpected JSON token at offset 3: Expected colon ':', ... | No JSON object found in LLM response | ```json}{""pick up groceries this weekend""}{"""Task:"""\nDescription: Pick up groceries this weekend. This task requires a simple errand to obtain necessary... | ```json {   "type": "task",   "title": "pick up groceries this weekend",   "body": "Pick up groceries today.",    "due_date": null,     // This is a note. Ke... |
| task_07 | Failed to decode JSON response: Unexpected JSON token at offset 3: Expected colon ':', ... | Failed to decode JSON response: Unexpected JSON token at offset 55: Trailing comma befo... | ```json}{""Input text"": {""title"": ""submit quarterly report today before 5pm"", ""body"": "", ""due_date"": "today", ""due_time"": "HH:MM"", "",priority""... | ```json {   "title": "Q&R Report Submission",   "body": null,   // No body needed - just submission confirmation\n  "due_date": null, // no due date specifie... |
| task_08 | Failed to decode JSON response: Unexpected JSON token at offset 13: Expected colon ':',... | No JSON object found in LLM response | ```json { 	'task': 'clean out the garage',  	'title': 'home',  	'body': 'null',  	'due_date': '2026-03-20',  	'due_time': null,      'priority':'medium'' ,  ... | ```json {   "title": "Home Cleaning",   "body": "Clean out the garage no rush #home",   "priority": "medium", // or low based on input text provided instead,... |
| task_09 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {   "priorities": ["medium"],   "tags": ["task", "note"],  "due_date": "2026-03-21", # or 18:77 (HH:mm) if available, otherwise use today_date and no... | ```json {   "title": "email the proposal to the client day after tomorrow",   "body": "Please email the proposal to the client day after tomorrow.",   "prior... |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 8838.9 | 8028.0 | 11973.0 |
| note | 8021.0 | 7559.0 | 10804.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 10298.6667 | 10940.0 | 10940.0 | 0.6667 | 0.0 | 1.0 |
| medium | 16 | 8134.0 | 7558.0 | 11973.0 | 0.5625 | 0.0 | 0.9 |
| long | 1 | 7559.0 | 7559.0 | 7559.0 | 0.0 | 0.0 | 0.4 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 10940.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.0571 | 7558.0 | 1.0 | 0.2857 | 0.0 | Utility delta -0.0571 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.0667 | 7929.0 | 2.0 | 0.7778 | 0.0 | Utility delta -0.0667 below threshold +0.03 |
| note/long | rule-based | 1 | -0.6 | 7559.0 | 3.0 | 0.0 | 0.0 | Utility delta -0.6 below threshold +0.03 |

## Model: gemma3-270m

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 9 |
| Failed to decode JSON response: Unexpected JSON token at offset 9: Expected colon ':', but had ',' instead at path: $... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 90: Expected colon ':', but had 'EOF' instead at path... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 7: Expected colon ':', but had ',' instead at path: $... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 1916 | false | true | No JSON object found in LLM response |
| note_02 | note | note | true | 1.0 | 4498 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... |
| note_03 | note | note | true | 1.0 | 4829 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 90: Expected colon ':', but had 'EOF' instead at path... |
| note_04 | note | note | true | 1.0 | 4052 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 7: Expected colon ':', but had ',' instead at path: $... |
| note_05 | note | note | true | 1.0 | 5839 | false | true | No JSON object found in LLM response |
| note_06 | note | task | false | 1.0 | 3459 | true | false |  |
| note_07 | note | task | false | 1.0 | 1417 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 1.0 | 1267 | false | true | No JSON object found in LLM response |
| note_09 | note | task | false | 1.0 | 3049 | true | false |  |
| note_10 | note | task | false | 1.0 | 2729 | true | false |  |
| task_01 | task | task | true | 1.0 | 1982 | false | true | No JSON object found in LLM response |
| task_02 | task | task | true | 1.0 | 3364 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 9: Expected colon ':', but had ',' instead at path: $... |
| task_03 | task | task | true | 1.0 | 4949 | false | true | No JSON object found in LLM response |
| task_04 | task | task | true | 0.6 | 862 | true | false |  |
| task_05 | task | task | true | 1.0 | 1456 | false | true | No JSON object found in LLM response |
| task_06 | task | task | true | 1.0 | 1466 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 1.0 | 2822 | true | false |  |
| task_08 | task | task | true | 1.0 | 4922 | false | true | No JSON object found in LLM response |
| task_09 | task | task | true | 1.0 | 845 | true | false |  |
| task_10 | task | task | true | 0.8 | 2130 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | No JSON object found in LLM response | No JSON object found in LLM response | ```json ["the sushi place on Main Street was amazing definitely go back"]<end_of_turn> | response = """Invalid due_date expression. Please use the `due_date` field of input text to determine the full date from the provided time phrase. """ <end_o... |
| note_02 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | ```json {   "type": "task",   "title": "Handle a Date Phrase in an Encountered Input",   "body": "Kotlin corutinizes structured concurrency to prevent leaked... | ```json { ... } <0xBB> name: """handling concurrency between main class classes and other work.""" The input type is `action` or `note` (default `"task"`). T... |
| note_03 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 90: Expected colon ':',... | ```json {   type: "task" // default, can specify multiple requested keys present in the format of a value, but this approach favors just one for demo purpose... | ```json {   "type": "task",   "title": null,   "body": null,   "due_date": "2019-03-20", undefined, // date phrase is absent.  }<end_of_turn> |
| note_04 | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', ... | Failed to decode JSON response: Unexpected JSON token at offset 7: Expected colon ':', ... | ```json {... */} ```<end_of_turn> | ```python> return {"task", title, body, due_date, due\_time, priority, tags, confidence}  pid = input("Enter a name for the project: ")  if pid == "Atomic Ha... |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', ... | No JSON object found in LLM response | ```json {   "type": "task",   "title": "team_migration_to_pg,"   "body": null,   "due_{date}: YYYY-MM-DD", 떻่ 나옴 ? [null] ? } + { /* Note: Keep full input te... | ```json {  "type": "task",  "title": "migrate to Postgres before Q3",  body: null, due_date: requested_date, due_time: requested_time, priority: low, tags: a... |
| note_07 | Failed to decode JSON response: Unexpected JSON token at offset 73: Expected end of the... | No JSON object found in LLM response | ```json {   "type": "task",   "title": ".simple is prerequisite for reliability". } ```<end_of_turn> | <end_of_turn> |
| note_08 | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', ... | No JSON object found in LLM response | ```json {... } ```<end_of_turn> | название: onboarding confusion around reminders<end_of_turn> |
| task_01 | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | No JSON object found in LLM response | ```json { ... } keys: type, title, body, due_date, due\_time, priority, tags, confidence, use type="note" for observations and ideas (default: medium), use t... | <end_of_turn> |
| task_02 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 9: Expected colon ':', ... | ```json {  "type": "task",  "title": "read book appointment", // desired style.   ".body": "Book dentist appointment March 20 at 10am" // No added data.   .d... | ```json {  "type", "task" or "note" } ```<end_of_turn> |
| task_03 | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', ... | No JSON object found in LLM response | ```json { ...} <think> unknown read your request requested an article review topic related to the weather forecast of ]]>&#x1d622;<![CDATA[ ]]>&#x1d622;<![CDATA[ ]]>&#x1d635;<![CDATA[]]>&#x1d626;<![CDATA[]]>&#x1d626;<![CDATA[ ]]>&#x1d622;<![CDATA[ ]]>&#x1d622;<![CDATA[ ]]>&#x1d635;<![CDATA[]]>&#x1d626;<![CDATA[]]>&#x1d626;<![CDATA[ ]]>&#x1d635;<![CDATA[]]>&#x1d452;<![CDATA[]]>&#x1d626;<![CDATA[ ]]>&#x1d635;<![CDATA[]]>&#x1d452;<![CDATA[]]>&#x1d626;<![CDATA[... | ```json {   "type": "task",   "title": "find out about grocery shopping",   "body": null,   "due_date": "2026-03-20",   "due_time": null,   "priority":["low"... |
| task_05 | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | No JSON object found in LLM response | ```json { “[“ echo“ *write* “” do not return”.} / … << content added to string”</div> ``` <end_of_turn> | <end_of_turn> |
| task_06 | No JSON object found in LLM response | No JSON object found in LLM response | Please provide only one JSON object of desired length and no more than one less than the requested information, adhering to the requirements given in the pro... | <end_of_turn> |
| task_08 | Failed to decode JSON response: Unexpected JSON token at offset 10: Expected colon ':',... | No JSON object found in LLM response | ```json {   "type",   "title",   "body",   "", ... // Missing 'due_date', due\_time, priority, tags, confidence's value if needed for a reasonable response a... | ```json {  "type": "task", ... none else 2026-03-2018 YYYY-XXXX-MMDD <...> / None else 54999581317 YYYY-<,...> MMDD</...> / None else 4675600815 <...> / None... |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 2479.8 | 1982.0 | 4922.0 |
| note | 3305.5 | 3049.0 | 4829.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 3079.0 | 2822.0 | 2822.0 | 0.6667 | 0.0 | 1.0 |
| medium | 16 | 2736.6875 | 2130.0 | 4922.0 | 0.625 | 0.0 | 0.835 |
| long | 1 | 4829.0 | 4829.0 | 4829.0 | 1.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | 0.0 | 2822.0 | 1.0 | 0.6667 | 0.0 | Utility delta +0.0 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.0343 | 1982.0 | 1.0 | 0.5714 | 0.0 | Utility delta -0.0343 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.2 | 3049.0 | 2.0 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 4829.0 | 3.0 | 1.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |

## Model: qwen3-0.6

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 2 |
| Failed to decode JSON response: Unexpected JSON token at offset 36: Expected colon ':', but had 'EOF' instead at path... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 99: Expected colon ':', but had ',' instead at path: ... | 1 |
| Failed to decode JSON response: Failed to parse literal '"medium"' as a float value at element: $.confidence JSON inp... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 9770 | true | false |  |
| note_02 | note | task | false | 1.0 | 2316 | true | false |  |
| note_03 | note | note | true | 1.0 | 2664 | true | false |  |
| note_04 | note | note | true | 1.0 | 9931 | false | true | No JSON object found in LLM response |
| note_05 | note | note | true | 1.0 | 11075 | false | true | No JSON object found in LLM response |
| note_06 | note | task | false | 0.8 | 7233 | true | false |  |
| note_07 | note | note | true | 0.5 | 11440 | true | false |  |
| note_08 | note | task | false | 1.0 | 8901 | true | false |  |
| note_09 | note | task | false | 1.0 | 6781 | true | false |  |
| note_10 | note | task | false | 0.8 | 5318 | true | false |  |
| task_01 | task | task | true | 1.0 | 4360 | true | false |  |
| task_02 | task | task | true | 1.0 | 9584 | true | false |  |
| task_03 | task | note | false | 1.0 | 3232 | true | false |  |
| task_04 | task | note | false | 0.75 | 12255 | true | false |  |
| task_05 | task | note | false | 1.0 | 6556 | true | false |  |
| task_06 | task | task | true | 1.0 | 9176 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 36: Expected colon ':', but had 'EOF' instead at path... |
| task_07 | task | task | true | 1.0 | 8920 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 99: Expected colon ':', but had ',' instead at path: ... |
| task_08 | task | note | false | 0.75 | 2378 | true | false |  |
| task_09 | task | note | false | 1.0 | 3009 | true | false |  |
| task_10 | task | task | true | 1.0 | 8833 | false | true | Failed to decode JSON response: Failed to parse literal '"medium"' as a float value at element: $.confidence JSON inp... |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_04 | No JSON object found in LLM response | No JSON object found in LLM response | <think>  </think> {     "type": "note",     "title": "Atomic Habits has great advice on habit stacking really insightful",     "body": input_text,     "     ... | enses:  Okay, let me tackle this problem. The user provided an input that needs to be converted into the specified JSON format. First, I need to check for an... |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 228: Expected colon ':'... | No JSON object found in LLM response | <think>  </think>  {   "type": "note",   "title": "",   "body": "@the team agreed we should migrate to Postgres before Q3", "text": "+ the team agreed we sho... | <think> Okay, let's see. The user mentioned that the team agreed they should migrate to PostgreSQL before Q3. They want me to return a JSON object based on t... |
| task_06 | Failed to decode JSON response: Unexpected JSON token at offset 31: Expected end of the... | Failed to decode JSON response: Unexpected JSON token at offset 36: Expected colon ':',... | <think>  </think>   {     type: 'task',     title: 'pay electricity bill in 3 days',     body: input_text,     due_date: '2026-11-30',     due_time: null,   ... | ()): <tool_call> {"type": "note", "title": "Pay Electricity Bill in 3 Days", "body": "I will pay my electric bill by paying now as soon as possible, I have t... |
| task_07 | Failed to decode JSON response: Unexpected JSON token at offset 34: Expected end of the... | Failed to decode JSON response: Unexpected JSON token at offset 99: Expected colon ':',... | <think>  {     type: 'task',     title: 'submit quarterly report today before 5pm',     body: 'submit quarterly report today before 5pm',     due_date: null,... | <think>  </think>  { "type": "task", "title": "", "default_priority": "", "due_date": "", "due_time\": \"2026-03-29\"", "priority\": \"medium\"", "body\": \"... |
| task_10 | Failed to decode JSON response: Unexpected JSON token at offset 35: Expected end of the... | Failed to decode JSON response: Failed to parse literal '"medium"' as a float value at ... | <think>  </think>    {     type: 'task',     title: 'prepare slides for investor pitch in 2 weeks #work #presentation',     body: 'prepare slides for investo... | <think>  </think>  {"type": "note", "title": "investor pitch preparation", "body": "Prepare slides for the investor pitch over the next two weeks. Focus on k... |

### Latency By Prompt Type
| Type | Mean (ms) | P50 (ms) | P95 (ms) |
|---|---:|---:|---:|
| task | 6830.3 | 6556.0 | 9584.0 |
| note | 7542.9 | 7233.0 | 11075.0 |

### Latency By Prompt Length
| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |
|---|---:|---:|---:|---:|---:|---:|---:|
| short | 3 | 7109.3333 | 8920.0 | 8920.0 | 0.6667 | 0.0 | 0.8 |
| medium | 16 | 7483.75 | 7233.0 | 11440.0 | 0.1875 | 0.0 | 0.6275 |
| long | 1 | 2664.0 | 2664.0 | 2664.0 | 0.0 | 0.0 | 1.0 |

### Routing Recommendation
Keep all prompts on rule-based parser for this model.

| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |
|---|---|---:|---:|---:|---:|---:|---:|---|
| task/short | rule-based | 3 | -0.2 | 8920.0 | 1.0 | 0.6667 | 0.0 | Utility delta -0.2 below threshold +0.03 |
| task/medium | rule-based | 7 | -0.3714 | 6556.0 | 1.0 | 0.1429 | 0.0 | Utility delta -0.3714 below threshold +0.03 |
| note/medium | rule-based | 9 | -0.3067 | 8901.0 | 2.0 | 0.2222 | 0.0 | Utility delta -0.3067 below threshold +0.03 |
| note/long | rule-based | 1 | 0.0 | 2664.0 | 3.0 | 0.0 | 0.0 | Utility delta +0.0 below threshold +0.03 |
