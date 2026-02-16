package com.markduenas.localmind.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Today : Screen()

    @Serializable
    data object Upcoming : Screen()

    @Serializable
    data object AllTasks : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Capture : Screen()

    @Serializable
    data class ParseReview(val captureText: String) : Screen()
}
