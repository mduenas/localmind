package com.markduenas.localmind.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("Today", Icons.Default.Today, Screen.Today),
    BottomNavItem("Calendar", Icons.Default.CalendarMonth, Screen.Calendar),
    BottomNavItem("Upcoming", Icons.Default.DateRange, Screen.Upcoming),
    BottomNavItem("All", Icons.Default.Checklist, Screen.AllTasks),
    BottomNavItem("Notes", Icons.AutoMirrored.Filled.StickyNote2, Screen.Notes),
)

@Composable
fun BottomNavBar(
    currentScreen: Screen?,
    onNavigate: (Screen) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
