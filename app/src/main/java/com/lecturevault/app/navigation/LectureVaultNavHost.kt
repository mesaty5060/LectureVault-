package com.lecturevault.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lecturevault.app.ui.screens.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Subjects : Screen("subjects", "Subjects", Icons.Default.Folder)
    data object Favorites : Screen("favorites", "Favorites", Icons.Default.Star)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private val bottomItems = listOf(Screen.Home, Screen.Subjects, Screen.Favorites, Screen.Search, Screen.Settings)
private val noBottomBarRoutes = setOf("onboarding", "camera/{id}")

@Composable
fun LectureVaultNavHost(startWithOnboarding: Boolean) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottom = current != null && current !in noBottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottom && bottomItems.any { it.route == current }) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        NavigationBarItem(
                            selected = current == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startWithOnboarding) "onboarding" else Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable("onboarding") {
                OnboardingScreen(onDone = {
                    navController.navigate(Screen.Home.route) { popUpTo("onboarding") { inclusive = true } }
                })
            }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Subjects.route) { SubjectsScreen(navController) }
            composable(Screen.Favorites.route) { FavoritesScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable("subject/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                SubjectDetailScreen(id, navController)
            }
            composable("note/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                NoteViewerScreen(id, navController)
            }
            composable("camera/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                CameraCaptureScreen(id, navController)
            }
            composable("trash") { TrashScreen(navController) }
            composable("reminders") { RemindersScreen(navController) }
        }
    }
}
