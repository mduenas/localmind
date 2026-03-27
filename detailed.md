# LocalMind LLM Benchmark Detailed Report

- Generated: 2026-03-20T16:06:35.023890
- Fixture suite: v1.0.0
- Baseline: rule-based

## Model Metrics
| Model | Composite | Prompts | Classification | Task Fields | Note Fields | Valid JSON | Fallback | Mean (ms) | P50 (ms) | P95 (ms) | Cache Hit |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| rule-based | 0.9648 | 20 | 0.95 | 1.0 | 1.0 | 1.0 | 0.0 | 1.95 | 2.0 | 3.0 | true |
| gemma3-270m | 0.705 | 20 | 0.85 | 1.0 | 1.0 | 0.1 | 0.9 | 3475.15 | 3632.0 | 5349.0 | true |
| qwen3-0.6 | 0.6352 | 20 | 0.7 | 0.935 | 0.9 | 0.7 | 0.3 | 6618.25 | 6728.0 | 11003.0 | true |
| gemma3-1b | 0.631 | 20 | 0.7 | 0.94 | 1.0 | 0.5 | 0.5 | 7373.9 | 6153.0 | 13863.0 | true |

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

## Model: gemma3-270m

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 14 |
| Failed to decode JSON response: Unexpected JSON token at offset 75: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 79: Expected colon ':', but had 'd' instead at path: ... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | note | true | 1.0 | 4280 | false | true | No JSON object found in LLM response |
| note_02 | note | note | true | 1.0 | 3620 | false | true | No JSON object found in LLM response |
| note_03 | note | note | true | 1.0 | 1056 | false | true | No JSON object found in LLM response |
| note_04 | note | note | true | 1.0 | 4416 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', but had 'EOF' instead at path:... |
| note_05 | note | note | true | 1.0 | 3841 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 79: Expected colon ':', but had 'd' instead at path: ... |
| note_06 | note | note | true | 1.0 | 2727 | false | true | No JSON object found in LLM response |
| note_07 | note | task | false | 1.0 | 3560 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 1.0 | 2997 | false | true | No JSON object found in LLM response |
| note_09 | note | note | true | 1.0 | 1452 | false | true | No JSON object found in LLM response |
| note_10 | note | task | false | 1.0 | 3993 | true | false |  |
| task_01 | task | task | true | 1.0 | 5648 | false | true | No JSON object found in LLM response |
| task_02 | task | task | true | 1.0 | 4108 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 75: Expected end of the object or comma at path: $ JS... |
| task_03 | task | note | false | 1.0 | 3632 | true | false |  |
| task_04 | task | task | true | 1.0 | 4220 | false | true | No JSON object found in LLM response |
| task_05 | task | task | true | 1.0 | 4855 | false | true | No JSON object found in LLM response |
| task_06 | task | task | true | 1.0 | 1864 | false | true | No JSON object found in LLM response |
| task_07 | task | task | true | 1.0 | 1471 | false | true | No JSON object found in LLM response |
| task_08 | task | task | true | 1.0 | 5349 | false | true | No JSON object found in LLM response |
| task_09 | task | task | true | 1.0 | 2509 | false | true | No JSON object found in LLM response |
| task_10 | task | task | true | 1.0 | 3905 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', but had 'EOF' instead at path:... |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_01 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {   type: "task",   title: "Sushi Place on Main Street - Was Amazing!" at YYYY-MM-DD and HH:MM format with a negative date name, due_date is null. gi... | ```json {     "type": "note",     "title": "Sushi Place on Main Street",     "body": "The sushi place was amazing definitely go back.",<end_of_turn> |
| note_02 | Failed to decode JSON response: Unexpected JSON token at offset 4: Expected colon ':', ... | No JSON object found in LLM response | ```json {...}<end_of_turn> | ```java import json.JSON;  class JsonObject {  var type = "task"; // Default to task or note, default value 'task' if input asks for a date phrase. Note that... |
| note_03 | No JSON object found in LLM response | No JSON object found in LLM response | <end_of_turn> | <end_of_turn> |
| note_04 | Failed to decode JSON response: Unexpected JSON token at offset 90: Expected colon ':',... | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | ```json {   "type": "note",   "title": "Atomic Habits has great advice...",   "body": null, ...na explanation: key row provided should be 'atomic_habits' or ... | ```json { ... } instructions for updating content when data changes.  today\_date: 2026-3-20 14 *PM* on March 20, 24. We are using the data from Tomorrow's N... |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 59: Expected colon ':',... | Failed to decode JSON response: Unexpected JSON token at offset 79: Expected colon ':',... | ```json {   "type": "task",   "title": null,   "body": null,   ... except for date phrase and time phrase for notes.", // Note: Date phrase is not available ... | ```json {     type: "task",     title: "Migration of Postgres to PostgreSQL before Q3", ..-- due_data: 2026-03-20,-18:00 - due_time: 6pm, due_time: null, pri... |
| note_06 | No JSON object found in LLM response | No JSON object found in LLM response | ```python> {   type, title, body, due_date, time, priority , tags , confidence  = "task" or "note" (default "task") ; no return requested.    title : string ... | <end_of_turn> |
| note_07 | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | No JSON object found in LLM response | ```json { ... } ```<end_of_turn> | Here'zipped JSON object with only the specified keys: ```json {     "type": "task",     title: "book quote",    // default "note" (default _date") or null va... |
| note_08 | No JSON object found in LLM response | No JSON object found in LLM response | <end_of_turn> | ```json { Nineteen years ago, customer interview takeaway: onboarding confusion around reminders due date: 3 days from today's day - March 21, 2018 due_time:... |
| note_09 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {"task": "travel thought:", “title”：“Boise in early fall has perfect running weather"} ```<end_of_turn> | <end_of_turn> |
| task_01 | Failed to decode JSON response: Unexpected JSON token at offset 6: Expected colon ':', ... | No JSON object found in LLM response | ```json { ... }  The following is a simple example of assigning the input text to the `json.dumps` object provided in your prompt. For more complex scenarios... | ```json {   "type": "task",   "title": "remind me to call mom tomorrow at 6pm",   "body": null,   "due_dates": {     "2026-03-20": null,     // Other relevan... |
| task_02 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 75: Expected end of the... | ```json {   "type": "task", // default "note" or "// previous notes", keep full input in body and keep due_date/due_time null   "title": "book dentist appoin... | ```json {     type: "task",     title: "book appointment",     body: null,     due_date:  &=& "31 March 2026" ，  `true` }  ```<end_of_turn> |
| task_04 | Failed to decode JSON response: Unexpected JSON token at offset 12: Expected colon ':',... | No JSON object found in LLM response | ```json {   "type", "task" or "note" (default "task")...", only "" <think> ... and null for due_date/due_time when that field is truly absent.    "title:", s... | ```json {"type": "task", title": "improve production database bug","body":null, due_date": "2026-03-20", due_time": null, priority": "low", tags": ["array of... |
| task_05 | No JSON object found in LLM response | No JSON object found in LLM response | ```python import json  type = input("Enter a date phrase (YYYY-MM-DD): ") title = input("Enter a time phrase (HH:MM): ")  body = input("Input the schedule te... | ```json {   "type": "task",   "title": null,   "body": null,   "due_dates":[null], // due\_date: null (default default). We will use 'today\_date' for one-ti... |
| task_06 | No JSON object found in LLM response | No JSON object found in LLM response | ```python> return json.loads("pay electricity bill in 3 days")  ```<end_of_turn> | The requested JSON object and nothing else is:  The requested object reads as: <th name="my_note"> ... is unknown... </th><end_of_turn> |
| task_07 | No JSON object found in LLM response | No JSON object found in LLM response | result: The specified JSON object with the recommended key handling of missing data and careful normalization of dates. <end_of_turn> | document "quarterly report today before 5pm" <end_of_turn> |
| task_08 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {   "type": "task",   "title": "clean out garage no rush",   "body": "clean out the garage no rush , # home",   "due_date": null,   "due_time": null,... | ```json {     type: "task", message: """Remove this line from the main body of the input text.""" // The line that is removed should be replaced by a new one... |
| task_09 | Failed to decode JSON response: Unexpected JSON token at offset 118: Expected colon ':'... | No JSON object found in LLM response | ```json {   "type": "task",   "title": "proposal submission",   "body": null,   "due_date": "2026-03-20", JECTION: null, key = actual_date, ensure is ignored... | ```json{number: 148979575487623  </think><end_of_turn> |
| task_10 | Failed to decode JSON response: Unexpected JSON token at offset 22: Expected colon ':',... | Failed to decode JSON response: Unexpected JSON token at offset 5: Expected colon ':', ... | ```json { "... due_date: ..." error message ": ... of ...". fix=""}  ```<end_of_turn> | ```json {n="task"} body: {null} due_date: 3/15/2026 due\_time: {null} due\_data: "{null}" due\_time:: "Time" is present. The input text is likely to require ... |

## Model: qwen3-0.6

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 2 |
| Failed to decode JSON response: Unexpected JSON token at offset 26: Expected end of the object or comma at path: $ JS... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 185: Expected end of the object or comma at path: $ J... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', but had 'EOF' instead at path:... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 34: Expected end of the object or comma at path: $ JS... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | task | false | 1.0 | 3179 | true | false |  |
| note_02 | note | note | true | 1.0 | 11003 | true | false |  |
| note_03 | note | note | true | 1.0 | 6314 | false | true | No JSON object found in LLM response |
| note_04 | note | note | true | 1.0 | 3982 | true | false |  |
| note_05 | note | note | true | 1.0 | 14494 | false | true | No JSON object found in LLM response |
| note_06 | note | task | false | 1.0 | 8364 | true | false |  |
| note_07 | note | task | false | 1.0 | 6424 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', but had 'EOF' instead at path:... |
| note_08 | note | note | true | 0.5 | 10410 | true | false |  |
| note_09 | note | note | true | 1.0 | 7904 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 34: Expected end of the object or comma at path: $ JS... |
| note_10 | note | note | true | 0.5 | 2335 | true | false |  |
| task_01 | task | task | true | 1.0 | 7046 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 26: Expected end of the object or comma at path: $ JS... |
| task_02 | task | task | true | 1.0 | 3939 | true | false |  |
| task_03 | task | note | false | 1.0 | 2126 | true | false |  |
| task_04 | task | task | true | 0.6 | 5508 | true | false |  |
| task_05 | task | task | true | 1.0 | 7909 | true | false |  |
| task_06 | task | note | false | 1.0 | 3264 | true | false |  |
| task_07 | task | task | true | 1.0 | 6909 | true | false |  |
| task_08 | task | task | true | 1.0 | 6728 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 185: Expected end of the object or comma at path: $ J... |
| task_09 | task | task | true | 1.0 | 6752 | true | false |  |
| task_10 | task | note | false | 0.75 | 7775 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_03 | Failed to decode JSON response: Unexpected JSON token at offset 33: Expected end of the... | No JSON object found in LLM response | <think>  </think> {   type: 'task',   title: 'plant watering sensor project',   body: input_text,   due_date: '2026-03-20',   due_time: '',   priority: '', }... | <think>  </think>   {"type":"note","title":"A Plant Watering Sensor with Raspberry Pi Project","body":"","due_date":"today_date","due_time":"+1459","priority... |
| note_05 | Failed to decode JSON response: Unexpected JSON token at offset 31: Expected end of the... | No JSON object found in LLM response | <think>  ```json {   type: 'task',   title: 'the team agreed we should migrate to Postgres before Q3',   body: input_text,   due_date: today_date,   due_time... | <think> Okay, let me try to figure out how to process this information. The user said that the team agreed they should migrate to PostgreSQL before Q3. So fi... |
| note_07 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 2: Expected colon ':', ... | <think>  ```json {   "task": "book quote: simplicity is prerequisite for reliability",   "title": "",   "body": "",   "due_date": null,   " } ```<\|im_end\|> | <think>  </think>  { 	"typesections" : [ 		{ 			"type : note",  			"title : 'Simplify to Build Reliability',",  			"description : 'Simple solutions are the f... |
| note_09 | Failed to decode JSON response: Unexpected JSON token at offset 75: Expected colon ':',... | Failed to decode JSON response: Unexpected JSON token at offset 34: Expected end of the... | <think>  </think>   {"type":"task","title":"","body":"","due_date":"2026-03-20","due_time:null","priority":"","tags":[""],"confidence":""}<\|im_end\|> | <think>  ```json {     type: 'task',     title: 'Travel Thoughts - Boise in Early Fall with Perfect Running Weather',     body: 'Boise in early fall has perf... |
| task_01 | Failed to decode JSON response: Expected JsonPrimitive, but had JsonArray as the serial... | Failed to decode JSON response: Unexpected JSON token at offset 26: Expected end of the... | <think>  {"type":"note","title":"Call mom tomorrow at PM6","body":"Remind me to call mom tomorrow at 6pm",  "tags":["call-mom"], "confidence":[0.9], "due_tim... | <think>  </think>   {"type":"/note","title":@"call mom tomorrow@9:30am"", $body":@"I need your help today. Please call my mother tonight before I go.", "due_... |
| task_08 | Failed to decode JSON response: Unexpected JSON token at offset 77: Expected colon ':',... | Failed to decode JSON response: Unexpected JSON token at offset 185: Expected end of th... | <think>  </think>  {     type:"task",     title:"",     body:"",     due_date:"",      due_time:"",   	(priority,"medium"),   	tags:[],   	confidence:0.0, }<... | <think>  </think>  [   {       "type": "note",       title: "",       body: "",       due_date: null,       because_of_input:           ["clean out the garag... |

## Model: gemma3-1b

### Error Breakdown
| Error | Count |
|---|---:|
| No JSON object found in LLM response | 7 |
| Failed to decode JSON response: Unexpected JSON token at offset 51: Expected colon ':', but had '"' instead at path: ... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 91: Trailing comma before the end of JSON object at p... | 1 |
| Failed to decode JSON response: Unexpected JSON token at offset 48: Expected colon ':', but had 't' instead at path: ... | 1 |

### Prompt Evaluations
| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |
|---|---|---|---|---:|---:|---|---|---|
| note_01 | note | task | false | 1.0 | 3981 | true | false |  |
| note_02 | note | task | false | 1.0 | 3304 | true | false |  |
| note_03 | note | task | false | 1.0 | 7732 | true | false |  |
| note_04 | note | note | true | 1.0 | 13863 | false | true | No JSON object found in LLM response |
| note_05 | note | note | true | 1.0 | 14317 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 48: Expected colon ':', but had 't' instead at path: ... |
| note_06 | note | task | false | 1.0 | 3854 | true | false |  |
| note_07 | note | task | false | 1.0 | 11277 | false | true | No JSON object found in LLM response |
| note_08 | note | note | true | 1.0 | 6153 | false | true | No JSON object found in LLM response |
| note_09 | note | task | false | 1.0 | 5367 | true | false |  |
| note_10 | note | note | true | 1.0 | 6687 | false | true | No JSON object found in LLM response |
| task_01 | task | task | true | 1.0 | 11720 | false | true | No JSON object found in LLM response |
| task_02 | task | task | true | 1.0 | 11177 | true | false |  |
| task_03 | task | task | true | 0.8 | 2876 | true | false |  |
| task_04 | task | task | true | 1.0 | 8685 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 51: Expected colon ':', but had '"' instead at path: ... |
| task_05 | task | task | true | 1.0 | 1918 | true | false |  |
| task_06 | task | task | true | 1.0 | 8948 | true | false |  |
| task_07 | task | task | true | 1.0 | 5957 | false | true | No JSON object found in LLM response |
| task_08 | task | task | true | 1.0 | 4973 | false | true | No JSON object found in LLM response |
| task_09 | task | task | true | 1.0 | 8794 | false | true | Failed to decode JSON response: Unexpected JSON token at offset 91: Trailing comma before the end of JSON object at p... |
| task_10 | task | task | true | 0.6 | 5895 | true | false |  |

### Invalid JSON Raw Output Samples
| Prompt | First Error | Retry Error | First Response | Retry Response |
|---|---|---|---|---|
| note_04 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {     "title": "Atomic Habits has great advice on habit stacking really insightful", # No due_date/due_time values here since original input doesn't ... | ```json {   "title": "Atomic Habits - Insightful Advice",    "body": "Previous output was invalid. Try again from scratch.",  সুরই无意义的建议(Atomic Habits) 有令人启发... |
| note_05 | No JSON object found in LLM response | Failed to decode JSON response: Unexpected JSON token at offset 48: Expected colon ':',... | ```json {   "title": "Task Migration to PostgreSQL",   "body": "The team agreed that we should migrate to Postgres before Q3.",   "priority": "medium",    "t... | ```json {    "title": "Task - Migration to Postgres", # title string    "body": "The team agreed that we should migrate to Postgres before Q3.", # body strin... |
| note_07 | No JSON object found in LLM response | No JSON object found in LLM response | ```json  { 실제 본문에는 적절한 미드타임이없는 누락된 날짜 및 시간입니다. 본문의 출처를 알 수 없습니다. 사용 유형을 수정했습니다. 정확한 답변을 제공하려면 주장이 있는 입력과 날/시간에서 특정 정보를 추출하는 데 필요한 예제를 더 많이 지원해야 합니다.$<end_of_... | ```json {   "task": "book quote simplicity is prerequisite for reliability",   "due_date": null,   "due_time": null, // Or "18:00" if preferred in the body o... |
| note_08 | Failed to decode JSON response: Unexpected JSON token at offset 167: Expected end of th... | No JSON object found in LLM response | ```json {   "due_date": null,   "due_time": null,    "priority": "medium",    "tags": [     "<task>",     "<note>" , "onboarding confusion around reminders>"... | User interview takeaway: onboarding confusion around reminders<end_of_turn> |
| note_10 | Failed to decode JSON response: Unexpected JSON token at offset 136: Trailing comma bef... | No JSON object found in LLM response | ```json {   "title": "retro note: focus on reducing friction in quick capture flow",   "body": "Reduce friction in quickly captured workflows.", , null, null... | add_to_notes_list("focus on reducing friction in quick capture flow")due_date=2026-03-20 due_time=17:30 relevance="Focuses heavily on eliminating unnecessary... |
| task_01 | No JSON object found in LLM response | No JSON object found in LLM response | ```json {   'type': 'task', // or 'note' or 'task' depending on the input. The final result is a single object. This will be modified within the generated js... | ```json {   "title": "Reminder: Call Mom",   "body": "Remind me to call Mom tomorrow at 6pm",    "due_date": null,   // Due date is not specified. Use today_... |
| task_04 | Failed to decode JSON response: Unexpected JSON token at offset 182: Expected colon ':'... | Failed to decode JSON response: Unexpected JSON token at offset 51: Expected colon ':',... | ```json() {   "title": null,   "body": "**urgent fix the production database bug**\n\nThis is a matter of urgent attention.",   "due_date": null,   "due_time... | ```json {     “title”: “fix the production database bug”, ，“priority”: “high”, ，“tags”: ["task"],      ，“confidence”: 1.0   ，         “due_date”: “2026-03-20... |
| task_07 | No JSON object found in LLM response | No JSON object found in LLM response | ```json absent null: null   today_date: 2026-03-20 ```<end_of_turn> | ```json  {     "" : "submit quarterly report today before 5pm", ?full text is missing. Consider just submitting the report." ,<0xC2><0xA0>?due\_date=null<0xC... |
| task_08 | No JSON object found in LLM response | No JSON object found in LLM response | ```jsonстема { "title": "Clean out the garage - no rush", “body”: “clean out the garage”, “due_date": null, “due_time”: null,  “priority”:“low”} ```<end_of_t... | Please provide the input text. I need it to fulfill your requirements!<end_of_turn> |
| task_09 | Failed to decode JSON response: Unexpected JSON token at offset 153: Expected colon ':'... | Failed to decode JSON response: Unexpected JSON token at offset 91: Trailing comma befo... | ```json {   "priority": "medium",   "tags": ["note", "task"],    "body": "*email the proposal to the client day after tomorrow*",    "due_date": null, 2026-0... | ```jsonเพียงแค่ JSON object: {     "type":"task",      “title”: “email the proposal”,      “body”: “day after tomorrow”, ,      “due_date”: null ,      “prio... |
