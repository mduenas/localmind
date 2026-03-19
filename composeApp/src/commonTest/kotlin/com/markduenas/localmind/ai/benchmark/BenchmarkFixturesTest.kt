package com.markduenas.localmind.ai.benchmark

import kotlin.test.Test
import kotlin.test.assertEquals

class BenchmarkFixturesTest {

    @Test
    fun fixtureHasExpectedDistribution() {
        val suite = BenchmarkFixtures.suite
        assertEquals("v1.0.0", suite.suiteVersion)
        assertEquals(20, suite.prompts.size)
        assertEquals(10, suite.prompts.count { it.type == "task" })
        assertEquals(10, suite.prompts.count { it.type == "note" })
    }
}
