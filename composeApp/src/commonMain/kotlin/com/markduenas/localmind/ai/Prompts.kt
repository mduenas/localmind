package com.markduenas.localmind.ai

object Prompts {

    private val BASE_SYSTEM_PROMPT = """
        You are a deterministic information extractor.
        Return exactly one RFC8259-valid JSON object and nothing else.
        No markdown, no code fences, no comments, no prose, no <think>.
        Use double quotes for all keys and string values.
        Use literal null for missing values (never None, never "").

        Output schema: {"type":"","title":"","body":null,"due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.0}
        Required keys (exactly these 8 keys, in any order):
        type,title,body,due_date,due_time,priority,tags,confidence

        Semantic rules:
        - type is "task" or "note"
        - note: observations/ideas/quotes/takeaways/general statements without an action request
        - task: requests to do/remind/schedule/pay/call/book/submit/check/follow up
        - title: concise summary string
        - body: string or null; for note, keep full input text in body
        - due_date: "YYYY-MM-DD" or null
        - due_time: "HH:MM" 24-hour or null
        - priority: "low"|"medium"|"high" (default "medium")
        - tags: array of lowercase strings without "#"
        - confidence: number from 0.0 to 1.0

        Date/time rules:
        - Resolve relative dates using today_date.
        - Normalize time like "6pm"->"18:00", "9.30 in the morning"->"09:30".
        - For note, set due_date=null and due_time=null.
        - For task, set due_date/due_time when explicitly present; otherwise null.
    """.trimIndent()

    val SYSTEM_PROMPT: String = BASE_SYSTEM_PROMPT

    fun systemPromptForModel(modelSlug: String?): String {
        return if (isQwen3(modelSlug)) {
            "/no_think\n$BASE_SYSTEM_PROMPT"
        } else {
            BASE_SYSTEM_PROMPT
        }
    }

    fun buildUserPrompt(rawText: String, todayDate: String, modelSlug: String? = null): String {
        val thinkControl = if (isQwen3(modelSlug)) "/no_think" else ""
        val typeHint = typeHintFor(rawText)
        return """
            today_date: $todayDate
            type_hint: $typeHint
            input_text:
            $rawText

            Build the final JSON in one pass.

            Validation checklist before answering:
            1) Exactly one JSON object.
            2) Exactly these keys: type,title,body,due_date,due_time,priority,tags,confidence
            3) Strict JSON formatting (double quotes, no trailing commas, no comments).
            4) confidence is numeric, tags is array, due_date is YYYY-MM-DD or null, due_time is HH:MM or null.
            5) Use type_hint unless the input clearly contradicts it.

            Return only the JSON object.
            $thinkControl
        """.trimIndent()
    }

    private fun isQwen3(modelSlug: String?): Boolean {
        return modelSlug?.lowercase()?.startsWith("qwen3-") == true
    }

    private fun typeHintFor(rawText: String): String {
        val lower = rawText.lowercase()

        val taskPatterns = listOf(
            Regex("\\b(remind|schedule|call|book|submit|pay|email|check|follow up|todo|to do|task)\\b"),
            Regex("\\b(today|tomorrow|tonight|next week|in \\d+ (day|days|week|weeks))\\b"),
            Regex("\\b\\d{1,2}[:.]\\d{2}\\b"),
            Regex("\\b\\d{1,2}(am|pm)\\b"),
        )
        val notePatterns = listOf(
            Regex("\\b(note|idea|thought|quote|takeaway|retro|journal|observation|insight)\\b"),
            Regex("\\b(remember this|learned|i noticed|travel thought)\\b"),
        )

        val taskScore = taskPatterns.count { it.containsMatchIn(lower) }
        val noteScore = notePatterns.count { it.containsMatchIn(lower) }

        return when {
            taskScore > noteScore -> "task"
            noteScore > taskScore -> "note"
            else -> "unknown"
        }
    }
}
