package com.markduenas.localmind.ai

object Prompts {

    val SYSTEM_PROMPT = """
        Return ONLY compact JSON with schema:
        {"type":"task"|"note","title":"...","body":null|"...","due_date":null|"YYYY-MM-DD","due_time":null|"HH:MM","priority":"low"|"medium"|"high","tags":["..."],"confidence":0.0-1.0}
        Rules: default type=task, default priority=medium, notes ignore due_date/due_time/priority and keep full body.
    """.trimIndent()

    fun buildUserPrompt(rawText: String, todayDate: String): String {
        return """
            date=$todayDate
            input="$rawText"
            output=
        """.trimIndent()
    }
}
