package com.adn.adnapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.adn.adnapp.core.ui.MainScreen
import com.adn.adnapp.feature.auth.login.LoginScreen
import com.adn.adnapp.feature.auth.register.RegisterScreen
import com.adn.adnapp.feature.auth.welcome.WelcomeScreen
import com.adn.adnapp.feature.registration.dietselection.DietSelectionScreen
import com.adn.adnapp.feature.registration.userinfo.UserInfoScreen
import com.adn.adnapp.feature.splash.SplashScreen

@Composable
fun AdnNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = { navController.clearTo(Screen.Welcome) },
                onNavigateToUserInfo = { navController.clearTo(Screen.UserInfo) },
                onNavigateToDietSelection = { navController.clearTo(Screen.DietSelection) },
                onNavigateToMain = { navController.clearTo(Screen.Main) }
            )
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = navController::popBackStack,
                onNavigateToMain = { navController.clearTo(Screen.Main) },
                onNavigateToUserInfo = { navController.clearTo(Screen.UserInfo) },
                onNavigateToDietSelection = { navController.clearTo(Screen.DietSelection) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = navController::popBackStack,
                onNavigateToUserInfo = { navController.clearTo(Screen.UserInfo) }
            )
        }
        composable(Screen.UserInfo.route) {
            UserInfoScreen(onNavigateToDietSelection = { navController.navigate(Screen.DietSelection.route) })
        }
        composable(Screen.DietSelection.route) {
            DietSelectionScreen(onNavigateToMain = { navController.clearTo(Screen.Main) })
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSplash = { navController.clearTo(Screen.Welcome) },
                onNavigateToDietSelection = { navController.navigate(Screen.DietSelection.route) }
            )
        }
    }
}

private fun NavHostController.clearTo(screen: Screen) {
    navigate(screen.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
