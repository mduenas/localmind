package com.markduenas.localmind.domain.model

enum class TaskStatus(val value: Int) {
    PENDING(0),
    COMPLETED(1),
    ARCHIVED(2);

    companion object {
        fun fromValue(value: Int): TaskStatus = entries.firstOrNull { it.value == value } ?: PENDING
    }
}
