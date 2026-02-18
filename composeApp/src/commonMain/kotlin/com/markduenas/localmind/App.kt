package com.markduenas.localmind

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.markduenas.localmind.ui.components.FloatingCaptureButton
import com.markduenas.localmind.ui.navigation.BottomNavBar
import com.markduenas.localmind.ui.navigation.NavGraph
import com.markduenas.localmind.ui.navigation.Screen
import com.markduenas.localmind.ui.navigation.bottomNavItems
import com.markduenas.localmind.ui.theme.LocalMindTheme

@Composable
fun App() {
    LocalMindTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val destination = navBackStackEntry?.destination

        val currentScreen = bottomNavItems.find { item ->
            destination?.hasRoute(item.screen::class) == true
        }?.screen

        val isBottomNavScreen = currentScreen != null

        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
