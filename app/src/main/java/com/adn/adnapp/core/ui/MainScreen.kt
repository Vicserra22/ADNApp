package com.adn.adnapp.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adn.adnapp.R
import com.adn.adnapp.core.navigation.Screen
import com.adn.adnapp.feature.dashboard.DashboardScreen
import com.adn.adnapp.feature.home.HomeScreen
import com.adn.adnapp.feature.profile.ProfileScreen

@Composable
fun MainScreen(
    onNavigateToSplash: () -> Unit
) {
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
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { screen ->
                    val labelRes = when(screen) {
                        Screen.Home -> R.string.title_home
                        Screen.Dashboard -> R.string.title_dashboard
                        Screen.Profile -> R.string.title_profile
                        else -> R.string.app_name
                    }
                    val icon = when(screen) {
                        Screen.Home -> Icons.Default.Home
                        Screen.Dashboard -> Icons.Default.List
                        Screen.Profile -> Icons.Default.Person
                        else -> Icons.Default.Home
                    }
                    
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = stringResource(labelRes)) },
                        label = { Text(stringResource(labelRes)) },
                        selected = currentRoute == screen.route,
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onNavigateToSplash = onNavigateToSplash) }
        }
    }
}
