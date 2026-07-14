package com.markduenas.localmind.ai.benchmark

import kotlin.test.Test
import kotlin.test.assertEquals

class BenchmarkFixturesTest {

    @Test
    fun fixtureHasExpectedDistribution() {
        val suite = BenchmarkFixtures.suite
        assertEquals("v2.0.0", suite.suiteVersion)
        assertEquals(200, suite.prompts.size)
        assertEquals(100, suite.prompts.count { it.type == "task" })
        assertEquals(100, suite.prompts.count { it.type == "note" })
    }
}
