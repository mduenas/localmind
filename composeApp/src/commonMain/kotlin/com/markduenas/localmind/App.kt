package com.markduenas.localmind

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.markduenas.localmind.domain.usecase.DrainCaptureQueueUseCase
import com.markduenas.localmind.ui.components.FloatingCaptureButton
import com.markduenas.localmind.ui.navigation.BottomNavBar
import com.markduenas.localmind.ui.navigation.NavGraph
import com.markduenas.localmind.ui.navigation.Screen
import com.markduenas.localmind.ui.navigation.bottomNavItems
import com.markduenas.localmind.ui.theme.LocalMindTheme
import localmind.composeapp.generated.resources.Res
import localmind.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    LocalMindTheme {
        val drainCaptureQueue = koinInject<DrainCaptureQueueUseCase>()
        LaunchedEffect(Unit) {
            drainCaptureQueue()
        }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val destination = navBackStackEntry?.destination

        val currentScreen = bottomNavItems.find { item ->
            destination?.hasRoute(item.screen::class) == true
        }?.screen

        val isBottomNavScreen = currentScreen != null
        val isSettingsScreen = destination?.hasRoute(Screen.Settings::class) == true

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (isBottomNavScreen) {
                    CenterAlignedTopAppBar(
                        title = {
                            Image(
                                painter = painterResource(Res.drawable.app_icon),
                                contentDescription = "LocalMind",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.Settings) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        },
                    )
                } else if (isSettingsScreen) {
                    TopAppBar(
                        title = { Text("Settings") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                    )
                }
            },
            bottomBar = {
                if (isBottomNavScreen) {
                    BottomNavBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            navController.navigate(screen) {
                                popUpTo<Screen.Today> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                if (isBottomNavScreen) {
                    FloatingCaptureButton(
                        onClick = { navController.navigate(Screen.Capture) },
                    )
                }
            },
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
