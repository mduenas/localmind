package com.markduenas.localmind.domain.model

enum class Priority(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    companion object {
        fun fromValue(value: Int): Priority = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}
