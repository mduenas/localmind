package com.markduenas.localmind

import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun priorityFromValueWorks() {
        assertEquals(Priority.LOW, Priority.fromValue(0))
        assertEquals(Priority.MEDIUM, Priority.fromValue(1))
        assertEquals(Priority.HIGH, Priority.fromValue(2))
        assertEquals(Priority.MEDIUM, Priority.fromValue(99))
    }

    @Test
    fun taskStatusFromValueWorks() {
        assertEquals(TaskStatus.PENDING, TaskStatus.fromValue(0))
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromValue(1))
        assertEquals(TaskStatus.ARCHIVED, TaskStatus.fromValue(2))
        assertEquals(TaskStatus.PENDING, TaskStatus.fromValue(99))
    }
}
