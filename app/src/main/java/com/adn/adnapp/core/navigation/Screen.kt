package com.adn.adnapp.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object UserInfo : Screen("user_info")
    object Priorities : Screen("priorities")
    object DietSelection : Screen("diet_selection")
    object Main : Screen("main")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Soon : Screen("soon")
    object DayViewer : Screen("day/{date}") {
        fun createRoute(date: String) = "day/$date"
    }
}
