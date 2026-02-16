package com.markduenas.localmind.ai

object Prompts {

    val SYSTEM_PROMPT = """
        You are a task extraction assistant. Given a natural language input, extract structured task information.

        Respond ONLY with a JSON object. No explanation, no markdown, no extra text.

        JSON schema:
        {
          "title": "string — clean, concise task title",
          "due_date": "string or null — ISO 8601 date (YYYY-MM-DD) if mentioned",
          "due_time": "string or null — HH:MM in 24h format if mentioned",
          "priority": "string — one of: low, medium, high",
          "tags": ["string"] — extracted topics or categories,
          "confidence": number — 0.0 to 1.0 how confident you are in the extraction
        }

        Rules:
        - "tomorrow" means the day after today
        - "next week" means the upcoming Monday
        - Default priority is "medium" unless urgency words are present
        - Extract implicit tags from context (e.g., "call dentist" → ["health"])
        - Title should be imperative and concise (e.g., "Call dentist")
    """.trimIndent()

    val FEW_SHOT_EXAMPLES = """
        Input: "call mom tomorrow"
        Output: {"title":"Call mom","due_date":"2026-02-17","due_time":null,"priority":"medium","tags":["family"],"confidence":0.95}

        Input: "urgent fix the login bug #work"
        Output: {"title":"Fix the login bug","due_date":null,"due_time":null,"priority":"high","tags":["work"],"confidence":0.9}

        Input: "dentist appointment March 15 at 2pm"
        Output: {"title":"Dentist appointment","due_date":"2026-03-15","due_time":"14:00","priority":"medium","tags":["health"],"confidence":0.95}

        Input: "buy groceries this weekend"
        Output: {"title":"Buy groceries","due_date":"2026-02-21","due_time":null,"priority":"medium","tags":["shopping"],"confidence":0.9}
    """.trimIndent()

    fun buildUserPrompt(rawText: String, todayDate: String): String {
        return """
            Today's date is $todayDate.

            $FEW_SHOT_EXAMPLES

            Input: "$rawText"
            Output:
        """.trimIndent()
    }
}
