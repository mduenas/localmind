package com.markduenas.localmind.data.local

import com.markduenas.localmind.Captures
import com.markduenas.localmind.Tags
import com.markduenas.localmind.Tasks
import com.markduenas.localmind.domain.model.Capture
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.model.Tag
import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun Tasks.toDomainTask(tags: List<Tag> = emptyList()): Task {
    return Task(
        id = id,
        title = title,
        originalText = original_text,
        dueDate = due_date?.let { epochDaysToLocalDate(it) },
        dueTime = due_time?.let { secondsToLocalTime(it) },
        priority = Priority.fromValue(priority.toInt()),
        status = TaskStatus.fromValue(status.toInt()),
        tags = tags,
        createdAt = Instant.fromEpochMilliseconds(created_at),
        updatedAt = Instant.fromEpochMilliseconds(updated_at),
        completedAt = completed_at?.let { Instant.fromEpochMilliseconds(it) },
        parsingConfidence = parsing_confidence?.toFloat()
    )
}

fun Tags.toDomainTag(): Tag {
    return Tag(
        id = id,
        name = name,
        color = color
    )
}

fun Captures.toDomainCapture(): Capture {
    return Capture(
        id = id,
        rawText = raw_text,
        audioPath = audio_path,
        createdAt = Instant.fromEpochMilliseconds(created_at),
        processed = processed != 0L
    )
}

fun LocalDate.toEpochDaysLong(): Long {
    return this.toEpochDays().toLong()
}

fun epochDaysToLocalDate(epochDays: Long): LocalDate {
    return LocalDate.fromEpochDays(epochDays.toInt())
}

fun LocalTime.toSecondOfDayLong(): Long {
    return (this.hour * 3600 + this.minute * 60 + this.second).toLong()
}

fun secondsToLocalTime(seconds: Long): LocalTime {
    val hour = (seconds / 3600).toInt()
    val minute = ((seconds % 3600) / 60).toInt()
    val second = (seconds % 60).toInt()
    return LocalTime(hour, minute, second)
}
