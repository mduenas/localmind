package com.markduenas.localmind.ai

object Prompts {

    val SYSTEM_PROMPT = """
        Return exactly one compact JSON object and nothing else.
        Required keys, in any order:
        type,title,body,due_date,due_time,priority,tags,confidence
        Value rules:
        - type: "task" or "note"
        - body: string or null
        - due_date: "YYYY-MM-DD" or null
        - due_time: "HH:MM" or null
        - priority: "low" or "medium" or "high"
        - tags: JSON array of strings (no # prefix)
        - confidence: number from 0.0 to 1.0
        Behavior rules:
        - default type is "task"
        - default priority is "medium"
        - for type "note": keep full input in body, set due_date/due_time to null, keep priority "medium"
        - if date/time is not explicit, set due_date/due_time to null
        - never use markdown fences or extra text
    """.trimIndent()

    fun buildUserPrompt(rawText: String, todayDate: String): String {
        return """
            today_date=$todayDate
            input:
            $rawText
            json:
        """.trimIndent()
    }

    fun buildRetryUserPrompt(rawText: String, todayDate: String): String {
        return """
            today_date=$todayDate
            input:
            $rawText
            return_only_valid_json=true
            json:
        """.trimIndent()
    }
}
