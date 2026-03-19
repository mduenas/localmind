package com.markduenas.localmind.ai

object Prompts {

    private val BASE_SYSTEM_PROMPT = """
        Return exactly one JSON object and nothing else.
        Do not output <think> blocks, markdown, comments, code fences, or explanations.

        Required keys (exactly these keys):
        type,title,body,due_date,due_time,priority,tags,confidence

        Rules:
        - type: "task" or "note" (default "task")
        - title: string
        - body: string or null
        - due_date: "YYYY-MM-DD" or null
        - due_time: "HH:MM" or null
        - priority: "low" | "medium" | "high" (default "medium")
        - tags: array of strings without "#"
        - confidence: number from 0.0 to 1.0
        - use type="note" for observations, ideas, quotes, takeaways, retro notes, and general statements without an action request
        - use type="task" when the input asks to do/remind/schedule/pay/call/book/submit something
        - if type is "note": keep full input text in body, due_date=null, due_time=null, priority="medium"
        - if input has an explicit date phrase, due_date must not be null
        - if input has an explicit time phrase, due_time must not be null
        - only use null for due_date/due_time when that field is truly absent
        - normalize due_time to 24-hour HH:MM (examples: "6pm"->"18:00", "9.30 in the morning"->"09:30")
        - resolve relative dates using today_date (examples: "tomorrow", "day after tomorrow", "in 3 days")
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
        return """
            today_date: $todayDate
            input_text:
            $rawText
            
            Extraction requirements:
            - If the input includes a date phrase, set due_date (do not return null).
            - If the input includes a time phrase, set due_time (do not return null).
            - For notes, keep full input in body and keep due_date/due_time null.
            
            Return only the JSON object now.
            $thinkControl
        """.trimIndent()
    }

    fun buildRetryUserPrompt(rawText: String, todayDate: String, modelSlug: String? = null): String {
        val thinkControl = if (isQwen3(modelSlug)) "/no_think" else ""
        return """
            today_date: $todayDate
            input_text:
            $rawText

            Previous output was invalid. Try again from scratch.
            Return exactly one valid JSON object with required keys only.
            If a date phrase is present, due_date must be populated.
            If a time phrase is present, due_time must be populated.
            Do not include markdown, comments, code, or explanations.
            $thinkControl
        """.trimIndent()
    }

    private fun isQwen3(modelSlug: String?): Boolean {
        return modelSlug?.lowercase()?.startsWith("qwen3-") == true
    }
}
