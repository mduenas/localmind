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
        require(parsed.prompts.size == 200) {
            "Benchmark fixture must contain exactly 200 prompts (100 task + 100 note), found ${parsed.prompts.size}"
        }
        val taskCount = parsed.prompts.count { it.type == "task" }
        val noteCount = parsed.prompts.count { it.type == "note" }
        require(taskCount == 100 && noteCount == 100) {
            "Benchmark fixture must contain 100 task and 100 note prompts (found task=$taskCount note=$noteCount)"
        }
        parsed
    }

    // Versioned inline JSON fixture selected for benchmark reproducibility.
    private const val FIXTURE_JSON = """
{
  "suiteVersion": "v2.0.0",
  "prompts": [
    {
      "id": "task_01",
      "type": "task",
      "prompt": "remind me to call mom before dinner at 6pm",
      "expected": {
        "titleContains": [
          "call",
          "mom"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "18:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_02",
      "type": "task",
      "prompt": "book a dentist appointment on the 20th of March around 10 in the morning",
      "expected": {
        "titleContains": [
          "dentist",
          "appointment"
        ],
        "dueDateToken": "MONTH_DAY_03_20",
        "dueTime": "10:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_03",
      "type": "task",
      "prompt": "grab groceries before the family dinner this week",
      "expected": {
        "titleContains": [
          "groceries"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_04",
      "type": "task",
      "prompt": "urgent fix the production database bug",
      "expected": {
        "titleContains": [
          "database",
          "bug"
        ],
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_05",
      "type": "task",
      "prompt": "schedule team standup meeting next week at 9:30am",
      "expected": {
        "titleContains": [
          "team",
          "standup",
          "meeting"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "dueTime": "09:30",
        "priority": "medium"
      }
    },
    {
      "id": "task_06",
      "type": "task",
      "prompt": "pay electricity bill before it goes overdue this week",
      "expected": {
        "titleContains": [
          "electricity",
          "bill"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium"
      }
    },
    {
      "id": "task_07",
      "type": "task",
      "prompt": "submit quarterly report before 5pm",
      "expected": {
        "titleContains": [
          "quarterly",
          "report"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "17:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_08",
      "type": "task",
      "prompt": "clean out the garage no rush",
      "expected": {
        "titleContains": [
          "garage"
        ],
        "priority": "low",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_09",
      "type": "task",
      "prompt": "email the client proposal within the next couple of days",
      "expected": {
        "titleContains": [
          "email",
          "proposal",
          "client"
        ],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium"
      }
    },
    {
      "id": "task_10",
      "type": "task",
      "prompt": "prepare slides for investor pitch in 2 weeks",
      "expected": {
        "titleContains": [
          "slides",
          "investor",
          "pitch"
        ],
        "dueDateToken": "TODAY_PLUS_14",
        "priority": "medium",
        "tags": [
          "work",
          "presentation"
        ]
      }
    },
    {
      "id": "task_11",
      "type": "task",
      "prompt": "call the dentist to reschedule before their office closes",
      "expected": {
        "titleContains": [
          "call",
          "dentist"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_12",
      "type": "task",
      "prompt": "send Alex a birthday message first thing in the morning",
      "expected": {
        "titleContains": [
          "birthday",
          "Alex"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "medium"
      }
    },
    {
      "id": "task_13",
      "type": "task",
      "prompt": "fix login page bug high priority",
      "expected": {
        "titleContains": [
          "login",
          "bug"
        ],
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_14",
      "type": "task",
      "prompt": "pick up dry cleaning before the shop closes for the day",
      "expected": {
        "titleContains": [
          "dry cleaning"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_15",
      "type": "task",
      "prompt": "attend yoga class early morning at 7am",
      "expected": {
        "titleContains": [
          "yoga",
          "class"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "07:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_16",
      "type": "task",
      "prompt": "submit expense report today by 5pm",
      "expected": {
        "titleContains": [
          "expense",
          "report"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "17:00",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_17",
      "type": "task",
      "prompt": "pay credit card bill before the next statement closes",
      "expected": {
        "titleContains": [
          "credit card",
          "bill"
        ],
        "dueDateToken": "TODAY_PLUS_3",
        "priority": "medium"
      }
    },
    {
      "id": "task_18",
      "type": "task",
      "prompt": "schedule a haircut for sometime this week when I can fit it in",
      "expected": {
        "titleContains": [
          "haircut"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium"
      }
    },
    {
      "id": "task_19",
      "type": "task",
      "prompt": "review contract and send signature back today",
      "expected": {
        "titleContains": [
          "contract",
          "signature"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_20",
      "type": "task",
      "prompt": "make sure to water the plants before they wilt",
      "expected": {
        "titleContains": [
          "water",
          "plants"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_21",
      "type": "task",
      "prompt": "low priority sort out old clothes for donation",
      "expected": {
        "titleContains": [
          "clothes",
          "donation"
        ],
        "priority": "low",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_22",
      "type": "task",
      "prompt": "cancel the subscription before the next renewal date",
      "expected": {
        "titleContains": [
          "cancel",
          "subscription"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium"
      }
    },
    {
      "id": "task_23",
      "type": "task",
      "prompt": "deploy hotfix to production urgently today",
      "expected": {
        "titleContains": [
          "deploy",
          "hotfix",
          "production"
        ],
        "dueDateToken": "TODAY",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_24",
      "type": "task",
      "prompt": "buy anniversary gift well before the big day in a couple of weeks",
      "expected": {
        "titleContains": [
          "anniversary",
          "gift"
        ],
        "dueDateToken": "TODAY_PLUS_14",
        "priority": "medium"
      }
    },
    {
      "id": "task_25",
      "type": "task",
      "prompt": "email Q2 numbers to finance team today",
      "expected": {
        "titleContains": [
          "email",
          "finance"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_26",
      "type": "task",
      "prompt": "make sure to backup the laptop before running the software update",
      "expected": {
        "titleContains": [
          "backup",
          "laptop"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_27",
      "type": "task",
      "prompt": "check tire pressure this weekend",
      "expected": {
        "titleContains": [
          "tire",
          "pressure"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium",
        "tags": [
          "car"
        ]
      }
    },
    {
      "id": "task_28",
      "type": "task",
      "prompt": "sign up for annual conference in 30 days",
      "expected": {
        "titleContains": [
          "conference"
        ],
        "dueDateToken": "TODAY_PLUS_30",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_29",
      "type": "task",
      "prompt": "schedule the annual physical exam for sometime in the coming week",
      "expected": {
        "titleContains": [
          "physical",
          "exam"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_30",
      "type": "task",
      "prompt": "prepare welcome email for new hire tomorrow",
      "expected": {
        "titleContains": [
          "welcome",
          "email"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_31",
      "type": "task",
      "prompt": "finish reading and annotating design spec today",
      "expected": {
        "titleContains": [
          "design",
          "spec"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_32",
      "type": "task",
      "prompt": "low priority organize home office desk",
      "expected": {
        "titleContains": [
          "organize",
          "home office"
        ],
        "priority": "low",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_33",
      "type": "task",
      "prompt": "call the plumber about the kitchen leak before it causes more damage",
      "expected": {
        "titleContains": [
          "call",
          "plumber",
          "leak"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_34",
      "type": "task",
      "prompt": "set up new developer environment in 2 days",
      "expected": {
        "titleContains": [
          "developer",
          "environment"
        ],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_35",
      "type": "task",
      "prompt": "attend the board meeting on the 15th of April in the morning",
      "expected": {
        "titleContains": [
          "board",
          "meeting"
        ],
        "dueDateToken": "MONTH_DAY_04_15",
        "dueTime": "10:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_36",
      "type": "task",
      "prompt": "return the Amazon package before the return window expires",
      "expected": {
        "titleContains": [
          "return",
          "Amazon",
          "package"
        ],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium"
      }
    },
    {
      "id": "task_37",
      "type": "task",
      "prompt": "get project proposal to client by noon today #work",
      "expected": {
        "titleContains": [
          "project",
          "proposal",
          "client"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "12:00",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_38",
      "type": "task",
      "prompt": "organize garage sale for this weekend",
      "expected": {
        "titleContains": [
          "garage sale"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_39",
      "type": "task",
      "prompt": "review quarterly OKRs in 7 days",
      "expected": {
        "titleContains": [
          "quarterly",
          "OKRs"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_40",
      "type": "task",
      "prompt": "low priority clean out email inbox",
      "expected": {
        "titleContains": [
          "email",
          "inbox"
        ],
        "priority": "low",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_41",
      "type": "task",
      "prompt": "schedule team building event for next Monday",
      "expected": {
        "titleContains": [
          "team building"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_42",
      "type": "task",
      "prompt": "buy new running shoes this weekend",
      "expected": {
        "titleContains": [
          "running shoes"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium",
        "tags": [
          "fitness"
        ]
      }
    },
    {
      "id": "task_43",
      "type": "task",
      "prompt": "send monthly newsletter tomorrow morning",
      "expected": {
        "titleContains": [
          "monthly",
          "newsletter"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_44",
      "type": "task",
      "prompt": "pay property tax by December 25",
      "expected": {
        "titleContains": [
          "property tax"
        ],
        "dueDateToken": "MONTH_DAY_12_25",
        "priority": "medium",
        "tags": [
          "finance"
        ]
      }
    },
    {
      "id": "task_45",
      "type": "task",
      "prompt": "check in with the therapist sometime in the coming days",
      "expected": {
        "titleContains": [
          "therapist"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium"
      }
    },
    {
      "id": "task_46",
      "type": "task",
      "prompt": "urgent finish and submit grant application today",
      "expected": {
        "titleContains": [
          "grant",
          "application"
        ],
        "dueDateToken": "TODAY",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_47",
      "type": "task",
      "prompt": "pick up medication at the pharmacy before the day gets away",
      "expected": {
        "titleContains": [
          "medication",
          "pharmacy"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_48",
      "type": "task",
      "prompt": "low priority update LinkedIn profile photo",
      "expected": {
        "titleContains": [
          "LinkedIn",
          "profile"
        ],
        "priority": "low",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_49",
      "type": "task",
      "prompt": "set up automatic bill payments in 30 days",
      "expected": {
        "titleContains": [
          "automatic",
          "bill payments"
        ],
        "dueDateToken": "TODAY_PLUS_30",
        "priority": "medium",
        "tags": [
          "finance"
        ]
      }
    },
    {
      "id": "task_50",
      "type": "task",
      "prompt": "give mom and dad a call when there is a quiet moment this week",
      "expected": {
        "titleContains": [
          "call",
          "mom",
          "dad"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium"
      }
    },
    {
      "id": "task_51",
      "type": "task",
      "prompt": "finalize budget spreadsheet today at 3pm",
      "expected": {
        "titleContains": [
          "budget",
          "spreadsheet"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "15:00",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_52",
      "type": "task",
      "prompt": "book hotel for June 1 business trip #work",
      "expected": {
        "titleContains": [
          "book",
          "hotel"
        ],
        "dueDateToken": "MONTH_DAY_06_01",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_53",
      "type": "task",
      "prompt": "respond to client inquiry today #work",
      "expected": {
        "titleContains": [
          "client",
          "inquiry"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_54",
      "type": "task",
      "prompt": "high priority patch security vulnerability in API #work",
      "expected": {
        "titleContains": [
          "patch",
          "security",
          "vulnerability"
        ],
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_55",
      "type": "task",
      "prompt": "prepare monthly report for next Monday at 9am #work",
      "expected": {
        "titleContains": [
          "monthly",
          "report"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "dueTime": "09:00",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_56",
      "type": "task",
      "prompt": "take car in for oil change this weekend",
      "expected": {
        "titleContains": [
          "car",
          "oil change"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_57",
      "type": "task",
      "prompt": "write thank you card for gift today",
      "expected": {
        "titleContains": [
          "thank you card",
          "gift"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_58",
      "type": "task",
      "prompt": "test new feature before release tomorrow #work",
      "expected": {
        "titleContains": [
          "test",
          "feature",
          "release"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_59",
      "type": "task",
      "prompt": "low priority read and annotate research paper #research",
      "expected": {
        "titleContains": [
          "research",
          "paper"
        ],
        "priority": "low",
        "tags": [
          "research"
        ]
      }
    },
    {
      "id": "task_60",
      "type": "task",
      "prompt": "schedule post-mortem meeting in 3 days #work",
      "expected": {
        "titleContains": [
          "post",
          "meeting"
        ],
        "dueDateToken": "TODAY_PLUS_3",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_61",
      "type": "task",
      "prompt": "submit design mockups for review today by noon #work",
      "expected": {
        "titleContains": [
          "design",
          "mockups"
        ],
        "dueDateToken": "TODAY",
        "dueTime": "12:00",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_62",
      "type": "task",
      "prompt": "order office supplies for team in 7 days #work",
      "expected": {
        "titleContains": [
          "office supplies"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_63",
      "type": "task",
      "prompt": "plan birthday party for Emma in 30 days #personal",
      "expected": {
        "titleContains": [
          "birthday party",
          "Emma"
        ],
        "dueDateToken": "TODAY_PLUS_30",
        "priority": "medium",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_64",
      "type": "task",
      "prompt": "fix broken link on website today #work",
      "expected": {
        "titleContains": [
          "broken link",
          "website"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_65",
      "type": "task",
      "prompt": "review and merge code pull requests today #work",
      "expected": {
        "titleContains": [
          "review",
          "merge"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_66",
      "type": "task",
      "prompt": "set up monitoring alerts for production server #work",
      "expected": {
        "titleContains": [
          "monitoring",
          "alerts",
          "server"
        ],
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_67",
      "type": "task",
      "prompt": "drop off recycling this weekend #home",
      "expected": {
        "titleContains": [
          "recycling"
        ],
        "dueDateToken": "THIS_WEEKEND_SATURDAY",
        "priority": "medium",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_68",
      "type": "task",
      "prompt": "complete compliance training by tomorrow high priority #work",
      "expected": {
        "titleContains": [
          "compliance",
          "training"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_69",
      "type": "task",
      "prompt": "send project status update to stakeholders today #work",
      "expected": {
        "titleContains": [
          "project status",
          "stakeholders"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_70",
      "type": "task",
      "prompt": "low priority declutter kitchen drawers #home",
      "expected": {
        "titleContains": [
          "declutter",
          "kitchen"
        ],
        "priority": "low",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_71",
      "type": "task",
      "prompt": "schedule weekly sync with manager for next Monday #work",
      "expected": {
        "titleContains": [
          "weekly sync",
          "manager"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_72",
      "type": "task",
      "prompt": "buy concert tickets for September 15 show #entertainment",
      "expected": {
        "titleContains": [
          "concert",
          "tickets"
        ],
        "dueDateToken": "MONTH_DAY_09_15",
        "priority": "medium",
        "tags": [
          "entertainment"
        ]
      }
    },
    {
      "id": "task_73",
      "type": "task",
      "prompt": "write unit tests for the payment module today #work",
      "expected": {
        "titleContains": [
          "unit tests",
          "payment"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_74",
      "type": "task",
      "prompt": "pick up kids from school tomorrow at 3pm",
      "expected": {
        "titleContains": [
          "pick up",
          "kids",
          "school"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "dueTime": "15:00",
        "priority": "medium"
      }
    },
    {
      "id": "task_75",
      "type": "task",
      "prompt": "renew domain name before it expires in 7 days #work",
      "expected": {
        "titleContains": [
          "renew",
          "domain"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_76",
      "type": "task",
      "prompt": "high priority finalize acquisition deal terms today #work",
      "expected": {
        "titleContains": [
          "acquisition",
          "deal"
        ],
        "dueDateToken": "TODAY",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_77",
      "type": "task",
      "prompt": "coordinate delivery with vendor in 2 days",
      "expected": {
        "titleContains": [
          "vendor",
          "delivery"
        ],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium"
      }
    },
    {
      "id": "task_78",
      "type": "task",
      "prompt": "low priority learn vim keyboard shortcuts #productivity",
      "expected": {
        "titleContains": [
          "keyboard shortcuts"
        ],
        "priority": "low",
        "tags": [
          "productivity"
        ]
      }
    },
    {
      "id": "task_79",
      "type": "task",
      "prompt": "buy Valentine's Day gift by February 14 #personal",
      "expected": {
        "titleContains": [
          "Valentine",
          "gift"
        ],
        "dueDateToken": "MONTH_DAY_02_14",
        "priority": "medium",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_80",
      "type": "task",
      "prompt": "organize team lunch for next week #work",
      "expected": {
        "titleContains": [
          "team lunch"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_81",
      "type": "task",
      "prompt": "send follow-up email to recruiter today #work",
      "expected": {
        "titleContains": [
          "follow",
          "email",
          "recruiter"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_82",
      "type": "task",
      "prompt": "update project roadmap before board meeting tomorrow #work",
      "expected": {
        "titleContains": [
          "project roadmap",
          "board meeting"
        ],
        "dueDateToken": "TODAY_PLUS_1",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_83",
      "type": "task",
      "prompt": "fill out health insurance forms in 3 days #personal",
      "expected": {
        "titleContains": [
          "health insurance",
          "forms"
        ],
        "dueDateToken": "TODAY_PLUS_3",
        "priority": "medium",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_84",
      "type": "task",
      "prompt": "schedule dentist cleaning for kids next week",
      "expected": {
        "titleContains": [
          "dentist",
          "cleaning",
          "kids"
        ],
        "dueDateToken": "NEXT_WEEK_MONDAY",
        "priority": "medium"
      }
    },
    {
      "id": "task_85",
      "type": "task",
      "prompt": "review vendor contract renewal in 7 days #work",
      "expected": {
        "titleContains": [
          "vendor",
          "contract"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_86",
      "type": "task",
      "prompt": "prepare keynote talk slides by July 4 #work #conference",
      "expected": {
        "titleContains": [
          "keynote",
          "slides"
        ],
        "dueDateToken": "MONTH_DAY_07_04",
        "priority": "medium",
        "tags": [
          "work",
          "conference"
        ]
      }
    },
    {
      "id": "task_87",
      "type": "task",
      "prompt": "urgent resolve production outage now #work",
      "expected": {
        "titleContains": [
          "resolve",
          "production",
          "outage"
        ],
        "dueDateToken": "TODAY",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_88",
      "type": "task",
      "prompt": "low priority reorganize bookshelves in bedroom #home",
      "expected": {
        "titleContains": [
          "bookshelves"
        ],
        "priority": "low",
        "tags": [
          "home"
        ]
      }
    },
    {
      "id": "task_89",
      "type": "task",
      "prompt": "send weekly status update email to team today #work",
      "expected": {
        "titleContains": [
          "weekly status",
          "team"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_90",
      "type": "task",
      "prompt": "book conference room for next month planning session #work",
      "expected": {
        "titleContains": [
          "conference room",
          "planning"
        ],
        "dueDateToken": "TODAY_PLUS_30",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_91",
      "type": "task",
      "prompt": "apply for travel credit card today #finance",
      "expected": {
        "titleContains": [
          "credit card"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "finance"
        ]
      }
    },
    {
      "id": "task_92",
      "type": "task",
      "prompt": "attend product launch event on October 31 #work",
      "expected": {
        "titleContains": [
          "product launch",
          "event"
        ],
        "dueDateToken": "MONTH_DAY_10_31",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_93",
      "type": "task",
      "prompt": "urgent escalate customer complaint to support manager today #work",
      "expected": {
        "titleContains": [
          "escalate",
          "customer",
          "complaint"
        ],
        "dueDateToken": "TODAY",
        "priority": "high",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_94",
      "type": "task",
      "prompt": "file expense report before Friday in 3 days #work",
      "expected": {
        "titleContains": [
          "expense report"
        ],
        "dueDateToken": "TODAY_PLUS_3",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_95",
      "type": "task",
      "prompt": "pick up prescription at pharmacy in 2 days",
      "expected": {
        "titleContains": [
          "prescription",
          "pharmacy"
        ],
        "dueDateToken": "TODAY_PLUS_2",
        "priority": "medium"
      }
    },
    {
      "id": "task_96",
      "type": "task",
      "prompt": "low priority update software on old laptop #personal",
      "expected": {
        "titleContains": [
          "update",
          "software"
        ],
        "priority": "low",
        "tags": [
          "personal"
        ]
      }
    },
    {
      "id": "task_97",
      "type": "task",
      "prompt": "set reminder for tax filing deadline January 15 #finance",
      "expected": {
        "titleContains": [
          "tax",
          "deadline"
        ],
        "dueDateToken": "MONTH_DAY_01_15",
        "priority": "medium",
        "tags": [
          "finance"
        ]
      }
    },
    {
      "id": "task_98",
      "type": "task",
      "prompt": "complete onboarding checklist for new job today #work",
      "expected": {
        "titleContains": [
          "onboarding",
          "checklist"
        ],
        "dueDateToken": "TODAY",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "task_99",
      "type": "task",
      "prompt": "book flight to Austin in 30 days #travel",
      "expected": {
        "titleContains": [
          "flight",
          "Austin"
        ],
        "dueDateToken": "TODAY_PLUS_30",
        "priority": "medium",
        "tags": [
          "travel"
        ]
      }
    },
    {
      "id": "task_100",
      "type": "task",
      "prompt": "prepare for performance review in 7 days #work",
      "expected": {
        "titleContains": [
          "performance review"
        ],
        "dueDateToken": "TODAY_PLUS_7",
        "priority": "medium",
        "tags": [
          "work"
        ]
      }
    },
    {
      "id": "note_01",
      "type": "note",
      "prompt": "definitely check out the sushi place on Main Street it was amazing",
      "expected": {
        "bodyContains": [
          "sushi",
          "Main Street"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_02",
      "type": "note",
      "prompt": "write a note on how Kotlin coroutines structured concurrency prevents leaked jobs",
      "expected": {
        "bodyContains": [
          "Kotlin",
          "coroutines",
          "structured concurrency"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_03",
      "type": "note",
      "prompt": "build a plant watering sensor using raspberry pi for a fun weekend project idea",
      "expected": {
        "bodyContains": [
          "plant",
          "raspberry",
          "project"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_04",
      "type": "note",
      "prompt": "review Atomic Habits for its great advice on habit stacking really insightful",
      "expected": {
        "bodyContains": [
          "Atomic Habits",
          "habit stacking"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_05",
      "type": "note",
      "prompt": "check the team decision: migrate to Postgres before Q3",
      "expected": {
        "bodyContains": [
          "team",
          "Postgres",
          "Q3"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_06",
      "type": "note",
      "prompt": "build this idea: one-tap grocery history list synced by household",
      "expected": {
        "bodyContains": [
          "grocery",
          "household"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_07",
      "type": "note",
      "prompt": "book quote: simplicity is prerequisite for reliability",
      "expected": {
        "bodyContains": [
          "simplicity",
          "reliability"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_08",
      "type": "note",
      "prompt": "review customer interview takeaway: onboarding confusion around reminders",
      "expected": {
        "bodyContains": [
          "interview",
          "onboarding",
          "reminders"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_09",
      "type": "note",
      "prompt": "visit Boise in early fall for perfect running weather",
      "expected": {
        "bodyContains": [
          "Boise",
          "fall",
          "running weather"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_10",
      "type": "note",
      "prompt": "review retro note: focus on reducing friction in quick capture flow",
      "expected": {
        "bodyContains": [
          "reducing friction",
          "quick capture"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_11",
      "type": "note",
      "prompt": "visit the new coffee shop downtown for its incredible pour-over technique",
      "expected": {
        "bodyContains": [
          "coffee shop",
          "pour-over"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_12",
      "type": "note",
      "prompt": "learned about SOLID principles in today's architecture review session",
      "expected": {
        "bodyContains": [
          "SOLID",
          "architecture"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_13",
      "type": "note",
      "prompt": "build this idea: mobile app that tracks plant watering schedules using sensors",
      "expected": {
        "bodyContains": [
          "plant",
          "sensors"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_14",
      "type": "note",
      "prompt": "book takeaway: the key to habit change is environment design not willpower",
      "expected": {
        "bodyContains": [
          "habit change",
          "environment design"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_15",
      "type": "note",
      "prompt": "review team discussion outcome: adopt trunk-based development to reduce merge conflicts",
      "expected": {
        "bodyContains": [
          "trunk-based",
          "merge conflicts"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_16",
      "type": "note",
      "prompt": "idea: subscription service for curated monthly book and tea boxes",
      "expected": {
        "bodyContains": [
          "subscription",
          "book",
          "tea"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_17",
      "type": "note",
      "prompt": "write down mentor quote: move fast but never break trust",
      "expected": {
        "bodyContains": [
          "trust",
          "fast"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_18",
      "type": "note",
      "prompt": "review user research finding: people abandon onboarding when asked for credit card upfront",
      "expected": {
        "bodyContains": [
          "credit card",
          "onboarding"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_19",
      "type": "note",
      "prompt": "visit Tokyo and note its public transit as a masterclass in efficiency and design",
      "expected": {
        "bodyContains": [
          "Tokyo",
          "transit",
          "efficiency"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_20",
      "type": "note",
      "prompt": "review retro insight: demo day prep always reveals gaps that daily standups miss",
      "expected": {
        "bodyContains": [
          "demo day",
          "standups"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_21",
      "type": "note",
      "prompt": "write podcast note: cognitive load theory says chunking information improves retention significantly",
      "expected": {
        "bodyContains": [
          "cognitive load",
          "information",
          "retention"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_22",
      "type": "note",
      "prompt": "build this idea: shared family chore tracker app with points and rewards for kids",
      "expected": {
        "bodyContains": [
          "family",
          "chore",
          "rewards"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_23",
      "type": "note",
      "prompt": "check observation: remote teams communicate better using async video than Slack messages",
      "expected": {
        "bodyContains": [
          "remote teams",
          "async",
          "video"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_24",
      "type": "note",
      "prompt": "book quote: a complex system that works evolved from a simple system that worked",
      "expected": {
        "bodyContains": [
          "complex system",
          "simple system"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_25",
      "type": "note",
      "prompt": "review customer feedback: settings screen is overwhelming and users want fewer options",
      "expected": {
        "bodyContains": [
          "settings",
          "overwhelming",
          "options"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_26",
      "type": "note",
      "prompt": "check out this: one hour nature walk without phone restored focus better than any meditation app",
      "expected": {
        "bodyContains": [
          "phone",
          "focus",
          "meditation"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_27",
      "type": "note",
      "prompt": "review technical discovery: SQLite WAL mode dramatically improves concurrent read performance",
      "expected": {
        "bodyContains": [
          "SQLite",
          "WAL",
          "concurrent"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_28",
      "type": "note",
      "prompt": "review retro note: sprint velocity improved after we removed same-day tickets from the board",
      "expected": {
        "bodyContains": [
          "sprint velocity",
          "tickets"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_29",
      "type": "note",
      "prompt": "build this idea: AI that generates weekly meal plans from what is already in your fridge",
      "expected": {
        "bodyContains": [
          "meal plans",
          "fridge"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_30",
      "type": "note",
      "prompt": "write up meeting insight: people disengage in long planning sessions after the first 45 minutes",
      "expected": {
        "bodyContains": [
          "planning sessions",
          "disengage"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_31",
      "type": "note",
      "prompt": "visit Lisbon and note how its architecture blends Gothic Moorish and modern styles seamlessly",
      "expected": {
        "bodyContains": [
          "Lisbon",
          "architecture"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_32",
      "type": "note",
      "prompt": "book takeaway: deep work requires protecting uninterrupted blocks of at least 90 minutes",
      "expected": {
        "bodyContains": [
          "deep work",
          "90 minutes"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_33",
      "type": "note",
      "prompt": "check development note: Flutter hot reload is a game changer for UI iteration speed",
      "expected": {
        "bodyContains": [
          "Flutter",
          "hot reload",
          "UI"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_34",
      "type": "note",
      "prompt": "idea: a journaling app that asks one reflective question per day at sunset",
      "expected": {
        "bodyContains": [
          "journaling",
          "reflective",
          "sunset"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_35",
      "type": "note",
      "prompt": "user test observation: users tap the wrong button consistently when two CTAs are close",
      "expected": {
        "bodyContains": [
          "users",
          "button",
          "CTAs"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_36",
      "type": "note",
      "prompt": "food thought: the ramen at Ichiran felt like a private dining experience unlike anything else",
      "expected": {
        "bodyContains": [
          "ramen",
          "Ichiran",
          "dining"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_37",
      "type": "note",
      "prompt": "quote: the best tool is the one you actually use not the one with the most features",
      "expected": {
        "bodyContains": [
          "best tool",
          "features"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_38",
      "type": "note",
      "prompt": "team learning: rotating meeting facilitators builds leadership skills across the whole team",
      "expected": {
        "bodyContains": [
          "meeting facilitators",
          "leadership"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_39",
      "type": "note",
      "prompt": "insight: asking why five times almost always leads to the real root cause",
      "expected": {
        "bodyContains": [
          "why",
          "root cause"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_40",
      "type": "note",
      "prompt": "travel note: Melbourne laneway coffee culture is worth visiting for a whole month",
      "expected": {
        "bodyContains": [
          "Melbourne",
          "coffee",
          "laneway"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_41",
      "type": "note",
      "prompt": "reading note: Thinking Fast and Slow shows anchoring bias affects all decisions not just risky ones",
      "expected": {
        "bodyContains": [
          "Thinking Fast",
          "anchoring bias"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_42",
      "type": "note",
      "prompt": "idea: offline-first habit tracker with weekly coach-style summary using on-device ML",
      "expected": {
        "bodyContains": [
          "offline",
          "habit tracker",
          "on-device"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_43",
      "type": "note",
      "prompt": "pair programming observation: more bugs are caught when one person types and one reviews live",
      "expected": {
        "bodyContains": [
          "pair programming",
          "bugs"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_44",
      "type": "note",
      "prompt": "quote from design book: good design is invisible bad design is everywhere",
      "expected": {
        "bodyContains": [
          "design",
          "invisible"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_45",
      "type": "note",
      "prompt": "customer interview: power users want an export to CSV feature more than anything else",
      "expected": {
        "bodyContains": [
          "export",
          "CSV",
          "power users"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_46",
      "type": "note",
      "prompt": "idea: browser extension that removes recommended videos from YouTube to reduce distraction",
      "expected": {
        "bodyContains": [
          "browser extension",
          "YouTube",
          "distraction"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_47",
      "type": "note",
      "prompt": "architecture decision: migrating to a monorepo reduced CI build time by 40 percent",
      "expected": {
        "bodyContains": [
          "monorepo",
          "CI",
          "build time"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_48",
      "type": "note",
      "prompt": "food discovery: sourdough with long cold fermentation has significantly better flavor",
      "expected": {
        "bodyContains": [
          "sourdough",
          "cold fermentation",
          "flavor"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_49",
      "type": "note",
      "prompt": "retro note: having a single shared roadmap prevented duplicate work across three teams",
      "expected": {
        "bodyContains": [
          "roadmap",
          "duplicate work"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_50",
      "type": "note",
      "prompt": "idea: a community platform for early retirees to share skills and projects",
      "expected": {
        "bodyContains": [
          "community",
          "platform",
          "retirees"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_51",
      "type": "note",
      "prompt": "learned today: CSS container queries enable truly component-level responsive design",
      "expected": {
        "bodyContains": [
          "CSS",
          "container queries",
          "responsive"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_52",
      "type": "note",
      "prompt": "observation: people who exercise in the morning report higher productivity throughout the day",
      "expected": {
        "bodyContains": [
          "exercise",
          "morning",
          "productivity"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_53",
      "type": "note",
      "prompt": "book insight from Essentialism: clarity about what to eliminate is as important as what to pursue",
      "expected": {
        "bodyContains": [
          "Essentialism",
          "clarity",
          "eliminate"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_54",
      "type": "note",
      "prompt": "idea: local neighborhood tool-sharing app to reduce redundant household purchases",
      "expected": {
        "bodyContains": [
          "neighborhood",
          "tool-sharing",
          "purchases"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_55",
      "type": "note",
      "prompt": "technical note: lazy initialization in Kotlin using by lazy is thread-safe by default",
      "expected": {
        "bodyContains": [
          "Kotlin",
          "lazy",
          "thread-safe"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_56",
      "type": "note",
      "prompt": "user feedback: notifications clustered by context rather than time feel less overwhelming",
      "expected": {
        "bodyContains": [
          "notifications",
          "context",
          "overwhelming"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_57",
      "type": "note",
      "prompt": "meeting note: backend team agreed to deprecate legacy REST endpoints by Q4",
      "expected": {
        "bodyContains": [
          "backend",
          "REST",
          "Q4"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_58",
      "type": "note",
      "prompt": "idea: collaborative playlist app where each person adds one song per day",
      "expected": {
        "bodyContains": [
          "collaborative",
          "playlist",
          "song"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_59",
      "type": "note",
      "prompt": "travel thought: hiking Na Pali Coast at sunrise was among the most spiritual experiences ever",
      "expected": {
        "bodyContains": [
          "Na Pali Coast",
          "sunrise",
          "spiritual"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_60",
      "type": "note",
      "prompt": "retrospective insight: teams that celebrate small wins consistently outperform big-milestone-only teams",
      "expected": {
        "bodyContains": [
          "small wins",
          "milestones"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_61",
      "type": "note",
      "prompt": "book takeaway: Never Split the Difference teaches mirroring and labeling as negotiation tools",
      "expected": {
        "bodyContains": [
          "negotiation",
          "mirroring",
          "labeling"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_62",
      "type": "note",
      "prompt": "idea: smart thermostat that learns from occupancy data rather than just static schedules",
      "expected": {
        "bodyContains": [
          "thermostat",
          "occupancy",
          "schedules"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_63",
      "type": "note",
      "prompt": "code review observation: comments asking why code was written catch more bugs than style checks",
      "expected": {
        "bodyContains": [
          "code review",
          "comments",
          "bugs"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_64",
      "type": "note",
      "prompt": "quote: if something is important enough to complain about it is important enough to fix",
      "expected": {
        "bodyContains": [
          "complain",
          "fix"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_65",
      "type": "note",
      "prompt": "product insight: the apps users love most solve one problem exceptionally well",
      "expected": {
        "bodyContains": [
          "apps",
          "solve",
          "exceptionally"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_66",
      "type": "note",
      "prompt": "idea: AI voice journaling that summarizes thoughts into structured action items overnight",
      "expected": {
        "bodyContains": [
          "AI voice",
          "journaling",
          "action items"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_67",
      "type": "note",
      "prompt": "observation: walking meetings produce more creative ideas than sitting in a conference room",
      "expected": {
        "bodyContains": [
          "walking meetings",
          "creative",
          "conference room"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_68",
      "type": "note",
      "prompt": "learning note: Rust ownership model eliminates entire classes of memory bugs at compile time",
      "expected": {
        "bodyContains": [
          "Rust",
          "ownership",
          "memory bugs"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_69",
      "type": "note",
      "prompt": "travel note: taking a local bus through rural Portugal felt more authentic than any tour",
      "expected": {
        "bodyContains": [
          "Portugal",
          "local bus",
          "authentic"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_70",
      "type": "note",
      "prompt": "retro note: engineering velocity dropped after we added too many approval steps to deploys",
      "expected": {
        "bodyContains": [
          "engineering velocity",
          "approval",
          "deploy"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_71",
      "type": "note",
      "prompt": "podcast insight: reading nonfiction before email each morning primes creative thinking all day",
      "expected": {
        "bodyContains": [
          "nonfiction",
          "email",
          "creative"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_72",
      "type": "note",
      "prompt": "idea: smart garden kit that pairs with an app to guide first-time vegetable growers step by step",
      "expected": {
        "bodyContains": [
          "smart garden",
          "vegetable",
          "step by step"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_73",
      "type": "note",
      "prompt": "observation: teams that document decisions make onboarding for new engineers much faster",
      "expected": {
        "bodyContains": [
          "document",
          "decisions",
          "onboarding"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_74",
      "type": "note",
      "prompt": "book quote: we do not rise to the level of our goals we fall to the level of our systems",
      "expected": {
        "bodyContains": [
          "goals",
          "systems"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_75",
      "type": "note",
      "prompt": "customer interview: people want to capture ideas hands-free while driving or cooking",
      "expected": {
        "bodyContains": [
          "capture ideas",
          "hands-free",
          "driving"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_76",
      "type": "note",
      "prompt": "idea: app to help freelancers estimate project time using historical data from past projects",
      "expected": {
        "bodyContains": [
          "freelancers",
          "estimate",
          "historical data"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_77",
      "type": "note",
      "prompt": "technical note: sealed interfaces in Kotlin for state modeling reduce boilerplate significantly",
      "expected": {
        "bodyContains": [
          "sealed interfaces",
          "Kotlin",
          "state modeling"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_78",
      "type": "note",
      "prompt": "food thought: the dim sum at Dragon Beaux has the best shrimp dumplings I have ever had",
      "expected": {
        "bodyContains": [
          "dim sum",
          "shrimp dumplings"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_79",
      "type": "note",
      "prompt": "retro observation: code review cycle time is the single biggest blocker to shipping faster",
      "expected": {
        "bodyContains": [
          "code review",
          "cycle time",
          "shipping"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_80",
      "type": "note",
      "prompt": "idea: community ambient music app where anyone can contribute a recorded soundscape",
      "expected": {
        "bodyContains": [
          "ambient music",
          "soundscape",
          "community"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_81",
      "type": "note",
      "prompt": "book insight: The Mom Test says stop validating your idea and start discovering what users do",
      "expected": {
        "bodyContains": [
          "The Mom Test",
          "validating",
          "users"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_82",
      "type": "note",
      "prompt": "technical discovery: WebSockets are overkill for most real-time features polling every 5 seconds works",
      "expected": {
        "bodyContains": [
          "WebSockets",
          "polling",
          "real-time"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_83",
      "type": "note",
      "prompt": "idea: a wearable device that gives haptic nudge when you have been sitting for over an hour",
      "expected": {
        "bodyContains": [
          "wearable",
          "haptic",
          "sitting"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_84",
      "type": "note",
      "prompt": "travel thought: Amsterdam in early spring is quieter and cheaper than summer but just as beautiful",
      "expected": {
        "bodyContains": [
          "Amsterdam",
          "spring",
          "summer"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_85",
      "type": "note",
      "prompt": "observation: giving design feedback in writing is more precise and less intimidating than verbal",
      "expected": {
        "bodyContains": [
          "design feedback",
          "writing",
          "verbal"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_86",
      "type": "note",
      "prompt": "idea: passive savings app that rounds up purchases to the nearest dollar and invests the difference",
      "expected": {
        "bodyContains": [
          "savings",
          "rounds up",
          "invests"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_87",
      "type": "note",
      "prompt": "learning: understanding TCP handshake behavior helped diagnose a subtle connection timeout bug",
      "expected": {
        "bodyContains": [
          "TCP",
          "handshake",
          "connection timeout"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_88",
      "type": "note",
      "prompt": "retrospective note: automating test data setup reduced test flakiness by 80 percent",
      "expected": {
        "bodyContains": [
          "test data",
          "flakiness"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_89",
      "type": "note",
      "prompt": "idea: AI writing assistant that matches your personal tone from past writing samples",
      "expected": {
        "bodyContains": [
          "AI",
          "writing assistant",
          "personal tone"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_90",
      "type": "note",
      "prompt": "observation: cities with good public transit tend to have stronger local restaurant scenes",
      "expected": {
        "bodyContains": [
          "public transit",
          "restaurant",
          "cities"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_91",
      "type": "note",
      "prompt": "book quote: the goal of refactoring is not to add features but to make the next change easier",
      "expected": {
        "bodyContains": [
          "refactoring",
          "features",
          "change"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_92",
      "type": "note",
      "prompt": "idea: an e-ink daily planning pad that syncs wirelessly with your phone calendar",
      "expected": {
        "bodyContains": [
          "e-ink",
          "planning",
          "calendar"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_93",
      "type": "note",
      "prompt": "travel insight: hiring a local guide for half a day in Marrakech unlocked places no map shows",
      "expected": {
        "bodyContains": [
          "Marrakech",
          "local guide"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_94",
      "type": "note",
      "prompt": "meeting outcome: data team will build a unified event schema before end of quarter",
      "expected": {
        "bodyContains": [
          "data team",
          "event schema",
          "quarter"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_95",
      "type": "note",
      "prompt": "idea: peer-to-peer skill exchange app where users trade lessons without money",
      "expected": {
        "bodyContains": [
          "skill exchange",
          "lessons",
          "trade"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_96",
      "type": "note",
      "prompt": "technical note: immutable data structures in functional programming make concurrent code safer",
      "expected": {
        "bodyContains": [
          "immutable",
          "functional programming",
          "concurrent"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_97",
      "type": "note",
      "prompt": "observation: people who take handwritten notes in meetings remember decisions better than those typing",
      "expected": {
        "bodyContains": [
          "handwritten notes",
          "decisions",
          "typing"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_98",
      "type": "note",
      "prompt": "idea: subscription model for high-quality secondhand children's clothes sorted by size",
      "expected": {
        "bodyContains": [
          "subscription",
          "secondhand",
          "children"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_99",
      "type": "note",
      "prompt": "podcast insight: founders who talk to ten customers per week build better products than those who talk to none",
      "expected": {
        "bodyContains": [
          "founders",
          "customers",
          "products"
        ],
        "bodyRetainsInput": true
      }
    },
    {
      "id": "note_100",
      "type": "note",
      "prompt": "retro note: shipping a feature flag system first gave us flexibility to iterate without fear of breaking production",
      "expected": {
        "bodyContains": [
          "feature flag",
          "iterate",
          "production"
        ],
        "bodyRetainsInput": true
      }
    }
  ]
}
"""
}
