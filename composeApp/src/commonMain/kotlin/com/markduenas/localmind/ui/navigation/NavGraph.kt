package com.markduenas.localmind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.markduenas.localmind.ui.capture.CaptureScreen
import com.markduenas.localmind.ui.review.ParseReviewScreen
import com.markduenas.localmind.ui.settings.SettingsScreen
import com.markduenas.localmind.ui.tasks.AllTasksScreen
import com.markduenas.localmind.ui.tasks.TodayScreen
import com.markduenas.localmind.ui.tasks.UpcomingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Today,
        modifier = modifier,
    ) {
        composable<Screen.Today> {
            TodayScreen()
        }
        composable<Screen.Upcoming> {
            UpcomingScreen()
        }
        composable<Screen.AllTasks> {
            AllTasksScreen()
        }
        composable<Screen.Settings> {
            SettingsScreen()
        }
        composable<Screen.Capture> {
            CaptureScreen(
                onSubmit = { captureText ->
                    navController.navigate(Screen.ParseReview(captureText)) {
                        popUpTo<Screen.Capture> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Screen.ParseReview> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ParseReview>()
            ParseReviewScreen(
                captureText = route.captureText,
                onSaved = {
                    navController.navigate(Screen.Today) {
                        popUpTo<Screen.Today> { inclusive = true }
                    }
                },
                onDiscard = { navController.popBackStack() },
            )
        }
    }
}
