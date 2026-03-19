package com.markduenas.localmind.ai.benchmark

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BenchmarkSuiteFixture(
    val suiteVersion: String,
    val prompts: List<BenchmarkPromptFixture>,
)

@Serializable
data class BenchmarkPromptFixture(
    val id: String,
    val type: String,
    val prompt: String,
    val expected: BenchmarkExpected,
)

@Serializable
data class BenchmarkExpected(
    val titleContains: List<String> = emptyList(),
    val dueDateToken: String? = null,
    val dueTime: String? = null,
    val priority: String? = null,
    val tags: List<String>? = null,
    val bodyContains: List<String> = emptyList(),
    val bodyRetainsInput: Boolean = false,
)

object BenchmarkFixtures {
    private val fixtureJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val suite: BenchmarkSuiteFixture by lazy {
        val parsed = fixtureJson.decodeFromString<BenchmarkSuiteFixture>(FIXTURE_JSON)
        require(parsed.prompts.size == 20) {
            "Benchmark fixture must contain exactly 20 prompts (10 task + 10 note), found ${parsed.prompts.size}"
        }
        val taskCount = parsed.prompts.count { it.type == "task" }
        val noteCount = parsed.prompts.count { it.type == "note" }
        require(taskCount == 10 && noteCount == 10) {
            "Benchmark fixture must contain 10 task and 10 note prompts (found task=$taskCount note=$noteCount)"
        }
        parsed
    }

    // Versioned inline JSON fixture selected for benchmark reproducibility.
    private const val FIXTURE_JSON = """
{
  "suiteVersion": "v1.0.0",
  "prompts": [
    {
      "id": "task_01",
      "type": "task",
      "prompt": "remind me to call mom tomorrow at 6pm",
      "expected": {
        "titleContains": ["call", "mom"],
        "dueDateToken": "TODAY_PLUS_1",
        "dueTime": "18:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_02",
      "type": "task",
      "prompt": "book dentist appointment March 20 at 10am",
      "expected": {
        "titleContains": ["dentist", "appointment"],
        "dueDateToken": "MONTH_DAY_03_20",
        "dueTime": "10:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_03",
      "type": "task",
      "prompt": "pick up groceries this weekend",
      "expected": {
        "titleContains": ["groceries"],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_04",
      "type": "task",
      "prompt": "urgent fix the production database bug #work",
      "expected": {
        "titleContains": ["database", "bug"],
        "priority": "high",
        "tags": ["work"]
      }
    },
    {
      "id": "task_05",
      "type": "task",
      "prompt": "schedule team standup meeting next week at 9:30am",
      "expected": {
        "titleContains": ["team", "standup", "meeting"],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "dueTime": "09:30",
        "priority": "medium"
      }
    },
    {
      "id": "task_06",
      "type": "task",
      "prompt": "pay electricity bill in 3 days",
      "expected": {
        "titleContains": ["electricity", "bill"],
        "dueDateToken": "TODAY_PLUS_3",
        "priority": "medium"
      }
    },
    {
      "id": "task_07",
      "type": "task",
      "prompt": "submit quarterly report today before 5pm",
      "expected": {
        "titleContains": ["quarterly", "report"],
        "dueDateToken": "TODAY",
        "dueTime": "17:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_08",
      "type": "task",
      "prompt": "clean out the garage no rush #home",
      "expected": {
        "titleContains": ["garage"],
        "priority": "low",
        "tags": ["home"]
      }
    },
    {
      "id": "task_09",
      "type": "task",
      "prompt": "email the proposal to the client day after tomorrow",
      "expected": {
        "titleContains": ["email", "proposal", "client"],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium"
      }
    },
    {
      "id": "task_10",
      "type": "task",
      "prompt": "prepare slides for investor pitch in 2 weeks #work #presentation",
      "expected": {
        "titleContains": ["slides", "investor", "pitch"],
        "dueDateToken": "TODAY_PLUS_14",
        "priority": "medium",
        "tags": ["work", "presentation"]
      }
    },
    {
      "id": "note_01",
      "type": "note",
      "prompt": "the sushi place on Main Street was amazing definitely go back",
      "expected": {
        "bodyContains": ["sushi", "Main Street"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_02",
      "type": "note",
      "prompt": "Kotlin coroutines structured concurrency prevents leaked jobs really well",
      "expected": {
        "bodyContains": ["Kotlin", "coroutines", "structured concurrency"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_03",
      "type": "note",
      "prompt": "a plant watering sensor with raspberry pi would be a cool weekend project",
      "expected": {
        "bodyContains": ["plant", "raspberry", "project"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_04",
      "type": "note",
      "prompt": "Atomic Habits has great advice on habit stacking really insightful",
      "expected": {
        "bodyContains": ["Atomic Habits", "habit stacking"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_05",
      "type": "note",
      "prompt": "the team agreed we should migrate to Postgres before Q3",
      "expected": {
        "bodyContains": ["team", "Postgres", "Q3"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_06",
      "type": "note",
      "prompt": "idea: one-tap grocery history list synced by household",
      "expected": {
        "bodyContains": ["grocery", "household"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_07",
      "type": "note",
      "prompt": "book quote: simplicity is prerequisite for reliability",
      "expected": {
        "bodyContains": ["simplicity", "reliability"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_08",
      "type": "note",
      "prompt": "customer interview takeaway: onboarding confusion around reminders",
      "expected": {
        "bodyContains": ["interview", "onboarding", "reminders"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_09",
      "type": "note",
      "prompt": "travel thought: Boise in early fall has perfect running weather",
      "expected": {
        "bodyContains": ["Boise", "fall", "running weather"],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_10",
      "type": "note",
      "prompt": "retro note: focus on reducing friction in quick capture flow",
      "expected": {
        "bodyContains": ["reducing friction", "quick capture"],
        "bodyRetainsInput": true
      }
    }
  ]
}
"""
}
