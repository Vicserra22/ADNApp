package com.example.adnapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adnapp.R
import com.example.adnapp.ui.dashboard.DashboardScreen
import com.example.adnapp.ui.dashboard.DashboardViewModel
import com.example.adnapp.ui.home.FoodScreen
import com.example.adnapp.ui.home.FoodViewModel
import com.example.adnapp.ui.profile.ProfileScreen

sealed class Screen(val route: String, val resourceId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.title_home, Icons.Filled.Home)
    object Dashboard : Screen("dashboard", R.string.title_dashboard, Icons.Filled.DateRange)
    object Profile : Screen("profile", R.string.title_profile, Icons.Filled.Person)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                val foodViewModel: FoodViewModel = viewModel()
                FoodScreen(foodViewModel)
            }
            composable(Screen.Dashboard.route) { 
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(dashboardViewModel) 
            }
            composable(Screen.Profile.route) { 
                ProfileScreen()
            }
        }
    }
}
