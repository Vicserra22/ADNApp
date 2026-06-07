package com.adn.adnapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.adn.adnapp.feature.auth.welcome.WelcomeScreen
import com.adn.adnapp.feature.splash.SplashScreen

@Composable
fun AdnNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegistration = {
                    navController.navigate(Screen.RegistrationFlow.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            com.adn.adnapp.feature.auth.login.LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegistrationFlow = {
                    navController.navigate(Screen.RegistrationFlow.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            com.adn.adnapp.feature.auth.register.RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegistrationFlow = {
                    navController.navigate(Screen.RegistrationFlow.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        navigation(startDestination = Screen.UserInfo.route, route = Screen.RegistrationFlow.route) {
            composable(Screen.UserInfo.route) {
                com.adn.adnapp.feature.registration.userinfo.UserInfoScreen(
                    onNavigateToDietSelection = { navController.navigate(Screen.DietSelection.route) }
                )
            }
            composable(Screen.DietSelection.route) {
                com.adn.adnapp.feature.registration.dietselection.DietSelectionScreen(
                    onNavigateToMain = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.RegistrationFlow.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Screen.Main.route) {
            com.adn.adnapp.core.ui.MainScreen(
                onNavigateToSplash = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
