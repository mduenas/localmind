package com.markduenas.localmind.ai

object Prompts {

    val SYSTEM_PROMPT = """
        You are a capture classification assistant. Given a natural language input, classify it as a "task" or "note" and extract structured information.

        Respond ONLY with a JSON object. No explanation, no markdown, no extra text.

        JSON schema:
        {
          "type": "string — either \"task\" or \"note\"",
          "title": "string — clean, concise title",
          "body": "string or null — full content for notes, null for tasks",
          "due_date": "string or null — ISO 8601 date (YYYY-MM-DD) if mentioned (tasks only)",
          "due_time": "string or null — HH:MM in 24h format if mentioned (tasks only)",
          "priority": "string — one of: low, medium, high (tasks only)",
          "tags": ["string"] — extracted topics or categories,
          "confidence": number — 0.0 to 1.0 how confident you are in the extraction
        }

        Classification rules:
        - If the input contains action verbs (buy, call, fix, send, schedule, meet, finish, submit, clean, etc.) or deadlines/dates → type is "task"
        - If the input is an observation, idea, thought, fact, recommendation, or note-to-self without a clear action → type is "note"
        - When in doubt, classify as "task"

        Task rules:
        - "tomorrow" means the day after today
        - "next week" means the upcoming Monday
        - Default priority is "medium" unless urgency words are present
        - Title should be imperative and concise (e.g., "Call dentist")
        - body should be null for tasks

        Note rules:
        - Title should be a concise summary of the content
        - body should contain the full content or elaboration
        - due_date, due_time, and priority are ignored for notes
        - Extract implicit tags from context
    """.trimIndent()

    val FEW_SHOT_EXAMPLES = """
        Input: "call mom tomorrow"
        Output: {"type":"task","title":"Call mom","body":null,"due_date":"2026-02-17","due_time":null,"priority":"medium","tags":["family"],"confidence":0.95}

        Input: "urgent fix the login bug #work"
        Output: {"type":"task","title":"Fix the login bug","body":null,"due_date":null,"due_time":null,"priority":"high","tags":["work"],"confidence":0.9}

        Input: "dentist appointment March 15 at 2pm"
        Output: {"type":"task","title":"Dentist appointment","body":null,"due_date":"2026-03-15","due_time":"14:00","priority":"medium","tags":["health"],"confidence":0.95}

        Input: "buy groceries this weekend"
        Output: {"type":"task","title":"Buy groceries","body":null,"due_date":"2026-02-21","due_time":null,"priority":"medium","tags":["shopping"],"confidence":0.9}

        Input: "great pasta at the Italian place on 5th street"
        Output: {"type":"note","title":"Great pasta at Italian place","body":"Great pasta at the Italian place on 5th street","due_date":null,"due_time":null,"priority":"medium","tags":["food","restaurants"],"confidence":0.9}

        Input: "the new React compiler looks promising for performance"
        Output: {"type":"note","title":"React compiler looks promising","body":"The new React compiler looks promising for performance","due_date":null,"due_time":null,"priority":"medium","tags":["tech","react"],"confidence":0.85}
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
