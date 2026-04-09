package com.markduenas.localmind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.markduenas.localmind.ui.calendar.CalendarScreen
import com.markduenas.localmind.ui.capture.CaptureScreen
import com.markduenas.localmind.ui.notes.NoteDetailScreen
import com.markduenas.localmind.ui.notes.NotesScreen
import com.markduenas.localmind.ui.review.ParseReviewScreen
import com.markduenas.localmind.ui.review.SaveResult
import com.markduenas.localmind.ui.settings.SettingsScreen
import com.markduenas.localmind.ui.tasks.AllTasksScreen
import com.markduenas.localmind.ui.tasks.TaskDetailScreen
import com.markduenas.localmind.ui.tasks.TodayScreen
import com.markduenas.localmind.ui.tasks.UpcomingScreen
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

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
        composable<Screen.Calendar> {
            CalendarScreen(
                onNavigateToTask = { taskId ->
                    navController.navigate(Screen.TaskDetail(taskId))
                },
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.NoteDetail(noteId))
                },
            )
        }
        composable<Screen.Upcoming> {
            UpcomingScreen()
        }
        composable<Screen.AllTasks> {
            AllTasksScreen()
        }
        composable<Screen.Notes> {
            NotesScreen()
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
                onSaved = { result ->
                    val destination = when (result) {
                        is SaveResult.NoteSaved -> Screen.Notes
                        is SaveResult.TaskSaved -> {
                            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                            val dueDate = result.dueDate
                            when {
                                dueDate == null || dueDate == today || dueDate < today -> Screen.Today
                                else -> Screen.Upcoming
                            }
                        }
                    }
                    navController.navigate(destination) {
                        popUpTo<Screen.Today> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onDiscard = { navController.popBackStack() },
            )
        }
        composable<Screen.TaskDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.TaskDetail>()
            TaskDetailScreen(
                taskId = route.taskId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Screen.NoteDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.NoteDetail>()
            NoteDetailScreen(
                noteId = route.noteId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
