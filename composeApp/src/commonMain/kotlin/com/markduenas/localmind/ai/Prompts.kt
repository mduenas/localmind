package com.markduenas.localmind.ai

object Prompts {

    private val BASE_SYSTEM_PROMPT = """
        Return one JSON object only. No markdown, no prose, no <think>.
        Schema: {"type":"","title":"","body":null,"due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.0}
        type: "task"(action/remind/schedule/pay/call/book/check) or "note"(observation/idea/quote)
        title: concise summary. body: null for task, full input for note.
        due_date: YYYY-MM-DD or null. due_time: HH:MM or null (task only, when explicit).
        priority: "low"|"medium"|"high". tags: lowercase string array. confidence: 0.0-1.0.
        Resolve relative dates from today_date. Normalize time: "6pm"->"18:00". Note: due_date=null,due_time=null.
    """.trimIndent()

    val SYSTEM_PROMPT: String = BASE_SYSTEM_PROMPT

    /**
     * Returns the system prompt for a model, or null if the model should not
     * receive a separate system role (e.g. gemma3 uses few-shot user prompts instead).
     */
    fun systemPromptForModel(modelSlug: String?): String? {
        return when {
            isGemma3(modelSlug) -> null
            isQwen3(modelSlug) -> "/no_think\n$BASE_SYSTEM_PROMPT"
            else -> BASE_SYSTEM_PROMPT
        }
    }

    fun buildUserPrompt(rawText: String, todayDate: String, modelSlug: String? = null): String {
        return when {
            isGemma3(modelSlug) -> buildGemma3UserPrompt(rawText, todayDate)
            isQwen3(modelSlug) -> {
                val typeHint = typeHintFor(rawText)
                "today_date:$todayDate type_hint:$typeHint /no_think\n$rawText"
            }
            else -> {
                val typeHint = typeHintFor(rawText)
                "today_date:$todayDate type_hint:$typeHint\n$rawText"
            }
        }
    }

    /**
     * Few-shot user prompt for gemma3-270m. Tiny models respond far more reliably
     * to pattern-matching examples than to natural-language instructions.
     * System role is omitted for gemma3; all context is embedded here.
     */
    private fun buildGemma3UserPrompt(rawText: String, todayDate: String): String {
        val tomorrowDate = computeTomorrow(todayDate)
        return """
            today:$todayDate
            Input: call dentist tomorrow at 4pm
            Output: {"type":"task","title":"Call dentist","body":null,"due_date":"$tomorrowDate","due_time":"16:00","priority":"medium","tags":["health"],"confidence":0.9}
            Input: standup every day at 9am
            Output: {"type":"task","title":"Daily standup","body":null,"due_date":null,"due_time":"09:00","priority":"medium","tags":[],"confidence":0.9}
            Input: travel note - cities feel slower outside tourist zones
            Output: {"type":"note","title":"Cities feel slower outside tourist zones","body":"travel note - cities feel slower outside tourist zones","due_date":null,"due_time":null,"priority":"medium","tags":["travel"],"confidence":0.9}
            Input: $rawText
            Output:
        """.trimIndent()
    }

    private fun computeTomorrow(todayDate: String): String {
        return runCatching {
            val parts = todayDate.split("-")
            val y = parts[0].toInt(); val m = parts[1].toInt(); val d = parts[2].toInt()
            val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) daysInMonth[2] = 29
            val nextD = d + 1
            fun pad2(n: Int) = n.toString().padStart(2, '0')
            when {
                nextD <= daysInMonth[m] -> "$y-${pad2(m)}-${pad2(nextD)}"
                m < 12 -> "$y-${pad2(m + 1)}-01"
                else -> "${y + 1}-01-01"
            }
        }.getOrElse { todayDate }
    }

    private fun isGemma3(modelSlug: String?): Boolean {
        return modelSlug?.lowercase()?.startsWith("gemma3-") == true
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
