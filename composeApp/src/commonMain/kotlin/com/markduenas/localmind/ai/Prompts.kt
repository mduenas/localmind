package com.markduenas.localmind.ai

object Prompts {

    val SYSTEM_PROMPT = """
        Classify input as "task" or "note". Respond with ONLY a JSON object.
        {"type":"task"|"note","title":"concise title","body":null|"content for notes","due_date":null|"YYYY-MM-DD","due_time":null|"HH:MM","priority":"low"|"medium"|"high","tags":["topic"],"confidence":0.0-1.0}
        Task: has action verb or deadline. Note: observation/idea/thought. Default: task.
        Task title: imperative, concise. Task body: null. Priority: medium unless urgent/asap=high, no rush/someday=low.
        Note body: full content. Note ignores due_date/due_time/priority.
    """.trimIndent()

    val FEW_SHOT_EXAMPLES = """
        Input: "call mom tomorrow"
        Output: {"type":"task","title":"Call mom","body":null,"due_date":"DATE_TOMORROW","due_time":null,"priority":"medium","tags":["family"],"confidence":0.95}

        Input: "great pasta at the Italian place on 5th street"
        Output: {"type":"note","title":"Great pasta at Italian place","body":"Great pasta at the Italian place on 5th street","due_date":null,"due_time":null,"priority":"medium","tags":["food"],"confidence":0.9}
    """.trimIndent()

    fun buildUserPrompt(rawText: String, todayDate: String): String {
        val examples = FEW_SHOT_EXAMPLES.replace("DATE_TOMORROW", todayDate)
        return """
            Today's date is $todayDate.

            $examples

            Input: "$rawText"
            Output:
        """.trimIndent()
    }
}
