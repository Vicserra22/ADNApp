package com.adn.adnapp.core.di

import com.adn.adnapp.data.remote.firebase.AuthDataSource
import com.adn.adnapp.data.remote.firebase.FirestoreDataSource
import com.adn.adnapp.data.remote.firebase.WeightDataSource
import com.adn.adnapp.data.local.DashboardPreferences
import com.adn.adnapp.feature.splash.SplashViewModel
import com.adn.adnapp.feature.auth.login.LoginViewModel
import com.adn.adnapp.feature.auth.register.RegisterViewModel
import com.adn.adnapp.feature.registration.userinfo.UserInfoViewModel
import com.adn.adnapp.feature.registration.dietselection.DietSelectionViewModel
import com.adn.adnapp.feature.home.HomeViewModel
import com.adn.adnapp.feature.home.CustomDishViewModel
import com.adn.adnapp.feature.dashboard.DashboardViewModel
import com.adn.adnapp.feature.profile.ProfileViewModel
import com.adn.adnapp.feature.dayviewer.DayViewerViewModel
import com.adn.adnapp.feature.registration.priorities.PrioritiesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { Firebase.firestore }
    
    single { AuthDataSource(get()) }
    single { FirestoreDataSource(get()) }
    single { WeightDataSource(get()) }
    single { DashboardPreferences(get()) }
    
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::UserInfoViewModel)
    viewModelOf(::DietSelectionViewModel)
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { CustomDishViewModel(get(), get(), get()) }
    viewModelOf(::DashboardViewModel)
    viewModelOf(::ProfileViewModel)
    viewModel { parameters -> DayViewerViewModel(parameters.get(), get(), get(), get(), get(), get()) }
    viewModelOf(::PrioritiesViewModel)
}
